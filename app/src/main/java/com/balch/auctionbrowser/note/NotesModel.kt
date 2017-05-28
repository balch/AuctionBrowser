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

package com.balch.auctionbrowser.note

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.Cursor
import android.util.Log

import com.balch.android.app.framework.sql.SqlConnection
import com.balch.android.app.framework.sql.SqlMapper
import com.balch.auctionbrowser.auction.model.Auction

import java.lang.reflect.InvocationTargetException
import java.sql.SQLException
import java.util.HashMap

class NotesModel(private val sqlConnection: SqlConnection) : SqlMapper<Note> {
    companion object {
        private val TAG = NotesModel::class.java.simpleName

        val TABLE_NAME = "notes"

        val COLUMN_ITEM_ID = "item_id"
        val COLUMN_NOTE = "note"
    }

    override fun getTableName(): String {
        return TABLE_NAME
    }

    @SuppressLint("UseSparseArrays")
    fun getNotes(auctions: List<Auction>?): Map<Long, Note> {
        var noteMap:Map<Long, Note>? = null

        if (auctions!!.isNotEmpty()) {

            val cols = auctions.joinToString {"${it.itemId}"}
            val where = "$COLUMN_ITEM_ID in ($cols)"

            try {
                val notes = sqlConnection.query(this, Note::class.java, where, null, null)
                noteMap = notes.associateBy({it.itemId})
            } catch (e: NoSuchMethodException) {
                Log.e(TAG, "getNotes error", e)
            } catch (e: IllegalAccessException) {
                Log.e(TAG, "getNotes error", e)
            } catch (e: InvocationTargetException) {
                Log.e(TAG, "getNotes error", e)
            } catch (e: InstantiationException) {
                Log.e(TAG, "getNotes error", e)
            } catch (e: SQLException) {
                Log.e(TAG, "getNotes error", e)
            }
        }

        return noteMap ?: HashMap<Long, Note>()
    }

    fun insert(note: Note): Long {
        var id: Long = -1
        try {
            id = sqlConnection.insert(this, note)
        } catch (e: SQLException) {
            Log.e(TAG, "inserting note error", e)
        }

        return id
    }

    fun update(note: Note): Boolean {
        return sqlConnection.update(this, note)
    }

    fun delete(note: Note): Boolean {
        return sqlConnection.delete(this, note)
    }

    override fun getContentValues(note: Note): ContentValues {
        val values = ContentValues()

        values.put(COLUMN_ITEM_ID, note.itemId)
        values.put(COLUMN_NOTE, note.note)

        return values
    }

    override fun populate(note: Note, cursor: Cursor, columnMap: Map<String, Int>) {
        note.setId(cursor.getLong(columnMap[SqlMapper.COLUMN_ID]!!))
        note.itemId = cursor.getLong(columnMap[COLUMN_ITEM_ID]!!)
        note.note = cursor.getString(columnMap[COLUMN_NOTE]!!)
    }

}
