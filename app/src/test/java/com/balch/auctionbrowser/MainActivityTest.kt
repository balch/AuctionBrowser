package com.balch.auctionbrowser

import com.balch.android.app.framework.sql.SqlConnection
import com.balch.auctionbrowser.auction.AuctionView
import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.note.Note

import org.junit.Before
import org.junit.Test
import org.mockito.Mock

import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.runner.RunWith
import org.mockito.Matchers.anyInt
import org.mockito.Matchers.eq
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations.initMocks

class MainActivityTest {

    @Mock lateinit internal var mockView: AuctionView
    @Mock lateinit internal var mockSqlConnection: SqlConnection

    private val auctionViewModel = spy(AuctionViewModel())

    lateinit private var activity: MainActivity
    lateinit private var modelProvider: AuctionModelProvider

    @Before
    @Throws(Exception::class)
    fun setUp() {
        initMocks(this)

        modelProvider = spy<AuctionModelProvider>(object : AuctionApplication() {
            override var sqlConnection: SqlConnection = mockSqlConnection
        })

        activity = spy<MainActivity>(object : MainActivity() {
            init {
                createView()
                createModel(modelProvider)
            }

            override fun createView(): AuctionView {
                view = mockView
                return mockView
            }
        })

        doReturn("").`when`<MainActivity>(activity).getString(eq(R.string.ebay_app_id))
        doReturn(auctionViewModel).`when`<MainActivity>(activity).getAuctionViewModel()
        doReturn(false).`when`<MainActivity>(activity).handleIntent()
    }

    @Test
    @Throws(Exception::class)
    fun testOnLoadMore() {
        val page = 4
        doReturn(true).`when`(auctionViewModel).hasMoreAuctionPages(anyInt().toLong())
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
        doReturn(false).`when`(auctionViewModel).hasMoreAuctionPages(anyInt().toLong())

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

        verify(note).note = eq(text)
        verify(mockSqlConnection).update(eq(activity.getAuctionViewModel().notesModel), eq(note))
    }

}