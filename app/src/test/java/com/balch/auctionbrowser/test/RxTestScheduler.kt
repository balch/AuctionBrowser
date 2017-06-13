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


package com.balch.auctionbrowser.test

import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.TestScheduler

open class RxTestScheduler {
    internal val testScheduler = TestScheduler()

    init {
        RxJavaPlugins.setInitIoSchedulerHandler { _ -> testScheduler }
        RxJavaPlugins.setIoSchedulerHandler { _ -> testScheduler }
        RxJavaPlugins.setInitNewThreadSchedulerHandler { _ -> testScheduler }
        RxJavaPlugins.setNewThreadSchedulerHandler { _ -> testScheduler }
        RxJavaPlugins.setInitSingleSchedulerHandler { _ -> testScheduler }
        RxJavaPlugins.setSingleSchedulerHandler { _ -> testScheduler }
        RxJavaPlugins.setInitComputationSchedulerHandler { _ -> testScheduler }
        RxJavaPlugins.setComputationSchedulerHandler { _ -> testScheduler }
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { _ -> testScheduler }
        RxAndroidPlugins.setMainThreadSchedulerHandler { _ -> testScheduler }
    }

    fun cleanup() {
        RxJavaPlugins.reset()
        RxAndroidPlugins.reset()
    }
}

