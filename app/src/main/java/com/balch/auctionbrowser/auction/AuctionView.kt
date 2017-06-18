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
import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.base.BaseView
import com.balch.auctionbrowser.ext.inflate
import com.balch.auctionbrowser.note.Note
import com.balch.auctionbrowser.ui.EndlessScrollListener
import io.reactivex.Observable
import kotlinx.android.synthetic.main.auction_view.view.*

class AuctionView : FrameLayout, BaseView {

    // public properties
    val onLoadMore: Observable<Unit>
        get() = recyclerOnScrollListener.onLoadMore

    var showBusy: Boolean
        get() = progressBar.visibility == View.VISIBLE
        set(value) { progressBar.visibility = if (value) View.VISIBLE else View.GONE }

    // private properties
    lateinit private var auctionAdapter: AuctionAdapter
    lateinit private var recyclerOnScrollListener: EndlessScrollListener

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
        inflate(R.layout.auction_view, true)

        id = View.generateViewId()

        val layoutManager = LinearLayoutManager(context)
        recyclerOnScrollListener = EndlessScrollListener(layoutManager)

        recyclerView.layoutManager = layoutManager
        recyclerView.addOnScrollListener(recyclerOnScrollListener)
    }

    fun setAuctionAdapter(auctionAdapter: AuctionAdapter) {
        this.auctionAdapter = auctionAdapter
        recyclerView.adapter = this.auctionAdapter
    }

    override fun cleanup() {
        recyclerView.clearOnScrollListeners()
        recyclerView.adapter = null
    }

    fun addAuctions(auctions: List<Auction>, notes: Map<Long, Note>) {
        auctionAdapter.addAuctions(auctions, notes)
    }

    fun clearAuctions() {
        auctionAdapter.clearAuctions()
        recyclerOnScrollListener.reset()
    }

    fun doneLoading(hasMore: Boolean) {
        recyclerOnScrollListener.doneLoading(hasMore)
    }

    fun getNote(auction: Auction): Note? {
        return auctionAdapter.notes[auction.itemId]
    }

    fun clearNote(auction: Auction) {
        auctionAdapter.notes.remove(auction.itemId)
        auctionAdapter.notifyDataSetChanged()
    }

    fun addNote(auction: Auction, note: Note) {
        auctionAdapter.notes.put(auction.itemId, note)
        auctionAdapter.notifyDataSetChanged()
    }
}
