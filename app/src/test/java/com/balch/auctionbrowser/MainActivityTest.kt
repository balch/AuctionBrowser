package com.balch.auctionbrowser

import com.balch.auctionbrowser.auction.AuctionView
import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.note.Note
import com.balch.auctionbrowser.note.NoteDao
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations.initMocks

class MainActivityTest {

    @Mock lateinit internal var mockView: AuctionView
    @Mock lateinit internal var mockAuctionDatabase: AuctionDatabase
    @Mock lateinit internal var mockNoteDoa: NoteDao

    private val auctionViewModel = spy(AuctionViewModel())

    lateinit private var activity: MainActivity
    lateinit private var modelProvider: ModelProvider

    @Before
    @Throws(Exception::class)
    fun setUp() {
        initMocks(this)

        `when`(mockAuctionDatabase.noteDao()).thenReturn(mockNoteDoa)

        modelProvider = spy<ModelProvider>(object : AuctionApplication() {
            override var database: AuctionDatabase = mockAuctionDatabase
        })

        activity = spy<MainActivity>(object : MainActivity() {
            override fun createView(): AuctionView {
                view = mockView
                return mockView
            }
        })

        doReturn("").`when`<MainActivity>(activity).getString(eq(R.string.ebay_app_id))
        doReturn(auctionViewModel).`when`<MainActivity>(activity).getAuctionViewModel()
        doReturn(false).`when`<MainActivity>(activity).handleIntent()

        activity.createView()
        activity.createModel(modelProvider)
    }

    @Test
    @Throws(Exception::class)
    fun testOnLoadMore() {
        val page = 4
        doReturn(true).`when`(auctionViewModel).hasMoreAuctionPages(anyLong())
        doNothing().`when`(auctionViewModel).loadAuctionsNextPage()

        assertTrue(activity.onLoadMore(page))

        verify<AuctionView>(mockView).showBusy()
        verify(auctionViewModel).hasMoreAuctionPages(page.toLong())
        verify(auctionViewModel).loadAuctionsNextPage()
    }

    @Test
    @Throws(Exception::class)
    fun testOnLoadMoreNoMore() {
        val page = 4
        doReturn(false).`when`(auctionViewModel).hasMoreAuctionPages(anyLong())

        assertFalse(activity.onLoadMore(page))

        verify<AuctionView>(mockView, never()).showBusy()
        verify(auctionViewModel).hasMoreAuctionPages(page.toLong())
        verify(auctionViewModel, never()).loadAuctionsNextPage()
    }

    @Test
    @Throws(Exception::class)
    fun testSaveNote() {
        val auction = mock(Auction::class.java)
        val note = mock(Note::class.java)
        val text = "test text"

        activity.saveNote(auction, note, text)

        verify(note).noteText = text
        verify(mockNoteDoa).update(note)
    }

}