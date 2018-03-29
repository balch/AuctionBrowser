/*
 * Author: Balch
 *
 * This file is part of AuctionBrowser.
 *
 * AuctionBrowser is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuctionBrowser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MockTrade.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2017
 *
 */

package com.balch.auctionbrowser

import android.arch.lifecycle.LifecycleOwner
import com.balch.auctionbrowser.auction.AuctionAdapter
import com.balch.auctionbrowser.auction.AuctionPresenter
import com.balch.auctionbrowser.auction.AuctionView
import com.balch.auctionbrowser.auction.AuctionViewModel
import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.auction.model.AuctionData
import com.balch.auctionbrowser.auction.model.EBayModel
import com.balch.auctionbrowser.note.Note
import com.balch.auctionbrowser.note.NotesModel
import com.balch.auctionbrowser.test.BaseTest
import com.balch.auctionbrowser.test.anyArg
import com.balch.auctionbrowser.test.makeCaptor
import com.balch.auctionbrowser.test.uninitialized
import io.reactivex.Observable
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations.initMocks


class AuctionPresenterTest : BaseTest() {

    @Mock
    private lateinit var mockView: AuctionView
    @Mock
    private lateinit var lifecycleOwner: LifecycleOwner
    @Mock
    private lateinit var bridge: AuctionPresenter.ActivityBridge
    @Mock
    private lateinit var ebayModel: EBayModel
    @Mock
    private lateinit var notesModel: NotesModel

    lateinit var auctionAdapter: AuctionAdapter
    lateinit var auctionViewModel: AuctionViewModel

    private lateinit var presenter: AuctionPresenter

    @Before
    fun setUp() {
        initMocks(this)

        auctionAdapter = spy(AuctionAdapter())
        auctionViewModel = spy(AuctionViewModel(auctionAdapter, ebayModel, notesModel))
        presenter = spy(AuctionPresenter(mockView, auctionViewModel, bridge))

        doReturn(Observable.just(Unit)).`when`(mockView).onLoadMore
        doNothing().`when`(auctionAdapter).notifyDataSetChanged()

        doReturn(lifecycleOwner).`when`(bridge).lifecycleOwner
    }

    @Test
    fun testOnLoadMore() {
        doNothing().`when`(auctionViewModel).loadAuctionsNextPage()

        //region Execute Test
        presenter.onLoadMorePages()
        //endregion

        verify(mockView).showBusy = true
        verify(auctionViewModel).loadAuctionsNextPage()
    }

    @Test
    fun testSaveNote_update() {
        val auction = mock(Auction::class.java)
        val note = mock(Note::class.java)
        val text = "test text"

        //region Execute Test
        presenter.saveNote(auction, note, text)
        testScheduler.triggerActions()
        //endregion

        verify(note).noteText = text
        verify(notesModel).update(note)
    }

    @Test
    fun testSaveNote_insert() {
        val auction = mock(Auction::class.java)
        val text = "test text"

        //region Execute Test
        presenter.saveNote(auction, null, text)
        testScheduler.triggerActions()
        //endregion

        val (verifier, captors) = makeCaptor(notesModel, Note::class.java)
        verifier.insert(uninitialized())

        val note: Note = captors[0].value as Note
        assertThat(note.noteText).isEqualTo(text)

        verify(auctionAdapter).addNote(auction, note)
    }

    @Test
    fun testShowAuctions() {
        val auctionData: AuctionData = AuctionData().apply {
            auctions = ArrayList()
            notes = HashMap()
        }

        //region Execute Test
        presenter.showAuctions(auctionData)
        //endregion

        verify(mockView).showBusy = false
        verify(auctionAdapter).addAuctions(auctionData.auctions, auctionData.notes)
        verify(mockView).doneLoading(false)
    }

    @Test
    fun testClearNote() {
        val auction: Auction = mock(Auction::class.java)
        val note: Note = mock(Note::class.java)

        //region Execute Test
        presenter.clearNote(auction, note)
        testScheduler.triggerActions()
        //endregion

        verify(notesModel).delete(note)
        verify(auctionAdapter).clearNote(auction)
    }

    @Test
    fun testClearNote_null() {
        val auction: Auction = mock(Auction::class.java)

        //region Execute Test
        presenter.clearNote(auction, null)
        //endregion

        verify(notesModel, never()).delete(anyArg())
        verify(auctionAdapter, never()).clearNote(anyArg())
    }
}
