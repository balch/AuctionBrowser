/*
 *  Author: Balch
 *  Created: 6/14/15 11:21 AM
 *
 *  This file is part of BB_Challenge.
 *
 *  BB_Challenge is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  BB_Challenge is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MockTrade.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Copyright (C) 2015
 *
 */

package com.balch.auctionbrowser.auction;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.balch.android.app.framework.types.ISO8601DateTime;
import com.balch.android.app.framework.types.Money;
import com.balch.auctionbrowser.NetworkRequestProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class EBayModel {
    private static final String TAG = EBayModel.class.getSimpleName();

    private static final String EBAY_URL_BASE = "http://svcs.ebay.com/";
    private static final String EBAY_FINDING_SERVICE_PATH = "services/search/FindingService/v1";

    private static final String EBAY_SERVICE_PARAMS =
                    "?paginationInput.pageNumber=%d" +
                    "&paginationInput.entriesPerPage=%d" +
                    "&SECURITY-APPNAME=%s" +
                    "&OPERATION-NAME=%s" +
                    "&SERVICE-VERSION=1.0.0" +
                    "&RESPONSE-DATA-FORMAT=JSON" +
                    "&REST-PAYLOAD" +
                    "&keywords=%s" +
                    "&sortOrder=%s";

    private static final long TIMEOUT_SECS = 30;

    private final NetworkRequestProvider networkRequest;
    private final String eBayApiKey;

    public EBayModel(String eBayApiKey, NetworkRequestProvider networkRequest) {
        this.networkRequest = networkRequest;
        this.eBayApiKey = eBayApiKey;
    }

    public static class AuctionInfo {
        public final List<Auction> auctions;
        public final int totalPages;

        public AuctionInfo(List<Auction> auctions, int totalPages) {
            this.auctions = auctions;
            this.totalPages = totalPages;
        }
    }

    public LiveData<AuctionInfo> getAuctions(String keyword, long start, final int count,
                                             String sortOrder) {

        final MutableLiveData<AuctionInfo> auctionInfoLive = new MutableLiveData<>();

        if (!TextUtils.isEmpty(keyword)) {
            try {
                final String url = EBAY_URL_BASE + EBAY_FINDING_SERVICE_PATH +
                        String.format(EBAY_SERVICE_PARAMS, start + 1, count,
                                eBayApiKey,
                                "findItemsByKeywords",
                                URLEncoder.encode(keyword, "UTF-8"), sortOrder);


                Log.d(TAG, "ebay request: " + url.replace(eBayApiKey, "{secured}"));

                JsonObjectRequest request = new JsonObjectRequest(url, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    JSONObject items = response.getJSONArray("findItemsByKeywordsResponse").getJSONObject(0);
                                    boolean success = validateResponse(items);

                                    Log.d(TAG, "ebay request success status: " + success);

                                    if (success) {
                                        int totalPages = getTotalPages(items);
                                        List<Auction> auctions = parseAuctions(items);

                                        Log.d(TAG, "ebay request total pages: " + totalPages);

                                        auctionInfoLive.setValue(new AuctionInfo(auctions, totalPages));

                                    } else {
                                        handleError(auctionInfoLive);
                                    }

                                } catch (Exception e) {
                                    Log.e(TAG, "Exception on " + url, e);
                                    handleError(auctionInfoLive);
                                }

                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                handleError(auctionInfoLive);
                            }
                        });
                networkRequest.addRequest(request);

            } catch (Exception ex) {
                Log.e(TAG, "Parse Error", ex);
                handleError(auctionInfoLive);
            }
        }

        return auctionInfoLive;
    }

    private void handleError(MutableLiveData<AuctionInfo> auctionInfoLive) {
        auctionInfoLive.setValue(new AuctionInfo(null, -1));
    }

    private boolean validateResponse(JSONObject json) throws JSONException {
        return ((json != null) &&
                json.getJSONArray("ack").getString(0).equals("Success"));
    }

    private int getTotalPages(JSONObject json) throws JSONException {
        JSONObject paginationOutput = json.getJSONArray("paginationOutput").getJSONObject(0);
        return paginationOutput.getJSONArray("totalPages").optInt(0, -1);
    }

    private List<Auction> parseAuctions(JSONObject response) throws JSONException, ParseException {
        List<Auction> auctions = new ArrayList<>();
        JSONArray items = response.getJSONArray("searchResult").getJSONObject(0).getJSONArray("item");
        for(int x = 0; x < items.length(); x++){
            auctions.add(parseAuction(items.getJSONObject(x)));
        }
        return auctions;
    }

    private Auction parseAuction(JSONObject item) throws JSONException, ParseException {

        Auction auction = new Auction();
        auction.setItemId(item.getJSONArray("itemId").getLong(0));
        auction.setTitle(item.getJSONArray("title").getString(0));

        JSONArray jsonArray = item.optJSONArray("viewItemURL");
        auction.setListingUrl((jsonArray != null) ? jsonArray.getString(0) : "");

        jsonArray = item.optJSONArray("galleryURL");
        auction.setImageUrl((jsonArray != null) ? jsonArray.getString(0) : "");

        jsonArray = item.optJSONArray("location");
        auction.setLocation((jsonArray != null) ? jsonArray.getString(0) : "");

        JSONObject sellingStatus = item.getJSONArray("sellingStatus").getJSONObject(0);
        double convertedCurrentPrice = sellingStatus.getJSONArray("convertedCurrentPrice").getJSONObject(0).getDouble("__value__");
        auction.setCurrentPrice(new Money(convertedCurrentPrice));

        JSONArray shippingServiceCost = item.optJSONArray("shippingInfo").getJSONObject(0).optJSONArray("shippingServiceCost");
        if (shippingServiceCost != null) {
            double cost = shippingServiceCost.getJSONObject(0).getDouble("__value__");
            auction.setShippingCost(new Money(cost));
        }

        JSONArray listingInfoWrapper = item.getJSONArray("listingInfo");
        if (listingInfoWrapper != null) {
            JSONObject listingInfo = listingInfoWrapper.getJSONObject(0);
            String listingType = listingInfo.getJSONArray("listingType").getString(0);

            if ("Auction".equals(listingType)) {
                auction.setAuction(true);
                auction.setBuyItNow(listingInfo.getJSONArray("buyItNowAvailable").getBoolean(0));
            } else {
                auction.setAuction(false);
                auction.setBuyItNow(true);
            }

            auction.setStartTime(ISO8601DateTime.toDate(listingInfo.getJSONArray("startTime").getString(0)));
            auction.setEndTime(ISO8601DateTime.toDate(listingInfo.getJSONArray("endTime").getString(0)));
        }

        return auction;
    }

 }
