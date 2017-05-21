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

package com.balch.auctionbrowser.auction.model;

import android.text.TextUtils;

import com.balch.auctionbrowser.AuctionData;

import io.reactivex.Observable;

public class EBayModel {
    private final String eBayApiKey;
    private final EbayApi ebayApi;

    public enum SortColumn {
        BEST_MATCH("BestMatch"),
        ENDING_SOONEST("EndTimeSoonest"),
        LOWEST_PRICE("PricePlusShippingLowest");

        private final String sortTerm;

        SortColumn(String sortTerm) {
            this.sortTerm = sortTerm;
        }
    }

    public EBayModel(String eBayApiKey, EbayApi ebayApi) {
        this.ebayApi = ebayApi;
        this.eBayApiKey = eBayApiKey;
    }

    public Observable<AuctionData> getAuctions(String keyword, long start, final int count, SortColumn sortColumn) {

        if (!TextUtils.isEmpty(keyword)) {
            return ebayApi.findItemsByKeywords(keyword, start + 1, count, sortColumn.sortTerm, eBayApiKey);
        } else {
            return Observable.just(new AuctionData());
        }
    }

 }
