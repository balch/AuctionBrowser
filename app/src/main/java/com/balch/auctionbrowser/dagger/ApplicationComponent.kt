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

package com.balch.auctionbrowser.dagger

import com.balch.auctionbrowser.AuctionApplication
import com.balch.auctionbrowser.auction.AuctionDataSource
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidSupportInjectionModule::class,
    NetworkModule::class,
    ActivityBindingModule::class,
    ApplicationModule::class])
interface ApplicationComponent : AndroidInjector<AuctionApplication> {

    fun inject(auctionDataSource: AuctionDataSource)

    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<AuctionApplication>()

}
