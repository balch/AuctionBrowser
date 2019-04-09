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

package com.balch.auctionbrowser.auction;

import com.balch.auctionbrowser.MainActivity;
import com.balch.auctionbrowser.dagger.ActivityScope;

import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class AuctionModule {

    @Binds
    @ActivityScope
    abstract LifecycleOwner bindsLifecycleOwner(MainActivity activity);

    @Provides
    @ActivityScope
    static FragmentManager providesFragmentManager(MainActivity activity) {
        return activity.getSupportFragmentManager();
    }

    @Provides
    @ActivityScope
    static AuctionView providesAuctionView(MainActivity activity) {
        return new AuctionView(activity);
    }

    @Provides
    @ActivityScope
    static AuctionViewModel providesAuctionViewModel(MainActivity activity,
                                                     AuctionViewModelFactory factory) {
        return ViewModelProviders.of(activity, factory).get(AuctionViewModel.class);
    }


}
