/*
 *  Author: Balch
 *  Created: 6/14/15 11:24 AM
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

import android.os.Parcel;
import android.os.Parcelable;

import com.balch.android.app.framework.domain.DomainObject;
import com.balch.android.app.framework.types.Money;

import java.io.Serializable;
import java.util.Date;

public class Auction extends DomainObject implements Serializable, Parcelable {
    private long itemId;
    private String title;
    private String imageUrl;
    private String listingUrl;
    private String location;
    private Money shippingCost;
    private Money currentPrice;
    private String auctionSource;
    private Date startTime;
    private Date endTime;
    private boolean auction;
    private boolean buyItNow;

    public Auction() {

    }

    protected Auction(Parcel in) {
        super(in);
        itemId = in.readLong();
        title = in.readString();
        imageUrl = in.readString();
        listingUrl = in.readString();
        location = in.readString();
        shippingCost = in.readParcelable(Money.class.getClassLoader());
        currentPrice = in.readParcelable(Money.class.getClassLoader());
        auctionSource = in.readString();
        auction = in.readByte() != 0;
        buyItNow = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(itemId);
        dest.writeString(title);
        dest.writeString(imageUrl);
        dest.writeString(listingUrl);
        dest.writeString(location);
        dest.writeParcelable(shippingCost, flags);
        dest.writeParcelable(currentPrice, flags);
        dest.writeString(auctionSource);
        dest.writeByte((byte) (auction ? 1 : 0));
        dest.writeByte((byte) (buyItNow ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Auction> CREATOR = new Creator<Auction>() {
        @Override
        public Auction createFromParcel(Parcel in) {
            return new Auction(in);
        }

        @Override
        public Auction[] newArray(int size) {
            return new Auction[size];
        }
    };

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getListingUrl() {
        return listingUrl;
    }

    public void setListingUrl(String listingUrl) {
        this.listingUrl = listingUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Money getShippingCost() {
        return shippingCost;
    }

    public void setShippingCost(Money shippingCost) {
        this.shippingCost = shippingCost;
    }

    public Money getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(Money currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getAuctionSource() {
        return auctionSource;
    }

    public void setAuctionSource(String auctionSource) {
        this.auctionSource = auctionSource;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public boolean isAuction() {
        return auction;
    }

    public void setAuction(boolean auction) {
        this.auction = auction;
    }

    public boolean isBuyItNow() {
        return buyItNow;
    }

    public void setBuyItNow(boolean buyItNow) {
        this.buyItNow = buyItNow;
    }
}
