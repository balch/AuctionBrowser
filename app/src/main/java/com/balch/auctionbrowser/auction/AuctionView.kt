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

package com.balch.auctionbrowser.auction

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.balch.auctionbrowser.R
import com.balch.auctionbrowser.ext.inflate
import com.balch.auctionbrowser.ui.EndlessScrollListener
import io.reactivex.Observable
import kotlinx.android.synthetic.main.view_auction.view.*

class AuctionView : FrameLayout {

    // public properties
    val onLoadMore: Observable<Unit>
        get() = recyclerOnScrollListener.onLoadMore

    var showBusy: Boolean
        get() = progressBar.visibility == View.VISIBLE
        set(value) {
            progressBar.visibility = if (value) View.VISIBLE else View.GONE
        }

    // private properties
    private lateinit var recyclerOnScrollListener: EndlessScrollListener

    // private view layouts
    private val progressBar: ProgressBar by lazy { auction_view_progress_bar }
    private val recyclerView: RecyclerView by lazy { action_view_recycler }

    constructor(context: Context) : super(context) {
        initializeLayout()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initializeLayout()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        initializeLayout()
    }

    private fun initializeLayout() {
        inflate(R.layout.view_auction, true)

        id = View.generateViewId()

        val layoutManager = LinearLayoutManager(context)
        recyclerOnScrollListener = EndlessScrollListener(layoutManager)

        recyclerView.layoutManager = layoutManager
        recyclerView.addOnScrollListener(recyclerOnScrollListener)
    }

    fun setAuctionAdapter(auctionAdapter: AuctionAdapter) {
        recyclerView.adapter = auctionAdapter
    }

    fun cleanup() {
        recyclerView.clearOnScrollListeners()
        recyclerView.adapter = null
    }

    fun clearAuctions() {
        recyclerOnScrollListener.reset()
    }

    fun doneLoading(hasMore: Boolean) {
        recyclerOnScrollListener.doneLoading(hasMore)
    }
}
