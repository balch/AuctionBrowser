package com.balch.auctionbrowser

import com.balch.auctionbrowser.auction.model.AuctionDataTypeAdapter
import com.balch.auctionbrowser.auction.model.EBayApi
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class ModelApiFactory {

    companion object {

        private val EBAY_URL_BASE = "http://svcs.ebay.com/"

        private val gson = GsonBuilder()
                .registerTypeAdapter(AuctionData::class.java, AuctionDataTypeAdapter())
                .create()
    }

    private val ebayApi: EBayApi by lazy {getRetrofitService(EBAY_URL_BASE).create(EBayApi::class.java)}

    @Suppress("UNCHECKED_CAST")
    fun <T> getModelApi(api: Class<T>): T? {
        if (api == EBayApi::class.java) {
            return ebayApi as T
        }

        return null
    }

    private fun getRetrofitService(baseUrl: String): Retrofit {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = if (BuildConfig.DEBUG)
            HttpLoggingInterceptor.Level.BODY
        else
            HttpLoggingInterceptor.Level.NONE
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .build()
    }
}
