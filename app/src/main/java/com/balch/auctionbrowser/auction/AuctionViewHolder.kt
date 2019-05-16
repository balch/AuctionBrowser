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

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.balch.auctionbrowser.R
import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.ext.inflate
import com.balch.auctionbrowser.ext.loadUrl
import com.balch.auctionbrowser.ext.toLongDateTimeString
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.item_auction_list.view.*

class AuctionViewHolder(parent: ViewGroup, private val onClickAuction: AuctionHandler?,
                        private val onClickNote: AuctionHandler?)
    : RecyclerView.ViewHolder(parent.inflate(R.layout.item_auction_list)) {

    fun bind(auction: Auction) {

        with(auction) {
            itemView.list_item_auction_img.loadUrl(imageUrl) { it.apply(RequestOptions.centerCropTransform()) }
            itemView.list_item_auction_title.value = title

            itemView.list_item_auction_price.value = currentPrice.getFormatted(2)
            itemView.list_item_auction_end_time.value = endTime.toLongDateTimeString()

            itemView.list_item_auction_button_note.setOnClickListener { onClickAuction?.invoke(auction) }
            itemView.list_item_auction_button_note.visibility = if (note != null) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                if (itemId != -1L) {
                    onClickNote?.invoke(auction)
                }
            }
        }
    }
}

