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

import android.os.Bundle
import android.support.annotation.VisibleForTesting
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.balch.auctionbrowser.ext.logTiming
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

/**
 * This class enhances the Activity functionality by providing View/Model creation abstraction
 * and error handling.

 * @param <V> Type of View to create
</V> */
abstract class PresenterActivity<V: View> : AppCompatActivity()  {

    lateinit protected var view: V
    lateinit protected var modelProvider: ModelProvider

    open protected val mainThread: Scheduler
        get() = AndroidSchedulers.mainThread()

    open protected val ioThread: Scheduler
        get() = Schedulers.io()

    /**
     * Override abstract method to create a view of type V used by the Presenter.
     * The view id will be managed by this class if not specified
     * @return View containing view logic in the MVP pattern
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    abstract fun createView(): V

    /**
     * Override abstract method to create any models needed by the Presenter. AuctionModelProvider
     * is injected into this method to take advantage the Dependency Injection pattern.

     * @param modelProvider injected ModelProvider
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    abstract fun createModel(modelProvider: ModelProvider)

    @VisibleForTesting
    fun createModelInternal(modelProvider: ModelProvider) {
        this.modelProvider = modelProvider
        createModel(modelProvider)
    }

    open fun onHandleException(logMsg: String, ex: Exception): Boolean {
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        view = this.createView()
        setContentView(view)

        createModelInternal(application as ModelProvider)
    }

    /**
     * add timing logging and exception handling around the passed in body
     */
    protected fun wrap(tag: String, body: () -> Unit) {

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
