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
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.auction_view.view.*

class AuctionView : FrameLayout, BaseView {

    private val progressBar: ProgressBar by lazy { auction_view_progress_bar }
    private val recyclerView: RecyclerView by lazy { action_view_recycler }

    lateinit private var recyclerOnScrollListener: RecyclerOnScrollListener

    lateinit private var auctionAdapter: AuctionAdapter

    val onLoadMore: Observable<Int>
        get() = loadMoreSubject

    // backing for exposing user initiated events to Activity
    private val loadMoreSubject: PublishSubject<Int> = PublishSubject.create<Int>()

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
        recyclerOnScrollListener = RecyclerOnScrollListener(layoutManager, loadMoreSubject)

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

    fun showBusy() {
        progressBar.visibility = View.VISIBLE
    }

    fun hideBusy() {
        progressBar.visibility = View.GONE
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

    class RecyclerOnScrollListener internal constructor(private val linearLayoutManager: LinearLayoutManager,
                    private val loadMoreSubject: PublishSubject<Int>): RecyclerView.OnScrollListener() {

        var hasMore = true

        private var currentPage = 1
        private var loading = false

        internal fun reset() {
            currentPage = 1
            hasMore = true
            loading = false
        }

        internal fun doneLoading(hasMore: Boolean) {
            this.hasMore = hasMore
            loading = false
        }

        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            if (hasMore && !loading) {
                val visibleItemCount = linearLayoutManager.childCount
                val totalItemCount = linearLayoutManager.itemCount
                val firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition()

                if (visibleItemCount + firstVisibleItem >= totalItemCount) {
                    loadMoreSubject.onNext(++currentPage)
                    loading = true
                }
            }
        }
    }
}
