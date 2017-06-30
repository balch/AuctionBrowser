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

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.note.Note
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*

class AuctionAdapter : RecyclerView.Adapter<AuctionViewHolder>() {

    // public properties
    @SuppressLint("UseSparseArrays")
    val notes = HashMap<Long, Note>()

    val onClickAuction: Observable<Auction>
        get() = clickAuctionSubject

    val onClickNote: Observable<Auction>
        get() = clickNoteSubject

    // adapter auction data
    val auctions = ArrayList<Auction>()

    // backing for exposing user initiated events to Activity
    private val clickAuctionSubject: PublishSubject<Auction> = PublishSubject.create<Auction>()
    private val clickNoteSubject: PublishSubject<Auction> = PublishSubject.create<Auction>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuctionViewHolder {
        return AuctionViewHolder(parent, clickAuctionSubject, clickNoteSubject)
    }

    override fun onBindViewHolder(holder: AuctionViewHolder, position: Int) {
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

}
