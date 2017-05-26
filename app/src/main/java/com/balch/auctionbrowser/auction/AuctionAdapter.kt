/*
 *  Author: Balch
 *  Created: 6/14/15 11:21 AM
 *
 *  This file is part of BB_Challenge.
 *
 *  BB_Challenge is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  BB_Challenge is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MockTrade.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Copyright (C) 2015
 *
 */

package com.balch.auctionbrowser.auction

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import com.balch.auctionbrowser.R
import com.balch.auctionbrowser.auction.commons.inflate
import com.balch.auctionbrowser.auction.commons.loadUrl
import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.note.Note
import com.balch.auctionbrowser.ui.LabelTextView
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.auction_list_item.view.*
import java.text.DateFormat
import java.util.*

class AuctionAdapter : RecyclerView.Adapter<AuctionAdapter.MemberHolder>() {

    private val auctions = ArrayList<Auction>()
    @SuppressLint("UseSparseArrays")

    val notes = HashMap<Long, Note>()
    val clickAuctionObservable: PublishSubject<Auction> = PublishSubject.create<Auction>()
    val clickNoteObservable: PublishSubject<Auction> = PublishSubject.create<Auction>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberHolder {
        return MemberHolder(parent, clickAuctionObservable, clickNoteObservable)
    }

    override fun onBindViewHolder(holder: MemberHolder, position: Int) {
        val auction = auctions[position]
        holder.bind(auction, this.notes[auction.itemId])
    }

    override fun getItemCount(): Int {
        return auctions.size
    }

    internal fun addAuctions(auctions: List<Auction>, notes: Map<Long, Note>) {
        this.auctions.addAll(auctions)
        this.notes.putAll(notes)
        notifyDataSetChanged()
    }

    internal fun clearAuctions() {
        this.auctions.clear()
        this.notes.clear()
        notifyDataSetChanged()
    }

    class MemberHolder(parent: ViewGroup, private val clickAuctionObservable: PublishSubject<Auction>,
                                private val clickNoteObservable: PublishSubject<Auction>)
                        : RecyclerView.ViewHolder(parent.inflate(R.layout.auction_list_item)) {

        private val itemImageView: ImageView by lazy { itemView.list_item_auction_img }
        private val titleTextView: LabelTextView by lazy { itemView.list_item_auction_title }
        private val priceTextView: LabelTextView by lazy { itemView.list_item_auction_price }
        private val endTimeTextView: LabelTextView by lazy { itemView.list_item_auction_end_time }
        private val noteEditButton: Button by lazy { itemView.list_item_auction_button_note }

        fun bind(auction: Auction, note: Note?) {

            itemImageView.loadUrl(auction.imageUrl)
            titleTextView.setValue(auction.title)

            priceTextView.setValue(auction.currentPrice.getFormatted(2))
            endTimeTextView.setValue(DATE_TIME_FORMAT.format(auction.endTime))

            noteEditButton.setOnClickListener { clickNoteObservable.onNext(auction) }
            noteEditButton.visibility = if (note != null) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                if (auction.itemId != -1L) {
                    clickAuctionObservable.onNext(auction)
                }
            }
        }
    }

    companion object {
        private val DATE_TIME_FORMAT = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM)
    }

}
