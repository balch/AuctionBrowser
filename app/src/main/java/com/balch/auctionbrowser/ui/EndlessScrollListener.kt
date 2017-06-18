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

package com.balch.auctionbrowser.ui

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class EndlessScrollListener(private val linearLayoutManager: LinearLayoutManager,
                            private val triggerPosition:Int = 10):
        RecyclerView.OnScrollListener() {

    val onLoadMore: Observable<Unit>
        get() = loadMoreSubject

    private val loadMoreSubject: PublishSubject<Unit> = PublishSubject.create<Unit>()

    private var hasMore = true
    private var loading = false

    fun reset() {
        hasMore = true
        loading = false
    }

    fun doneLoading(hasMore: Boolean) {
        this.hasMore = hasMore
        loading = false
    }

    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        if (hasMore && !loading) {
            val visibleItemCount = linearLayoutManager.childCount
            val totalItemCount = linearLayoutManager.itemCount
            val firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition()

            if (visibleItemCount + firstVisibleItem >= (totalItemCount - triggerPosition)) {
                loading = true
                loadMoreSubject.onNext(Unit)
            }
        }
    }
}

