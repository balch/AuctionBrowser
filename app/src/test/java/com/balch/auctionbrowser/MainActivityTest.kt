package com.balch.auctionbrowser

import com.balch.auctionbrowser.auction.AuctionView
import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.note.Note
import com.balch.auctionbrowser.test.*
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations.initMocks

class MainActivityTest {

    @Mock lateinit private var mockView: AuctionView

    private val auctionViewModel = spy(AuctionViewModel())
    private val modelProvider = TestModelProvider()

    lateinit private var activity: MainActivity

    @Before
    fun setUp() {
        initMocks(this)

        activity = spy(MainActivity())

        activity.view = mockView

        doReturn(Schedulers.from(CurrentThreadExecutor())).`when`(activity).mainThread
        doReturn(Schedulers.from(CurrentThreadExecutor())).`when`(activity).ioThread
        doReturn("").`when`(activity).getString(eq(R.string.ebay_app_id))
        doReturn(auctionViewModel).`when`(activity).getAuctionViewModel()
        doReturn(false).`when`(activity).handleIntent()
        doReturn(false).`when`(activity).isFinishing

        activity.createModelInternal(modelProvider)
    }

    @Test
    fun testOnLoadMore() {
        val page = 4
        doReturn(true).`when`(auctionViewModel).hasMoreAuctionPages(anyLong())
        doNothing().`when`(auctionViewModel).loadAuctionsNextPage()

        assertTrue(activity.onLoadMore(page))

        verify(mockView).showBusy()
        verify(auctionViewModel).hasMoreAuctionPages(page.toLong())
        verify(auctionViewModel).loadAuctionsNextPage()
    }

    @Test
    fun testOnLoadMoreNoMore() {
        val page = 4
        doReturn(false).`when`(auctionViewModel).hasMoreAuctionPages(anyLong())

        assertFalse(activity.onLoadMore(page))

        verify(mockView, never()).showBusy()
        verify(auctionViewModel).hasMoreAuctionPages(page.toLong())
        verify(auctionViewModel, never()).loadAuctionsNextPage()
    }

    @Test
    fun testSaveNote_update() {
        val auction = mock(Auction::class.java)
        val note = mock(Note::class.java)
        val text = "test text"

        activity.saveNote(auction, note, text)

        verify(note).noteText = text
        verify(modelProvider.mockNotesDao).update(note)
    }

    @Test
    fun testSaveNote_insert() {
        val auction = mock(Auction::class.java)
        val text = "test text"

        activity.saveNote(auction, null, text)

        val (verifier, captor) = makeCaptor(modelProvider.mockNotesDao, Note::class.java)
        verifier.insert(uninitialized())

        val note: Note = captor.value
        assertTrue(note.noteText == text)

        verify(mockView).addNote(auction, note)
    }

    @Test
    fun testOnCreateInternal() {

        activity.onCreateInternal(null)

        verify(mockView).setAuctionAdapter(auctionViewModel.auctionAdapter)
        verify(activity).handleIntent()
    }

    @Test
    fun testShowAuctions() {
        val auctionData: AuctionData = AuctionData().apply {
            auctions = ArrayList<Auction>()
            notes = HashMap<Long, Note>()
        }

        activity.showAuctions(auctionData)

        verify(mockView).hideBusy()
        verify(mockView).addAuctions(auctionData.auctions, auctionData.notes)
        verify(mockView).doneLoading()
    }

    @Test
    fun testClearNote() {
        val auction: Auction = mock(Auction::class.java)
        val note: Note = mock(Note::class.java)

        activity.clearNote(auction, note)

        verify(modelProvider.mockNotesDao).delete(note)
        verify(mockView).clearNote(auction)
    }

    @Test
    fun testClearNote_null() {
        val auction: Auction = mock(Auction::class.java)

        activity.clearNote(auction, null)

        verify(modelProvider.mockNotesDao, never()).delete(anyArg())
        verify(mockView, never()).clearNote(anyArg())
    }

}