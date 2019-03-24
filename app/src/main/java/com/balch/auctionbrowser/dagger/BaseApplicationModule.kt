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
 * Copyright (C) 2019
 *
 */

package com.balch.auctionbrowser.dagger

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.balch.auctionbrowser.AuctionApplication
import com.balch.auctionbrowser.AuctionDatabase
import com.balch.auctionbrowser.R
import com.balch.auctionbrowser.note.NoteDao
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
open class BaseApplicationModule {

    companion object {
        const val EBAY_APP_ID = "ebayAppId"
        const val APP_CONTEXT = "appContext"
    }

    @Provides
    @Singleton
    @Named(APP_CONTEXT)
    internal fun providesApplicationContext(app: Application): Context {
        return app
    }

    @Provides
    @Singleton
    internal fun providesAuctionDatabase(@Named(APP_CONTEXT) context: Context): AuctionDatabase {
        return Room.databaseBuilder(context, AuctionDatabase::class.java, AuctionApplication.DATABASE_NAME).build()
    }

    @Singleton
    @Provides
    internal fun providesNoteDao(database: AuctionDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    @Singleton
    @Named(EBAY_APP_ID)
    internal fun providesEbayAppId(@Named(APP_CONTEXT) context: Context): String {
        return context.getString(R.string.ebay_app_id)
    }

}
