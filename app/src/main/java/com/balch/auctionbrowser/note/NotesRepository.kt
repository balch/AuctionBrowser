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
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Model API for retrieving/persisting data to the NotesDao repository (Sqlite db)
 */
@Singleton
class NotesRepository @Inject constructor(private val noteDao: NoteDao) {
    @SuppressLint("UseSparseArrays")
    suspend fun getNotes(auctions: List<Auction>): Map<Long, Note> {
        val deferred = CompletableDeferred<Map<Long, Note>>()
        withContext(Dispatchers.IO) {
            val noteMap: Map<Long, Note> = if (auctions.isNotEmpty()) {
                val itemIdsList: List<Long> = auctions.map { (itemId) -> itemId }
                val notes: List<Note> = noteDao.loadAllByIds(itemIdsList.toLongArray())
                notes.associateBy { it.itemId }
            } else {
                mutableMapOf()
            }
            deferred.complete(noteMap)
        }
        return deferred.await()
    }

    suspend fun insert(note: Note): List<Long> {
        val deferred = CompletableDeferred<List<Long>>()
        withContext(Dispatchers.IO) {
            deferred.complete(noteDao.insert(note))
        }

        return deferred.await()
    }

    suspend fun update(note: Note): Int {
        val deferred = CompletableDeferred<Int>()
        withContext(Dispatchers.IO) {
            deferred.complete(noteDao.update(note))
        }
        return deferred.await()
    }

    suspend fun delete(note: Note): Int {
        val deferred = CompletableDeferred<Int>()
        withContext(Dispatchers.IO) {
            deferred.complete(noteDao.delete(note))
        }
        return deferred.await()
    }
}
