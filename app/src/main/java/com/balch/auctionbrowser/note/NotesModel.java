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

package com.balch.auctionbrowser.note;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.balch.android.app.framework.sql.SqlConnection;
import com.balch.android.app.framework.sql.SqlMapper;
import com.balch.auctionbrowser.auction.model.Auction;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotesModel implements SqlMapper<Note> {
    private static final String TAG = NotesModel.class.getSimpleName();

    private final SqlConnection sqlConnection;

    public static final String TABLE_NAME = "notes";

    public static final String COLUMN_ITEM_ID = "item_id";
    public static final String COLUMN_NOTE = "note";

    public NotesModel(SqlConnection sqlConnection) {
        this.sqlConnection = sqlConnection;
    }

    public Map<Long, Note> getNotes(List<Auction> auctions) {
        @SuppressLint("UseSparseArrays")
        Map<Long, Note> noteMap = new HashMap<>();

        if ((auctions != null) && !auctions.isEmpty()) {

            String itemIds = "";
            for (int x = 0; x < auctions.size(); x++) {
                if (x > 0) {
                    itemIds += ",";
                }
                itemIds += auctions.get(x).getItemId();
            }

            String where = COLUMN_ITEM_ID + " in (" + itemIds + ")";

            try {
                List<Note> notes = sqlConnection.query(this, Note.class, where, null, null);
                for (Note note : notes) {
                    noteMap.put(note.getItemId(), note);
                }
            } catch (NoSuchMethodException | IllegalAccessException
                    | InvocationTargetException | InstantiationException
                    | SQLException e) {
                Log.e(TAG, "getNotes error", e);
            }
        }

        return noteMap;
    }

    public long insert(Note note) {
        long id = -1;
        try {
            id = sqlConnection.insert(this, note);
        } catch (SQLException e) {
            Log.e(TAG, "inserting note error", e);
        }

        return id;
    }

    public boolean update(Note note) {
        return sqlConnection.update(this, note);
    }

    public boolean delete(Note note) {
        return sqlConnection.delete(this, note);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public ContentValues getContentValues(Note note) {
        ContentValues values = new ContentValues();

        values.put(COLUMN_ITEM_ID, note.getItemId());
        values.put(COLUMN_NOTE, note.getNote());

        return values;
    }

    @Override
    public void populate(Note note, Cursor cursor, Map<String, Integer> columnMap) {
        note.setId(cursor.getLong(columnMap.get(COLUMN_ID)));
        note.setItemId(cursor.getLong(columnMap.get(COLUMN_ITEM_ID)));
        note.setNote(cursor.getString(columnMap.get(COLUMN_NOTE)));
    }
}
