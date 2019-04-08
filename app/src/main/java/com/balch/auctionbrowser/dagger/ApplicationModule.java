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

import com.balch.auctionbrowser.AuctionApplication;
import com.balch.auctionbrowser.auction.model.AuctionRepository;
import com.balch.auctionbrowser.auction.model.EBayRepository;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
abstract class ApplicationModule extends BaseApplicationModule {

    @Binds
    @Singleton
    abstract AuctionRepository bindsAuctionRepository(EBayRepository repository);

    @Binds
    @Singleton
    abstract Application providesApplicationContext(AuctionApplication app);

    @Provides
    @Singleton
    static Executor providesNetworkExecutor() {
        return Executors.newFixedThreadPool(5);
    }

}
