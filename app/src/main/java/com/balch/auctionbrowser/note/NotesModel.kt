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

package com.balch.auctionbrowser.note

import android.annotation.SuppressLint
import com.balch.auctionbrowser.auction.model.Auction
import java.util.*

class NotesModel(private val sqlConnection: NoteDao) {
    @SuppressLint("UseSparseArrays")
    fun getNotes(auctions: List<Auction>): Map<Long, Note> {
        var noteMap:Map<Long, Note>? = null

        if (auctions.isNotEmpty()) {

            val itemIdsList: List<Long> = auctions.map { (itemId) -> itemId }

            val notes = sqlConnection.loadAllByIds(itemIdsList.toLongArray())
            noteMap = notes.associateBy({it.itemId})
        }

        return noteMap ?: HashMap<Long, Note>()
    }

    fun insert(note: Note) {
        sqlConnection.insert(note)
    }

    fun update(note: Note) {
        sqlConnection.update(note)
    }

    fun delete(note: Note) {
        sqlConnection.delete(note)
    }
}
