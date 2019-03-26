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

import com.balch.auctionbrowser.TestAuctionApplication
import dagger.Module
import dagger.Provides
import okhttp3.mockwebserver.MockWebServer
import javax.inject.Named
import javax.inject.Singleton

@Module
open class TestNetworkModule : BaseNetworkModule() {

    companion object {
        private const val EBAY_URL = "ebay_url"
    }

    @Provides
    @Singleton
    internal fun providesMockWebServer(app: TestAuctionApplication): MockWebServer {
        val mockWebServer = MockWebServer()
        mockWebServer.start()
        return mockWebServer
    }

    @Provides
    @Singleton
    @Named(EBAY_URL)
    internal fun providesEbayApiUrl(mockWebServer: MockWebServer): String {
        return mockWebServer.url("").toString()
    }

}