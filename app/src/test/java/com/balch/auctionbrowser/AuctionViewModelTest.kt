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

import androidx.lifecycle.MutableLiveData
import com.balch.auctionbrowser.auction.AuctionAdapter
import com.balch.auctionbrowser.auction.AuctionViewModel
import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.auction.model.AuctionData
import com.balch.auctionbrowser.auction.model.EBayModel
import com.balch.auctionbrowser.note.NoteDao
import com.balch.auctionbrowser.note.NotesModel
import com.balch.auctionbrowser.test.BaseTest
import com.balch.auctionbrowser.test.anyArg
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations.initMocks


class AuctionViewModelTest : BaseTest() {
    private lateinit var viewModel: AuctionViewModel

    @Mock
    private lateinit var mockAdapter: AuctionAdapter
    @Mock
    private lateinit var mockAuctionDataLive: MutableLiveData<AuctionData>

    @Mock
    private lateinit var ebayModel: EBayModel
    @Mock
    private lateinit var noteDao: NoteDao
    private lateinit var notesModel: NotesModel

    @Before
    fun setUp() {

        initMocks(this)

        notesModel = NotesModel(noteDao)

        viewModel = spy(AuctionViewModel(mockAdapter, ebayModel, notesModel, mockAuctionDataLive))
    }

    @Test
    fun testLoadAuctions() {
        val searchText = "Search"
        val sortColumn = EBayModel.SortColumn.BEST_MATCH

        val auctions: MutableList<Auction> = ArrayList()
        auctions.add(mock(Auction::class.java))
        val auctionData = AuctionData(auctions)

        doReturn(Single.just(auctionData)).`when`(ebayModel).getAuctions(searchText, 1, 30, sortColumn)

        //region Execute Test
        viewModel.loadAuctions(searchText, sortColumn)
        testScheduler.triggerActions()
        //endregion

        verify(ebayModel).getAuctions(searchText, 1, 30, sortColumn)
        verify(noteDao).loadAllByIds(anyArg())
        verify(mockAuctionDataLive).value = auctionData
    }

    @Test
    fun testLoadAuctionsNextPage() {
        val searchText = "Search"
        val sortColumn = EBayModel.SortColumn.BEST_MATCH
        val currentPage = 5

        viewModel.searchText = searchText
        viewModel.sortColumn = sortColumn
        viewModel.currentPage = currentPage

        val auctions: MutableList<Auction> = ArrayList()
        auctions.add(mock(Auction::class.java))
        val auctionData = AuctionData(auctions)
        doReturn(Single.just(auctionData)).`when`(ebayModel)
                .getAuctions(searchText, currentPage + 1L, 30, sortColumn)

        //region Execute Test
        viewModel.loadAuctionsNextPage()
        testScheduler.triggerActions()
        //endregion

        verify(ebayModel).getAuctions(searchText, currentPage + 1L, 30, sortColumn)
        verify(noteDao).loadAllByIds(anyArg())
        verify(mockAuctionDataLive).value = auctionData
    }

}