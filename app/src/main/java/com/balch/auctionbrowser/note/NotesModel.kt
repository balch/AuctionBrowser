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
import com.balch.auctionbrowser.ext.logTiming
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Model API for retrieving/persisting data to the NotesDao repository (Sqlite db)
 */
@Singleton
class NotesModel @Inject constructor(private val noteDao: NoteDao) {
    @SuppressLint("UseSparseArrays")
    fun getNotes(auctions: List<Auction>): Map<Long, Note> {
        var noteMap:Map<Long, Note>? = null

        logTiming("getNotes") {
            if (auctions.isNotEmpty()) {

                val itemIdsList: List<Long> = auctions.map { (itemId) -> itemId }

                val notes = noteDao.loadAllByIds(itemIdsList.toLongArray())
                noteMap = notes.associateBy({ it.itemId })
            }
        }

        return noteMap ?: HashMap<Long, Note>()
    }

    fun insert(note: Note) {
        logTiming("insert itemId=${note.itemId}") {
            noteDao.insert(note)
        }
    }

    fun update(note: Note) {
        logTiming("update itemId=${note.itemId}") {
            noteDao.update(note)
        }
    }

    fun delete(note: Note) {
        logTiming("delete itemId=${note.itemId}") {
            noteDao.delete(note)
        }
    }
}
