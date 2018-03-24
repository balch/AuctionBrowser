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

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface EBayApi {
    @GET("services/search/FindingService/v1?"
            + "OPERATION-NAME=findItemsByKeywords"
            + "&SERVICE-VERSION=1.0.0"
            + "&RESPONSE-DATA-FORMAT=JSON"
            + "&REST-PAYLOAD")
    fun findItemsByKeywords(@Query("keywords") keywords: String,
                            @Query("paginationInput.pageNumber") pageNumber: Long,
                            @Query("paginationInput.entriesPerPage") entriesPerPage: Int,
                            @Query("sortOrder") sortOrder: String,
                            @Query("SECURITY-APPNAME") appName: String): Single<AuctionData>
}
