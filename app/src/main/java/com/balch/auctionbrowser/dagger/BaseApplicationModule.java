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

package com.balch.auctionbrowser.dagger;

import android.app.Application;
import android.content.Context;

import com.balch.auctionbrowser.AuctionApplication;
import com.balch.auctionbrowser.AuctionDatabase;
import com.balch.auctionbrowser.R;
import com.balch.auctionbrowser.note.NoteDao;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Named;
import javax.inject.Singleton;

import androidx.room.Room;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class BaseApplicationModule {
    public static final String EBAY_APP_ID = "ebayAppId";
    public static final String APP_CONTEXT = "appContext";

    @Binds
    @Singleton
    @Named(APP_CONTEXT)
    abstract Context bindsApplicationContext(Application app);

    @Provides
    @Singleton
    static AuctionDatabase providesAuctionDatabase(@Named(APP_CONTEXT)Context context) {
        return Room.databaseBuilder(context, AuctionDatabase.class, AuctionApplication.DATABASE_NAME)
                .build();
    }

    @Singleton
    @Provides
    static NoteDao providesNoteDao(AuctionDatabase database) {
        return database.noteDao();
    }

    @Provides
    @Singleton
    @Named(EBAY_APP_ID)
    static String providesEbayAppId(@Named(APP_CONTEXT) Context context) {
        return context.getString(R.string.ebay_app_id);
    }

    @Provides
    @Singleton
    static Executor providesNetworkExecutor() {
        return Executors.newFixedThreadPool(5);
    }

}
