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

package com.balch.auctionbrowser.base

import android.support.annotation.VisibleForTesting

abstract class BasePresenter<out V: BaseView>(private var viewInternal: V?) {

    protected val view: V
        get() = viewInternal!!

    lateinit protected var modelProvider: ModelProvider

    /**
     * Override abstract method to create any models needed by the Presenter. AuctionModelProvider
     * is injected into this method to take advantage the Dependency Injection pattern.

     * @param modelProvider injected ModelProvider
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    abstract fun createModel(modelProvider: ModelProvider)

    internal fun createModelInternal(modelProvider: ModelProvider) {
        this.modelProvider = modelProvider
        createModel(modelProvider)
    }

    open fun cleanup() {
        viewInternal?.cleanup()
        viewInternal = null
    }
}