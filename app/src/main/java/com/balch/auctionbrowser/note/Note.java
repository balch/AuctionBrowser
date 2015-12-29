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

import com.balch.android.app.framework.domain.DomainObject;

import java.io.Serializable;
import java.util.Date;

// TODO: make Parcelable
public class Note extends DomainObject  implements Serializable {
    private static final String TAG = Note.class.getSimpleName();

    private long itemId;
    private String note;

    public Note(long id, long itemId, String note, Date created,
                Date updated) {
        this.id = id;
        this.itemId = itemId;
        this.note = note;
        this.createTime = created;
        this.updateTime = updated;
    }

    public Note() {
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
