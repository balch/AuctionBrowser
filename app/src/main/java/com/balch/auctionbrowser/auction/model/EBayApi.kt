package com.balch.auctionbrowser.auction.model

import com.balch.auctionbrowser.AuctionData

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface EBayApi {
    @GET("services/search/FindingService/v1?OPERATION-NAME=findItemsByKeywords"
            + "&SERVICE-VERSION=1.0.0"
            + "&RESPONSE-DATA-FORMAT=JSON"
            + "&REST-PAYLOAD")
    fun findItemsByKeywords(@Query("keywords") keywords: String,
                            @Query("paginationInput.pageNumber") pageNumber: Long,
                            @Query("paginationInput.entriesPerPage") entriesPerPage: Int,
                            @Query("sortOrder") sortOrder: String,
                            @Query("SECURITY-APPNAME") appName: String):

            Observable<AuctionData>

}
