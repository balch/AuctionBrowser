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
 * Copyright (C) 2019
 *
 */

package com.balch.auctionbrowser.auction;

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.auction.model.AuctionData
import com.balch.auctionbrowser.auction.model.EBayModel
import com.balch.auctionbrowser.base.NetworkState
import com.balch.auctionbrowser.ext.component
import com.balch.auctionbrowser.note.NotesModel
import timber.log.Timber
import javax.inject.Inject

class AuctionDataSource constructor(
        context: Context,
        private val searchText: String,
        private val sortColumn: EBayModel.SortColumn)
    : PageKeyedDataSource<Long, Auction>() {

    @Inject lateinit var auctionModel: EBayModel
    @Inject lateinit var notesModel: NotesModel
    init {
        context.component.inject(this)
    }
    companion object {
        private const val AUCTION_FETCH_COUNT = 30
    }

    val networkState = MutableLiveData<NetworkState>()

    override fun loadInitial(params: LoadInitialParams<Long>,
                    callback: LoadInitialCallback<Long, Auction>) {

        networkState.postValue(NetworkState.LOADING)

        loadAuctions(1) {
            auctionData ->
                callback.onResult(auctionData.auctions, null,
                        if (auctionData.totalPages > 1) 2 else null)
        }
    }

    override fun loadBefore(params: LoadParams<Long>,
                   callback: LoadCallback<Long, Auction>) {

    }

    override fun loadAfter(params: LoadParams<Long>,
                  callback: LoadCallback<Long, Auction>) {

        networkState.postValue(NetworkState.LOADING)
        loadAuctions(params.key) {
            auctionData ->
                callback.onResult(auctionData.auctions,
                        if (auctionData.totalPages > params.key) params.key + 1 else null)
        }
    }

    private fun loadAuctions(page:Long, callback:(AuctionData)->Unit) {

        try {
            val auctionData = auctionModel.getAuctions(searchText, page, AUCTION_FETCH_COUNT, sortColumn)
                    .doOnSuccess { auctionData ->
                        val notes = notesModel.getNotes(auctionData.auctions)
                        auctionData.auctions.forEach{it.note = notes[it.itemId]}
                    }
                    .blockingGet()

            callback(auctionData)
            networkState.postValue(NetworkState.LOADED)
        } catch (ex: Throwable) {
            Timber.e(ex, "Error in .getAuctions()")
            networkState.postValue(NetworkState(NetworkState.Status.FAILED,
                    ex.message ?: "Unknown Error"))
        }
    }

}
