package com.balch.auctionbrowser.auction.model;

import com.balch.auctionbrowser.AuctionData;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface EbayApi {
    @GET("services/search/FindingService/v1?OPERATION-NAME=findItemsByKeywords"
            + "&OPERATION-NAME=findItemsByKeywords"
            + "&SERVICE-VERSION=1.0.0"
            + "&RESPONSE-DATA-FORMAT=JSON"
            + "&REST-PAYLOAD"
    )

    Observable<AuctionData> findItemsByKeywords(@Query("keywords") String keywords,
                                                @Query("pageNumber") long pageNumber,
                                                @Query("entriesPerPage") int entriesPerPage,
                                                @Query("sortOrder") String sortOrder,
                                                @Query("SECURITY-APPNAME") String appName);

}
