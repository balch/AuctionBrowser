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

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.balch.auctionbrowser.auction.Auction;
import com.balch.auctionbrowser.ModelProvider;
import com.balch.android.app.framework.sql.SqlMapper;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotesModel implements SqlMapper<Note> {
    private static final String TAG = NotesModel.class.getSimpleName();

    private final ModelProvider modelProvider;

    public static final String TABLE_NAME = "notes";

    public static final String COLUMN_ITEM_ID = "item_id";
    public static final String COLUMN_NOTE = "note";

    public NotesModel(ModelProvider modelProvider) {
        this.modelProvider = modelProvider;
    }

    public Map<Long, Note> getNotes(List<Auction> auctions) {
        Map<Long, Note> noteMap = null;

        if ((auctions == null) || (auctions.size()==0)) {
            return new HashMap<>();
        }

        String where = COLUMN_ITEM_ID + " in (";
        for (int x = 0; x < auctions.size(); x++) {
            if (x > 0) {
                where += ",";
            }
            where += auctions.get(x).getItemId();
        }
        where += ")";

        try {
            List<Note> notes = modelProvider.getSqlConnection().query(this, Note.class, where, null, null);
            noteMap = new HashMap<>(notes.size());
            for (Note note :notes) {
                noteMap.put(note.getItemId(), note);
            }
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "NoSuchMethodException getting notes", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "IllegalAccessException getting notes", e);
        } catch (InvocationTargetException e) {
            Log.e(TAG, "InvocationTargetException getting notes", e);
        } catch (InstantiationException e) {
            Log.e(TAG, "InstantiationException getting notes", e);
        } catch (SQLException e) {
            Log.e(TAG, "SQLException getting notes", e);
        }

        return noteMap;
    }

    public long insert(Note note) {
        long id = -1;
        try {
            id = modelProvider.getSqlConnection().insert(this, note);
        } catch (SQLException e) {
            Log.e(TAG, "SQLException inserting note", e);
        }

        return id;
    }

    public boolean update(Note note) {
        return modelProvider.getSqlConnection().update(this, note);
    }

    public boolean delete(Note note) {
        return modelProvider.getSqlConnection().delete(this, note);
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
