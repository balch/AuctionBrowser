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
import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.dagger.ActivityScope
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

@ActivityScope
class AuctionAdapter @Inject constructor() :
        PagedListAdapter<Auction, AuctionViewHolder>(diffCallback) {

    // public properties
    val onClickAuction: Observable<Auction>
        get() = clickAuctionSubject

    val onClickNote: Observable<Auction>
        get() = clickNoteSubject

    // backing for exposing user initiated events to Activity
    private val clickAuctionSubject: PublishSubject<Auction> = PublishSubject.create<Auction>()
    private val clickNoteSubject: PublishSubject<Auction> = PublishSubject.create<Auction>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuctionViewHolder {
        return AuctionViewHolder(parent, clickAuctionSubject, clickNoteSubject)
    }

    override fun onBindViewHolder(holder: AuctionViewHolder, position: Int) {
        val auction = getItem(position)!!
        holder.bind(auction)
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
