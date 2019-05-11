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
 * Copyright (C) 2018
 *
 */

package com.balch.auctionbrowser.auction

import android.content.Context
import androidx.annotation.MainThread
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.balch.auctionbrowser.AuctionDataSourceFactory
import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.auction.model.EBayRepository
import com.balch.auctionbrowser.base.Listing
import java.util.concurrent.Executor


/**
 * This ViewModel exposes a LiveData object which emits AuctionData objects from the
 * EBay API. An AuctionData object contains the current page of found Auctions.
 *
 * The class implements simple dependency injection using the `inject()` method to setter-inject
 * the Adapter and ModelApis.
 */
class AuctionViewModel(private val context: Context,
                       private val networkExecutor: Executor) : ViewModel() {

    companion object {
        private const val AUCTION_FETCH_COUNT = 30
    }

    private class SearchData(val searchText: String,
                             val sortColumn: EBayRepository.SortColumn)

    private val searchQuery = MutableLiveData<SearchData>()
    private val auctionResult = map(searchQuery) {
        searchAuctions(it.searchText, it.sortColumn, AUCTION_FETCH_COUNT)
    }
    val auctionData = switchMap(auctionResult) { it.pagedList }!!
    val networkState = switchMap(auctionResult) { it.networkState }!!

    val searchText: String
        get() = searchQuery.value?.searchText ?: ""

    val sortColumn: EBayRepository.SortColumn
        get() = searchQuery.value?.sortColumn ?: EBayRepository.SortColumn.BEST_MATCH

    fun loadAuctions(searchText: String, sortColumn: EBayRepository.SortColumn)  {
        searchQuery.value = SearchData(searchText, sortColumn)
    }

    @MainThread
    private fun searchAuctions(searchQuery: String, sortColumn: EBayRepository.SortColumn, pageSize: Int): Listing<Auction> {
        val factory = auctionDataSourceFactory(searchQuery, sortColumn)

        val config = pagedListConfig(pageSize)

        val livePagedList = LivePagedListBuilder(factory, config)
                .setFetchExecutor(networkExecutor)
                .build()

        return Listing(
                pagedList = livePagedList,
                networkState = switchMap(factory.source) { it.networkState })
    }

    private fun auctionDataSourceFactory(searchQuery: String, sortColumn: EBayRepository.SortColumn): AuctionDataSourceFactory {
        return AuctionDataSourceFactory(context,  searchQuery, sortColumn)
    }

    private fun pagedListConfig(pageSize: Int): PagedList.Config {
        return PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setInitialLoadSizeHint(pageSize * 2)
                .setPageSize(pageSize)
                .build()
    }


}


