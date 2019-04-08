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

import android.os.StrictMode
import com.balch.auctionbrowser.dagger.ApplicationComponent
import com.balch.auctionbrowser.dagger.DaggerApplicationComponent
import dagger.android.AndroidInjector
import dagger.android.support.DaggerApplication
import net.danlew.android.joda.JodaTimeAndroid
import timber.log.Timber
import timber.log.Timber.DebugTree


open class AuctionApplication : DaggerApplication() {

    lateinit var componet: ApplicationComponent

    companion object {
        const val DATABASE_NAME = "auction_browser.db"
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return (DaggerApplicationComponent.builder().create(this) as ApplicationComponent)
                .also { componet = it }
    }

    override fun onCreate() {
        super.onCreate()

        Timber.plant(DebugTree())

        setStrictMode()

        JodaTimeAndroid.init(this)
    }

    open fun setStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build())
        }
    }
}
