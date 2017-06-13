package com.balch.auctionbrowser

import com.balch.auctionbrowser.auction.AuctionView
import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.note.Note
import com.balch.auctionbrowser.test.*
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations.initMocks

class MainActivityTest: BaseTest() {

    @Mock lateinit private var mockView: AuctionView

    private val auctionViewModel = spy(AuctionViewModel())
    private val modelProvider = TestModelProvider()

    lateinit private var activity: MainActivity

    @Before
    fun setUp() {
        initMocks(this)

        activity = spy(MainActivity())

        activity.viewInternal = mockView

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

        //region Execute Test
        val hasMore = activity.onLoadMorePages(page)
        testScheduler.triggerActions()
        //endregion

        assertTrue(hasMore)
        verify(mockView).showBusy()
        verify(auctionViewModel).hasMoreAuctionPages(page.toLong())
        verify(auctionViewModel).loadAuctionsNextPage()
    }

    @Test
    fun testOnLoadMoreNoMore() {
        val page = 4
        doReturn(false).`when`(auctionViewModel).hasMoreAuctionPages(anyLong())

        //region Execute Test
        val hasMore = activity.onLoadMorePages(page)
        testScheduler.triggerActions()
        //endregion

        assertFalse(hasMore)
        verify(mockView, never()).showBusy()
        verify(auctionViewModel).hasMoreAuctionPages(page.toLong())
        verify(auctionViewModel, never()).loadAuctionsNextPage()
    }

    @Test
    fun testSaveNote_update() {
        val auction = mock(Auction::class.java)
        val note = mock(Note::class.java)
        val text = "test text"

        //region Execute Test
        activity.saveNote(auction, note, text)
        testScheduler.triggerActions()
        //endregion

        verify(note).noteText = text
        verify(modelProvider.mockNotesDao).update(note)
    }

    @Test
    fun testSaveNote_insert() {
        val auction = mock(Auction::class.java)
        val text = "test text"

        //region Execute Test
        activity.saveNote(auction, null, text)
        testScheduler.triggerActions()
        //endregion

        val (verifier, captors) = makeCaptor(modelProvider.mockNotesDao, Note::class.java)
        verifier.insert(uninitialized())

        val note: Note = captors[0].value as Note
        assertTrue(note.noteText == text)

        verify(mockView).addNote(auction, note)
    }

    @Test
    fun testOnCreateInternal() {

        //region Execute Test
        activity.onCreateInternal(null)
        //endregion

        verify(mockView).setAuctionAdapter(auctionViewModel.auctionAdapter)
        verify(activity).handleIntent()
    }

    @Test
    fun testShowAuctions() {
        val auctionData: AuctionData = AuctionData().apply {
            auctions = ArrayList<Auction>()
            notes = HashMap<Long, Note>()
        }

        //region Execute Test
        activity.showAuctions(auctionData)
        //endregion

        verify(mockView).hideBusy()
        verify(mockView).addAuctions(auctionData.auctions, auctionData.notes)
        verify(mockView).doneLoading()
    }

    @Test
    fun testClearNote() {
        val auction: Auction = mock(Auction::class.java)
        val note: Note = mock(Note::class.java)

        //region Execute Test
        activity.clearNote(auction, note)
        testScheduler.triggerActions()
        //endregion

        verify(modelProvider.mockNotesDao).delete(note)
        verify(mockView).clearNote(auction)
    }

    @Test
    fun testClearNote_null() {
        val auction: Auction = mock(Auction::class.java)

        //region Execute Test
        activity.clearNote(auction, null)
        //endregion

        verify(modelProvider.mockNotesDao, never()).delete(anyArg())
        verify(mockView, never()).clearNote(anyArg())
    }

}