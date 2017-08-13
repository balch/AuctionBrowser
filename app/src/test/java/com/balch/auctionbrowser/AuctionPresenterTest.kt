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
import com.balch.auctionbrowser.auction.AuctionView
import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.note.Note
import com.balch.auctionbrowser.test.*
import io.reactivex.Observable
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations.initMocks


class AuctionPresenterTest : BaseTest() {

    @Mock lateinit private var mockView: AuctionView
    @Mock lateinit private var lifecycleOwner: LifecycleOwner
    @Mock lateinit private var listener: AuctionPresenter.AuctionPresenterListener

    private val auctionViewModel = spy(AuctionViewModel())
    private val modelProvider = TestModelProvider()

    lateinit private var presenter: AuctionPresenter

    @Before
    fun setUp() {
        initMocks(this)

        presenter = spy(AuctionPresenter(mockView, auctionViewModel, lifecycleOwner, "ebayAppId", listener))

        doReturn(Observable.just(Unit)).`when`(mockView).onLoadMore

        doReturn(false).`when`(listener).isActivityFinishing

        presenter.createModelInternal(modelProvider)
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
        verify(modelProvider.database.noteDao()).update(note)
    }

    @Test
    fun testSaveNote_insert() {
        val auction = mock(Auction::class.java)
        val text = "test text"

        //region Execute Test
        presenter.saveNote(auction, null, text)
        testScheduler.triggerActions()
        //endregion

        val (verifier, captors) = makeCaptor(modelProvider.database.noteDao(), Note::class.java)
        verifier.insert(uninitialized())

        val note: Note = captors[0].value as Note
        assertThat(note.noteText).isEqualTo(text)

        verify(mockView).addNote(auction, note)
    }

    @Test
    fun testShowAuctions() {
        val auctionData: AuctionData = AuctionData().apply {
            auctions = ArrayList<Auction>()
            notes = HashMap<Long, Note>()
        }

        //region Execute Test
        presenter.showAuctions(auctionData)
        //endregion

        verify(mockView).showBusy = false
        verify(mockView).addAuctions(auctionData.auctions, auctionData.notes)
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

        verify(modelProvider.database.noteDao()).delete(note)
        verify(mockView).clearNote(auction)
    }

    @Test
    fun testClearNote_null() {
        val auction: Auction = mock(Auction::class.java)

        //region Execute Test
        presenter.clearNote(auction, null)
        //endregion

        verify(modelProvider.database.noteDao(), never()).delete(anyArg())
        verify(mockView, never()).clearNote(anyArg())
    }
}
