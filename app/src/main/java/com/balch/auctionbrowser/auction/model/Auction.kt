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

package com.balch.auctionbrowser.auction.model

import android.os.Parcel
import android.os.Parcelable

import com.balch.android.app.framework.domain.DomainObject
import com.balch.android.app.framework.types.Money

import java.io.Serializable
import java.util.Date

data class Auction(val itemId: Long,
                   val title: String,
                   val imageUrl: String,
                   val listingUrl: String,
                   val location: String,
                   val shippingCost: Money,
                   val currentPrice: Money,
                   val auctionSource: String,
                   val startTime: Date,
                   val endTime: Date,
                   var isAuction: Boolean,
                   var isBuyItNow: Boolean) : DomainObject(), Parcelable, Serializable {

    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Auction> = object : Parcelable.Creator<Auction> {
            override fun createFromParcel(source: Parcel): Auction = Auction(source)
            override fun newArray(size: Int): Array<Auction?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(
        source.readLong(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.readParcelable<Money>(Money::class.java.classLoader),
        source.readParcelable<Money>(Money::class.java.classLoader),
        source.readString(),
        source.readSerializable() as Date,
        source.readSerializable() as Date,
        1 == source.readInt(),
        1 == source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(itemId)
        dest.writeString(title)
        dest.writeString(imageUrl)
        dest.writeString(listingUrl)
        dest.writeString(location)
        dest.writeParcelable(shippingCost, flags)
        dest.writeParcelable(currentPrice, flags)
        dest.writeString(auctionSource)
        dest.writeSerializable(startTime)
        dest.writeSerializable(endTime)
        dest.writeInt((if (isAuction) 1 else 0))
        dest.writeInt((if (isBuyItNow) 1 else 0))
    }
}
