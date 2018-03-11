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
 * Copyright (C) 2018
 *
 */

package com.balch.auctionbrowser

import android.arch.lifecycle.ViewModelProviders
import com.balch.auctionbrowser.auction.AuctionView
import com.balch.auctionbrowser.dagger.ActivityScope
import dagger.Module
import dagger.Provides



@Module
class AuctionModule {

    @Provides
    @ActivityScope
    internal fun bindsActivityBridge(activity: MainActivity): AuctionPresenter.ActivityBridge {
        return activity
    }

    @Provides
    @ActivityScope
    internal fun providesAuctionView(activity: MainActivity): AuctionView {
        return AuctionView(activity)
    }

    @Provides
    @ActivityScope
    internal fun providesAuctionViewModel(activity: MainActivity, factory: AuctionViewModelFactory): AuctionViewModel {
        return ViewModelProviders.of(activity, factory).get(AuctionViewModel::class.java)
    }

}
