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

package com.balch.auctionbrowser

import android.os.StrictMode
import com.balch.auctionbrowser.dagger.DaggerTestApplicationComponent
import com.balch.auctionbrowser.dagger.TestApplicationComponent
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication


open class TestAuctionApplication : AuctionApplication() {

    lateinit var component: TestApplicationComponent

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        component = DaggerTestApplicationComponent.builder().create(this) as TestApplicationComponent
        return component
    }

    override fun setStrictMode() {
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                .permitAll()
                .build())
    }
}
