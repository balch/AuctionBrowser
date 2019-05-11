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

import androidx.room.*
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface NoteDao {
    @Query("SELECT * FROM note WHERE itemId IN (:itemIds)")
    fun loadAllByIds(itemIds: LongArray): Maybe<List<Note>>

    @Insert
    fun insert(vararg notes: Note): Single<List<Long>>

    @Delete
    fun delete(note: Note): Single<Int>

    @Update
    fun update(vararg notes: Note): Single<Int>
}