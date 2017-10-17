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

import android.annotation.SuppressLint
import android.app.SearchManager
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.VisibleForTesting
import android.support.design.widget.Snackbar
import android.support.v4.app.FragmentManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import com.balch.auctionbrowser.R.id.*
import com.balch.auctionbrowser.R.menu.options_menu
import com.balch.auctionbrowser.auction.AuctionView
import com.balch.auctionbrowser.auction.model.EBayModel
import com.balch.auctionbrowser.base.PresenterActivity

class MainActivity : PresenterActivity<AuctionView, AuctionPresenter>() {

    override fun createView(): AuctionView {
        return AuctionView(this)
    }

    private val auctionViewModel by lazy { ViewModelProviders.of(this).get(AuctionViewModel::class.java) }

    @SuppressLint("VisibleForTests")
    override fun createPresenter(view: AuctionView): AuctionPresenter {
        return AuctionPresenter(view, getString(R.string.ebay_app_id),
                object: AuctionPresenter.ActivityBridge {
                    override val isFinishing: Boolean
                        get() = this@MainActivity.isFinishing
                    override val fragmentManager: FragmentManager
                        get() = this@MainActivity.supportFragmentManager
                    override val auctionViewModel: AuctionViewModel
                        get() = this@MainActivity.auctionViewModel
                    override val lifecycleOwner: LifecycleOwner
                        get() = this@MainActivity

                    override fun showSnackBar(view: View, msg: Int) {
                        getSnackbar(view, getString(msg), Snackbar.LENGTH_LONG).show()
                    }
                })
    }

    @SuppressLint("VisibleForTests")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        wrap("onCreateInternal") {
            title = ""
            onCreateInternal(savedInstanceState)
        }
    }

    @VisibleForTesting
    fun onCreateInternal(savedInstanceState: Bundle?) {
        presenter.initialize(savedInstanceState)
        handleIntent()
    }

    override fun onNewIntent(intent: Intent) {
        wrap("OnNewIntent") {
            handleIntent(intent)
        }
    }

    @VisibleForTesting
    internal fun handleIntent(): Boolean {
        return handleIntent(intent)
    }

    private fun handleIntent(intent: Intent): Boolean {
        var handled = false
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            presenter.doSearch(query)

            handled = true
        }

        return handled
    }

    override fun onDestroy() {
        wrap("onDestroy") {
            presenter.cleanup()
        }
        super.onDestroy()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        wrap("onCreateOptionsMenu") {
            // Inflate the options menu from XML
            menuInflater.inflate(options_menu, menu)

            val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
            val searchView = menu.findItem(menu_search).actionView as SearchView
            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            searchView.setIconifiedByDefault(false)
            presenter.searchView = searchView

            menu.findItem(menu_sort_best_match).isChecked = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var handled = false
        wrap("onOptionsItemSelected") {
            // Handle item selection
            when (item.itemId) {
                menu_sort_best_match -> {
                    presenter.sortAuctions(EBayModel.SortColumn.BEST_MATCH)
                    item.isChecked = true
                    handled = true
                }
                menu_sort_ending_soonest -> {
                    presenter.sortAuctions(EBayModel.SortColumn.ENDING_SOONEST)
                    item.isChecked = true
                    handled = true
                }
                menu_sort_lowest_price -> {
                    presenter.sortAuctions(EBayModel.SortColumn.LOWEST_PRICE)
                    item.isChecked = true
                    handled = true
                }
            }
        }

        return handled || super.onOptionsItemSelected(item)
    }

}
