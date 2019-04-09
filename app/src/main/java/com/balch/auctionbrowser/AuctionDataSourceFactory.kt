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

package com.balch.auctionbrowser

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.balch.auctionbrowser.auction.AuctionDataSource
import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.auction.model.EBayModel


class AuctionDataSourceFactory(
        private val context: Context,
        private val searchQuery: String,
        private val sortColumn: EBayModel.SortColumn) : DataSource.Factory<Long, Auction>() {

    val source = MutableLiveData<AuctionDataSource>()

    override fun create(): DataSource<Long, Auction> {
        val source = AuctionDataSource(context, searchQuery, sortColumn)
        this.source.postValue(source)
        return source
    }
}