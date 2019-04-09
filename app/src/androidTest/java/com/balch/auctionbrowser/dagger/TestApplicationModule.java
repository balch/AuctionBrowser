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

package com.balch.auctionbrowser.dagger;

import android.app.Application;

import com.balch.auctionbrowser.TestAuctionApplication;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import androidx.test.espresso.idling.concurrent.IdlingThreadPoolExecutor;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
abstract class TestApplicationModule extends BaseApplicationModule {
    @Binds
    @Singleton
    abstract Application bindsApplicationContext(TestAuctionApplication app);

    @Provides
    @Singleton
    static Executor providesNetworkExecutor() {
        return new IdlingThreadPoolExecutor("test", 5, 5, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), Thread::new);
    }


}
