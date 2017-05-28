package com.balch.auctionbrowser.auction.model

import com.balch.android.app.framework.types.ISO8601DateTime
import com.balch.android.app.framework.types.Money
import com.balch.auctionbrowser.AuctionData
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive

import java.lang.reflect.Type
import java.text.ParseException
import java.util.ArrayList
import java.util.Date

class AuctionDataTypeAdapter : JsonDeserializer<AuctionData> {
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): AuctionData {

        val items = json.asJsonObject
                .getAsJsonArray("findItemsByKeywordsResponse").get(0)
                .asJsonObject
        val success = validateResponse(items)

        val auctionData = AuctionData()
        if (success) {
            auctionData.totalPages = getTotalPages(items)
            auctionData.auctions = parseAuctions(items)
        }

        return auctionData
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
        var startTime = Date()
        var endTime = Date()
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
                startTime = ISO8601DateTime.toDate(listingInfo.getAsJsonArray("startTime").get(0).asString)
                endTime = ISO8601DateTime.toDate(listingInfo.getAsJsonArray("endTime").get(0).asString)
            } catch (ex: ParseException) {
                throw JsonParseException(ex)
            }

        }

        val auction = Auction(
                item.getAsJsonArray("itemId").get(0).asLong,
                item.getAsJsonArray("title").get(0).asString,
                if (jsonViewItemURL != null) jsonViewItemURL.get(0).asString else "",
                if (jsonGalleryURL != null) jsonGalleryURL.get(0).asString else "",
                if (jsonLocaton != null) jsonLocaton.get(0).asString else "",
                shippingCost,
                Money(convertedCurrentPrice),
                "",
                startTime,
                endTime,
                isAuction!!,
                isButItNow!!)

        return auction
    }

}
