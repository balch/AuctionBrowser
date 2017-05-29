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

    private val EBAY_URL_BASE = "http://svcs.ebay.com/"
    private val ebayApi: EBayApi by lazy { getRetrofitService(EBAY_URL_BASE).create(EBayApi::class.java) }

    private val gson by lazy {
        GsonBuilder()
                .registerTypeAdapter(AuctionData::class.java, AuctionDataTypeAdapter())
                .create()
    }

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
