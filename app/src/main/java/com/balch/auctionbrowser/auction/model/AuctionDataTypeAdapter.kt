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
import com.google.gson.*
import org.joda.time.DateTime
import java.lang.reflect.Type
import java.text.ParseException
import java.util.*

/**
 * Retrofit parser for the EBayApi.
 */
class AuctionDataTypeAdapter : JsonDeserializer<AuctionData> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): AuctionData {

        val items = json.asJsonObject
                .getAsJsonArray("findItemsByKeywordsResponse").get(0)
                .asJsonObject
        val success = validateResponse(items)

        return if (success)
            AuctionData().apply {
                totalPages = getTotalPages(items)
                auctions = parseAuctions(items)
            }
        else
            AuctionData().apply {
                hasError = true
            }
    }

    private fun validateResponse(json: JsonObject?): Boolean {
        return json != null && json.getAsJsonArray("ack").get(0).asString == "Success"
    }

    private fun getTotalPages(json: JsonObject): Int {
        val totalPages = json
                .getAsJsonArray("paginationOutput").get(0).asJsonObject
                .getAsJsonArray("totalPages").get(0).asJsonPrimitive
        return totalPages.asInt
    }

    @Throws(JsonParseException::class)
    private fun parseAuctions(json: JsonObject): List<Auction> {
        val items = json
                .getAsJsonArray("searchResult").get(0).asJsonObject
                .getAsJsonArray("item")
        val auctions = items.mapTo(ArrayList<Auction>()) { parseAuction(it.asJsonObject) }
        return auctions
    }

    @Throws(JsonParseException::class)
    private fun parseAuction(item: JsonObject): Auction {

        val jsonViewItemURL = item.getAsJsonArray("viewItemURL")
        val jsonGalleryURL = item.getAsJsonArray("galleryURL")
        val jsonLocaton = item.getAsJsonArray("location")
        val sellingStatus = item.getAsJsonArray("sellingStatus").get(0).asJsonObject
        val convertedCurrentPrice = sellingStatus
                .getAsJsonArray("convertedCurrentPrice").get(0).asJsonObject
                .getAsJsonPrimitive("__value__").asDouble

        var shippingCost = Money()
        val shippingInfo = item.getAsJsonArray("shippingInfo")
        if (shippingInfo != null) {
            val shippingServiceCost = shippingInfo.get(0).asJsonObject
                    .getAsJsonArray("shippingServiceCost")
            if (shippingServiceCost != null) {
                val cost = shippingServiceCost.get(0).asJsonObject
                        .getAsJsonPrimitive("__value__").asDouble
                shippingCost = Money(cost)
            }
        }

        var isAuction: Boolean = false
        var isButItNow: Boolean = false
        var startTime = DateTime()
        var endTime = DateTime()
        val listingInfoWrapper = item.getAsJsonArray("listingInfo")
        if (listingInfoWrapper != null) {
            val listingInfo = listingInfoWrapper.get(0).asJsonObject
            val listingType = listingInfo.getAsJsonArray("listingType").get(0).asString

            if ("Auction" == listingType) {
                isAuction = true
                isButItNow = listingInfo.getAsJsonArray("buyItNowAvailable").get(0).asBoolean
            } else {
                isAuction = false
                isButItNow = true
            }

            try {
                startTime = DateTime.parse(listingInfo.getAsJsonArray("startTime").get(0).asString)
                endTime = DateTime.parse(listingInfo.getAsJsonArray("endTime").get(0).asString)
            } catch (ex: ParseException) {
                throw JsonParseException(ex)
            }

        }

        val auction = Auction(
                item.getAsJsonArray("itemId").get(0).asLong,
                item.getAsJsonArray("title").get(0).asString,
                if (jsonGalleryURL != null) jsonGalleryURL.get(0).asString else "",
                if (jsonViewItemURL != null) jsonViewItemURL.get(0).asString else "",
                if (jsonLocaton != null) jsonLocaton.get(0).asString else "",
                shippingCost,
                Money(convertedCurrentPrice),
                "",
                startTime,
                endTime,
                isAuction,
                isButItNow)

        return auction
    }

}
