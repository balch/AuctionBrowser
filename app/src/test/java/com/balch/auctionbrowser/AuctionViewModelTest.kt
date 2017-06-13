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

import android.arch.lifecycle.MutableLiveData
import com.balch.auctionbrowser.auction.AuctionAdapter
import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.auction.model.EBayModel
import com.balch.auctionbrowser.note.NotesModel
import com.balch.auctionbrowser.test.BaseTest
import com.balch.auctionbrowser.test.TestModelProvider
import com.balch.auctionbrowser.test.anyArg
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations.initMocks



class AuctionViewModelTest: BaseTest() {
    lateinit private var viewModel: AuctionViewModel

    @Mock lateinit private var mockAdapter: AuctionAdapter
    @Mock lateinit private var mockAuctionDataLive: MutableLiveData<AuctionData>

    private val modelProvider = TestModelProvider()

    @Mock lateinit private var ebayModel: EBayModel
    lateinit private var notesModel: NotesModel

    @Before
    fun setUp() {

        initMocks(this)

        notesModel = NotesModel(modelProvider.mockNotesDao)

        viewModel = spy(AuctionViewModel())
        viewModel.inject(mockAdapter, ebayModel, notesModel)
        doReturn(mockAuctionDataLive).`when`(viewModel).auctionDataLive
    }

    @Test
    fun testLoadAuctions() {
        val searchText = "Search"
        val sortColumn = EBayModel.SortColumn.BEST_MATCH

        val auctions:MutableList<Auction> = ArrayList()
        auctions.add(mock(Auction::class.java))
        val auctionData = AuctionData(auctions)

        doReturn(Single.just(auctionData)).`when`(ebayModel).getAuctions(searchText, 1, 30, sortColumn)

        viewModel.loadAuctions(searchText, sortColumn)
        testScheduler.triggerActions()

        verify(ebayModel).getAuctions(searchText, 1, 30, sortColumn)
        verify(modelProvider.mockNotesDao).loadAllByIds(anyArg())
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

        val auctions:MutableList<Auction> = ArrayList()
        auctions.add(mock(Auction::class.java))
        val auctionData = AuctionData(auctions)
        doReturn(Single.just(auctionData)).`when`(ebayModel)
                .getAuctions(searchText, currentPage + 1L, 30, sortColumn)

        viewModel.loadAuctionsNextPage()
        testScheduler.triggerActions()

        verify(ebayModel).getAuctions(searchText, currentPage + 1L, 30, sortColumn)
        verify(modelProvider.mockNotesDao).loadAllByIds(anyArg())
        verify(mockAuctionDataLive).value = auctionData
    }

}