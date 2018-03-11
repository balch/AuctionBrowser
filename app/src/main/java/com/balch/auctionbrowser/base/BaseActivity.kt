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

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.balch.auctionbrowser.ext.logTiming
import dagger.android.AndroidInjection
import timber.log.Timber
import javax.inject.Inject

/**
 * This class enhances the Activity functionality by providing View/Model creation abstraction
 * and error handling.

 * @param <V> Type of View to create
</V> */
abstract class BaseActivity<P: BasePresenter> : AppCompatActivity() {

    @Inject
    lateinit var presenter: P

    open fun onHandleException(logMsg: String, ex: Exception): Boolean {
        return false
    }

    @SuppressLint("VisibleForTests")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AndroidInjection.inject(this)

        presenter.initialize(savedInstanceState)
        lifecycle.addObserver(presenter)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(presenter)
    }

    /**
     * add timing logging and exception handling around the passed in body
     */
    protected fun log(tag: String, body: () -> Unit) {

        try {
            logTiming(tag) { body() }
        } catch (ex: Exception) {
            if (!handleException("$tag ", ex)) {
                throw ex
            }
        }
    }

    private fun handleException(logMsg: String, ex: Exception): Boolean {
        Timber.e(ex, logMsg)
        return onHandleException(logMsg, ex)
    }

    fun getSnackbar(parent: View, msg: String, length: Int): Snackbar {
        return Snackbar.make(parent, msg, length)
    }
}
