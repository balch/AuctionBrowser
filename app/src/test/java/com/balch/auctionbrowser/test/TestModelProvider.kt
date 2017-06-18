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

package com.balch.auctionbrowser.test

import android.arch.persistence.db.SupportSQLiteOpenHelper
import android.arch.persistence.room.DatabaseConfiguration
import android.arch.persistence.room.InvalidationTracker
import com.balch.auctionbrowser.AuctionDatabase
import com.balch.auctionbrowser.base.ModelApiFactory
import com.balch.auctionbrowser.auction.model.EBayApi
import com.balch.auctionbrowser.base.ModelProvider
import com.balch.auctionbrowser.note.NoteDao
import org.mockito.Mockito.mock

class TestModelProvider: ModelProvider {
    override val database: AuctionDatabase = TestAuctionDatabase()
    override val modelApiFactory: ModelApiFactory = TestModelApiFactory()
}

class TestModelApiFactory: ModelApiFactory {
    val mockEbayApi: EBayApi by lazy { mock(EBayApi::class.java) }

    override val ebayApi: EBayApi
        get() = mockEbayApi
}

class TestAuctionDatabase: AuctionDatabase() {
    val mockNotesDao: NoteDao by lazy { mock(NoteDao::class.java) }

    override fun noteDao(): NoteDao {
        return mockNotesDao
    }

    override fun createOpenHelper(p0: DatabaseConfiguration?): SupportSQLiteOpenHelper? {
        // no-op
        return null
    }

    override fun createInvalidationTracker(): InvalidationTracker? {
        // no-op
        return null
    }

}
