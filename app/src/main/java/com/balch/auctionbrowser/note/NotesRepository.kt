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
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Model API for retrieving/persisting data to the NotesDao repository (Sqlite db)
 */
@Singleton
class NotesRepository @Inject constructor(private val noteDao: NoteDao) {
    @SuppressLint("UseSparseArrays")
    suspend fun getNotes(auctions: List<Auction>): Map<Long, Note> {
        return if (auctions.isNotEmpty()) {
            val itemIdsList: List<Long> = auctions.map { (itemId) -> itemId }
            val notes:List<Note> = noteDao.loadAllByIds(itemIdsList.toLongArray())
            return notes.associateBy { it.itemId }
        } else {
            mutableMapOf()
        }
    }

    suspend fun insert(note: Note): List<Long> {
        logTiming("insert itemId=${note.itemId}") {
            return noteDao.insert(note)
        }
    }

    suspend fun update(note: Note): Int {
        logTiming("update itemId=${note.itemId}") {
            return noteDao.update(note)
        }
    }

    suspend fun delete(note: Note): Int {
        logTiming("delete itemId=${note.itemId}") {
            return noteDao.delete(note)
        }
    }
}
