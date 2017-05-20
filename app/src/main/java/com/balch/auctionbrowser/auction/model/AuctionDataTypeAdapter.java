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

        Auction auction = new Auction();
        auction.setItemId(item.getAsJsonArray("itemId").get(0).getAsLong());
        auction.setTitle(item.getAsJsonArray("title").get(0).getAsString());

        JsonArray jsonArray = item.getAsJsonArray("viewItemURL");
        auction.setListingUrl((jsonArray != null) ? jsonArray.get(0).getAsString() : "");

        jsonArray = item.getAsJsonArray("galleryURL");
        auction.setImageUrl((jsonArray != null) ? jsonArray.get(0).getAsString() : "");

        jsonArray = item.getAsJsonArray("location");
        auction.setLocation((jsonArray != null) ? jsonArray.get(0).getAsString() : "");

        JsonObject sellingStatus = item.getAsJsonArray("sellingStatus").get(0).getAsJsonObject();
        double convertedCurrentPrice = sellingStatus
                .getAsJsonArray("convertedCurrentPrice").get(0).getAsJsonObject()
                .getAsJsonPrimitive("__value__").getAsDouble();
        auction.setCurrentPrice(new Money(convertedCurrentPrice));

        JsonArray shippingInfo = item.getAsJsonArray("shippingInfo");
        if (shippingInfo != null) {
            JsonArray shippingServiceCost = shippingInfo.get(0).getAsJsonObject()
                    .getAsJsonArray("shippingServiceCost");
            if (shippingServiceCost != null) {
                double cost = shippingServiceCost.get(0).getAsJsonObject()
                        .getAsJsonPrimitive("__value__").getAsDouble();
                auction.setShippingCost(new Money(cost));
            }
        }

        JsonArray listingInfoWrapper = item.getAsJsonArray("listingInfo");
        if (listingInfoWrapper != null) {
            JsonObject listingInfo = listingInfoWrapper.get(0).getAsJsonObject();
            String listingType = listingInfo.getAsJsonArray("listingType").get(0).getAsString();

            if ("Auction".equals(listingType)) {
                auction.setAuction(true);
                auction.setBuyItNow(listingInfo.getAsJsonArray("buyItNowAvailable").get(0).getAsBoolean());
            } else {
                auction.setAuction(false);
                auction.setBuyItNow(true);
            }

            try {
                auction.setStartTime(ISO8601DateTime.toDate(listingInfo.getAsJsonArray("startTime").get(0).getAsString()));
                auction.setEndTime(ISO8601DateTime.toDate(listingInfo.getAsJsonArray("endTime").get(0).getAsString()));
            } catch (ParseException ex) {
                throw new JsonParseException(ex);
            }
        }

        return auction;
    }

}
