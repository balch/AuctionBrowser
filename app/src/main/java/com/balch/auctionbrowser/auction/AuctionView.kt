/*
 * Author: Balch
 * Created: 9/4/14 12:26 AM
 *
 * This file is part of MockTrade.
 *
 * MockTrade is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MockTrade is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MockTrade.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2014
 */

package com.balch.auctionbrowser.auction

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar

import com.balch.android.app.framework.BaseView
import com.balch.auctionbrowser.R
import com.balch.auctionbrowser.auction.commons.inflate
import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.note.Note

import kotlinx.android.synthetic.main.auction_view.view.*

class AuctionView : FrameLayout, BaseView {

    interface AuctionViewListener {
        fun onLoadMore(page: Int): Boolean
    }

    private val progressBar: ProgressBar by lazy { auction_view_progress_bar }
    private val recyclerView: RecyclerView by lazy { action_view_recycler }

    lateinit private var recyclerOnScrollListener: RecyclerOnScrollListener
    private var auctionAdapter: AuctionAdapter? = null

    var auctionViewListener: AuctionViewListener? = null

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

        val layoutManager = LinearLayoutManager(context)
        recyclerOnScrollListener = RecyclerOnScrollListener(layoutManager,
                object : RecyclerOnScrollListener.LoadMoreListener {
                    override fun onLoadMore(page: Int): Boolean {
                        return auctionViewListener!!.onLoadMore(page)
                    }
                })


        recyclerView.layoutManager = layoutManager
        recyclerView.addOnScrollListener(recyclerOnScrollListener)
    }

    fun setAuctionAdapter(auctionAdapter: AuctionAdapter) {
        this.auctionAdapter = auctionAdapter
        recyclerView.adapter = this.auctionAdapter
    }

    fun showBusy() {
        this.progressBar.visibility = View.VISIBLE
    }

    fun hideBusy() {
        this.progressBar.visibility = View.GONE
    }

    fun addAuctions(auctions: List<Auction>, notes: Map<Long, Note>) {
        this.auctionAdapter!!.addAuctions(auctions, notes)
    }

    fun clearAuctions() {
        this.auctionAdapter!!.clearAuctions()
        this.recyclerOnScrollListener.reset()
    }

    fun doneLoading() {
        this.recyclerOnScrollListener.doneLoading()
    }

    fun getNote(auction: Auction): Note? {
        return this.auctionAdapter!!.notes[auction.itemId]
    }

    fun clearNote(auction: Auction) {
        this.auctionAdapter!!.notes.remove(auction.itemId)
        this.auctionAdapter!!.notifyDataSetChanged()
    }

    fun addNote(auction: Auction, note: Note) {
        this.auctionAdapter!!.notes.put(auction.itemId, note)
        this.auctionAdapter!!.notifyDataSetChanged()
    }

    class RecyclerOnScrollListener internal constructor(private val linearLayoutManager: LinearLayoutManager,
                                                        private val loadMoreListener: LoadMoreListener) : RecyclerView.OnScrollListener() {

        private var currentPage = 1
        private var loading = false
        private var hasMore = true

        internal fun reset() {
            currentPage = 1
            hasMore = true
            loading = false
        }

        internal fun doneLoading() {
            loading = false
        }

        override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            if (hasMore && !loading) {
                val visibleItemCount = linearLayoutManager.childCount
                val totalItemCount = linearLayoutManager.itemCount
                val firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition()

                if (visibleItemCount + firstVisibleItem >= totalItemCount) {
                    hasMore = loadMoreListener.onLoadMore(++currentPage)
                    loading = hasMore
                }
            }
        }

        interface LoadMoreListener {
            fun onLoadMore(page: Int): Boolean
        }
    }
}
