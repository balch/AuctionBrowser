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

package com.balch.auctionbrowser.auction.model

import android.content.Context
import androidx.annotation.MainThread
import androidx.lifecycle.Transformations.switchMap
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.balch.auctionbrowser.AuctionDataSourceFactory
import com.balch.auctionbrowser.base.Listing
import com.balch.auctionbrowser.dagger.BaseApplicationModule.APP_CONTEXT
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class EBayRepository @Inject constructor(
        @Named(APP_CONTEXT) private val context: Context,
        private val networkExecutor: Executor)
    : AuctionRepository {

    @MainThread
    override fun searchAuctions(searchQuery: String, sortColumn: EBayModel.SortColumn, pageSize: Int): Listing<Auction> {
        val factory = auctionDataSourceFactory(searchQuery, sortColumn)

        val config = pagedListConfig(pageSize)

        val livePagedList = LivePagedListBuilder(factory, config)
                .setFetchExecutor(networkExecutor)
                .build()

        return Listing(
                pagedList = livePagedList,
                networkState = switchMap(factory.source) { it.networkState })
    }

    private fun auctionDataSourceFactory(searchQuery: String, sortColumn: EBayModel.SortColumn): AuctionDataSourceFactory {
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

