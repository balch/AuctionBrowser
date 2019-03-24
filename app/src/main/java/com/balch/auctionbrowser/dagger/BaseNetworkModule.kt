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
 * Copyright (C) 2019
 *
 */

package com.balch.auctionbrowser.dagger

import com.balch.auctionbrowser.BuildConfig
import com.balch.auctionbrowser.auction.model.AuctionData
import com.balch.auctionbrowser.auction.model.AuctionDataTypeAdapter
import com.balch.auctionbrowser.auction.model.EBayApi
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
open class BaseNetworkModule {

    companion object {
        const val EBAY_URL = "ebay_url"
    }

    @Provides
    @Singleton
    internal fun providesOkHttpInterceptors(): MutableList<Interceptor> {

        val interceptors = mutableListOf<Interceptor>()
        if (BuildConfig.DEBUG) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            interceptors.add(interceptor)
        }
        return interceptors
    }

    @Provides
    @Singleton
    internal fun providesGson(): Gson {
        return GsonBuilder()
                .registerTypeAdapter(AuctionData::class.java, AuctionDataTypeAdapter())
                .create()
    }

    @Provides
    @Singleton
    internal fun providesOkHttpClient(interceptors: MutableList<Interceptor>): OkHttpClient {
        val builder = OkHttpClient.Builder()
        interceptors.forEach { builder.addInterceptor(it) }
        return builder.build()
    }

    @Provides
    @Singleton
    internal open fun providesEbayApi(@Named(EBAY_URL) baseUrl: String,
                                 okHttpClient: OkHttpClient,
                                 gson: Gson): EBayApi {
        return getRetrofitService(baseUrl, okHttpClient, gson).create(EBayApi::class.java)
    }

    private fun getRetrofitService(baseUrl: String,
                                   okHttpClient: OkHttpClient,
                                   gson: Gson): Retrofit {

        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .build()
    }

}