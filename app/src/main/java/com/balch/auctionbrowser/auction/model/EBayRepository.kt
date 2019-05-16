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

package com.balch.auctionbrowser.auction.model

import com.balch.auctionbrowser.dagger.BaseApplicationModule
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * ModelApi for getting data from the EBayApi repository (EBay Rest Service)
 */
@Singleton
class EBayRepository
@Inject constructor(@Named(BaseApplicationModule.EBAY_APP_ID) private val eBayApiKey: String,
                    private val ebayApi: EBayApi) {

    enum class SortColumn constructor(internal val sortTerm: String) {
        BEST_MATCH("BestMatch"),
        ENDING_SOONEST("EndTimeSoonest"),
        LOWEST_PRICE("PricePlusShippingLowest")
    }

    suspend fun getAuctions(keyword: String, start: Long,
                    count: Int, sortColumn: SortColumn): AuctionData {
        val deferred = CompletableDeferred<AuctionData>()
        withContext(Dispatchers.IO) {
            val auctionData = if (keyword.isNotEmpty())
                ebayApi.findItemsByKeywords(keyword, start, count, sortColumn.sortTerm, eBayApiKey).await()
            else AuctionData()

            deferred.complete(auctionData)
        }

        return deferred.await()
    }
}
