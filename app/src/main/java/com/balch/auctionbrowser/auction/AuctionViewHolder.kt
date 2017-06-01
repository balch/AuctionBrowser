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

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import com.balch.auctionbrowser.R
import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.ext.inflate
import com.balch.auctionbrowser.ext.loadUrl
import com.balch.auctionbrowser.ext.toLongDateTimeString
import com.balch.auctionbrowser.note.Note
import com.balch.auctionbrowser.ui.LabelTextView
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.auction_list_item.view.*

class AuctionViewHolder(parent: ViewGroup, private val clickAuctionObservable: PublishSubject<Auction>,
                   private val clickNoteObservable: PublishSubject<Auction>)
    : RecyclerView.ViewHolder(parent.inflate(R.layout.auction_list_item)) {

    private val itemImageView: ImageView by lazy { itemView.list_item_auction_img }
    private val titleTextView: LabelTextView by lazy { itemView.list_item_auction_title }
    private val priceTextView: LabelTextView by lazy { itemView.list_item_auction_price }
    private val endTimeTextView: LabelTextView by lazy { itemView.list_item_auction_end_time }
    private val noteEditButton: Button by lazy { itemView.list_item_auction_button_note }

    fun bind(auction: Auction, note: Note?) {

        with (auction) {
            itemImageView.loadUrl(imageUrl)
            titleTextView.value = title

            priceTextView.value = currentPrice.getFormatted(2)
            endTimeTextView.value = endTime.toLongDateTimeString()

            noteEditButton.setOnClickListener { clickNoteObservable.onNext(auction) }
            noteEditButton.visibility = if (note != null) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                if (itemId != -1L) {
                    clickAuctionObservable.onNext(auction)
                }
            }
        }
    }
}

