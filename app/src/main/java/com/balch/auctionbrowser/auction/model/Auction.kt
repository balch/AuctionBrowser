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

import com.balch.auctionbrowser.types.Money
import java.io.Serializable
import java.util.*

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
                   var isBuyItNow: Boolean) : Serializable
