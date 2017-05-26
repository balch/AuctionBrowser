package com.balch.auctionbrowser.auction.model;

import com.balch.android.app.framework.types.ISO8601DateTime;
import com.balch.android.app.framework.types.Money;
import com.balch.auctionbrowser.AuctionData;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AuctionDataTypeAdapter implements JsonDeserializer<AuctionData> {
    @Override
    public AuctionData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject items = json.getAsJsonObject()
                .getAsJsonArray("findItemsByKeywordsResponse").get(0)
                .getAsJsonObject();
        boolean success = validateResponse(items);

        AuctionData auctionData = new AuctionData();
        if (success) {
            auctionData.setTotalPages(getTotalPages(items));
            auctionData.setAuctions(parseAuctions(items));
        } else {
        }

        return auctionData;
    }

    private boolean validateResponse(JsonObject json) {
        return ((json != null) &&
                json.getAsJsonArray("ack").get(0).getAsString().equals("Success"));
    }

    private int getTotalPages(JsonObject json) {
        JsonPrimitive totalPages = json
                .getAsJsonArray("paginationOutput").get(0).getAsJsonObject()
                .getAsJsonArray("totalPages").get(0).getAsJsonPrimitive();
        return totalPages.getAsInt();

    }

    private List<Auction> parseAuctions(JsonObject json) throws JsonParseException {
        List<Auction> auctions = new ArrayList<>();
        JsonArray items = json
                .getAsJsonArray("searchResult").get(0).getAsJsonObject()
                .getAsJsonArray("item");
        for(JsonElement element : items){
            auctions.add(parseAuction(element.getAsJsonObject()));
        }
        return auctions;
    }

    private Auction parseAuction(JsonObject item) throws JsonParseException {

        JsonArray jsonViewItemURL = item.getAsJsonArray("viewItemURL");
        JsonArray jsonGalleryURL = item.getAsJsonArray("galleryURL");
        JsonArray jsonLocaton = item.getAsJsonArray("location");
        JsonObject sellingStatus = item.getAsJsonArray("sellingStatus").get(0).getAsJsonObject();
        double convertedCurrentPrice = sellingStatus
                .getAsJsonArray("convertedCurrentPrice").get(0).getAsJsonObject()
                .getAsJsonPrimitive("__value__").getAsDouble();

        Money shippingCost = new Money();
        JsonArray shippingInfo = item.getAsJsonArray("shippingInfo");
        if (shippingInfo != null) {
            JsonArray shippingServiceCost = shippingInfo.get(0).getAsJsonObject()
                    .getAsJsonArray("shippingServiceCost");
            if (shippingServiceCost != null) {
                double cost = shippingServiceCost.get(0).getAsJsonObject()
                        .getAsJsonPrimitive("__value__").getAsDouble();
                shippingCost = new Money(cost);
            }
        }

        Boolean isAuction = false;
        Boolean isButItNow = false;
        Date startTime = new Date();
        Date endTime = new Date();
        JsonArray listingInfoWrapper = item.getAsJsonArray("listingInfo");
        if (listingInfoWrapper != null) {
            JsonObject listingInfo = listingInfoWrapper.get(0).getAsJsonObject();
            String listingType = listingInfo.getAsJsonArray("listingType").get(0).getAsString();

            if ("Auction".equals(listingType)) {
                isAuction = true;
                isButItNow = listingInfo.getAsJsonArray("buyItNowAvailable").get(0).getAsBoolean();
            } else {
                isAuction = false;
                isButItNow = true;
            }

            try {
                startTime = ISO8601DateTime.toDate(listingInfo.getAsJsonArray("startTime").get(0).getAsString());
                endTime = ISO8601DateTime.toDate(listingInfo.getAsJsonArray("endTime").get(0).getAsString());
            } catch (ParseException ex) {
                throw new JsonParseException(ex);
            }
        }

        Auction auction = new Auction(
            item.getAsJsonArray("itemId").get(0).getAsLong(),
            item.getAsJsonArray("title").get(0).getAsString(),
            (jsonViewItemURL != null) ? jsonViewItemURL.get(0).getAsString() : "",
            (jsonGalleryURL != null) ? jsonGalleryURL.get(0).getAsString() : "",
            (jsonLocaton != null) ? jsonLocaton.get(0).getAsString() : "",
            shippingCost,
            new Money(convertedCurrentPrice),
            "",
            startTime,
            endTime,
            isAuction,
            isButItNow);

        return auction;
    }

}
