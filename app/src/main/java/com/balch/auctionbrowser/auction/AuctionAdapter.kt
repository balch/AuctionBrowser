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

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.dagger.ActivityScope
import com.balch.auctionbrowser.note.Note
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

@ActivityScope
class AuctionAdapter @Inject constructor() :
        androidx.recyclerview.widget.RecyclerView.Adapter<AuctionViewHolder>() {

    // public properties
    val onClickAuction: Observable<Auction>
        get() = clickAuctionSubject

    val onClickNote: Observable<Auction>
        get() = clickNoteSubject

    // backing for exposing user initiated events to Activity
    private val clickAuctionSubject: PublishSubject<Auction> = PublishSubject.create<Auction>()
    private val clickNoteSubject: PublishSubject<Auction> = PublishSubject.create<Auction>()

    // adapter auction data
    private val auctions = mutableListOf<Auction>()
    val notes = mutableMapOf<Long, Note>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuctionViewHolder {
        return AuctionViewHolder(parent, clickAuctionSubject, clickNoteSubject)
    }

    override fun onBindViewHolder(holder: AuctionViewHolder, position: Int) {
        val auction = auctions[position]
        holder.bind(auction, notes[auction.itemId])
    }

    override fun getItemCount(): Int {
        return auctions.size
    }

    fun addAuctions(auctions: List<Auction>, notes: Map<Long, Note>) {
        this.auctions.addAll(auctions)
        this.notes.putAll(notes)
        notifyDataSetChanged()
    }

    fun clearAuctions() {
        auctions.clear()
        notes.clear()
        notifyDataSetChanged()
    }

    fun getNote(auction: Auction): Note? {
        return notes[auction.itemId]
    }

    fun clearNote(auction: Auction) {
        notes.remove(auction.itemId)
        notifyDataSetChanged()
    }

    fun addNote(auction: Auction, note: Note) {
        notes.put(auction.itemId, note)
        notifyDataSetChanged()
    }

}
