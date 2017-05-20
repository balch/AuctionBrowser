package com.balch.auctionbrowser;

import com.balch.auctionbrowser.auction.model.AuctionDataTypeAdapter;
import com.balch.auctionbrowser.auction.model.EbayApi;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ModelApiFactory {

    private static final String EBAY_URL_BASE = "http://svcs.ebay.com/";

    private EbayApi ebayApi = null;

    private final static Gson gson = new GsonBuilder()
            .registerTypeAdapter(AuctionData.class, new AuctionDataTypeAdapter())
            .create();

    @SuppressWarnings("unchecked")
    public <T> T getModelApi(Class<T> api) {
        if (api == EbayApi.class) {
            if (ebayApi == null) {
                ebayApi = getRetrofitService(EBAY_URL_BASE).create(EbayApi.class);
            }
            return (T)ebayApi;
        }

        return null;
    }

    private static Retrofit getRetrofitService(String baseUrl) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.BODY
                : HttpLoggingInterceptor.Level.NONE);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build();
    }


}
