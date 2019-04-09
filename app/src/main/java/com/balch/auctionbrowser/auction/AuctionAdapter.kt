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

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.balch.auctionbrowser.R
import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.base.NetworkState
import com.balch.auctionbrowser.dagger.ActivityScope
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

@ActivityScope
class AuctionAdapter @Inject constructor() :
        PagedListAdapter<Auction, RecyclerView.ViewHolder>(diffCallback) {

    // public properties
    val onClickAuction: Observable<Auction>
        get() = clickAuctionSubject

    val onClickNote: Observable<Auction>
        get() = clickNoteSubject

    // backing for exposing user initiated events to Activity
    private val clickAuctionSubject: PublishSubject<Auction> = PublishSubject.create<Auction>()
    private val clickNoteSubject: PublishSubject<Auction> = PublishSubject.create<Auction>()

    private var networkState: NetworkState? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            R.layout.item_auction_list -> AuctionViewHolder(parent, clickAuctionSubject, clickNoteSubject)
            R.layout.network_state_item -> NetworkStateItemViewHolder.create(parent)
            else -> throw IllegalArgumentException("unknown view type $viewType")
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.item_auction_list -> (holder as AuctionViewHolder).bind(getItem(position)!!)
            R.layout.network_state_item -> (holder as NetworkStateItemViewHolder).bind(networkState)
        }
    }

    private fun hasExtraRow() = networkState != null && networkState != NetworkState.LOADED

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1) {
            R.layout.network_state_item
        } else {
            R.layout.item_auction_list
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasExtraRow()) 1 else 0
    }

    fun setNetworkState(newNetworkState: NetworkState?) {
        val previousState = this.networkState
        val hadExtraRow = hasExtraRow()
        this.networkState = newNetworkState
        val hasExtraRow = hasExtraRow()
        if (hadExtraRow != hasExtraRow) {
            if (hadExtraRow) {
                notifyItemRemoved(super.getItemCount())
            } else {
                notifyItemInserted(super.getItemCount())
            }
        } else if (hasExtraRow && previousState != newNetworkState) {
            notifyItemChanged(itemCount - 1)
        }
    }
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Auction>() {
            override fun areItemsTheSame(oldItem: Auction, newItem: Auction): Boolean =
                    oldItem.itemId == newItem.itemId

            override fun areContentsTheSame(oldItem: Auction, newItem: Auction): Boolean =
                    oldItem == newItem
        }
    }

}
