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

package com.balch.auctionbrowser;

import android.app.Application;
import android.graphics.Bitmap;
import android.os.StrictMode;
import android.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.balch.android.app.framework.sql.SqlConnection;
import com.balch.auctionbrowser.settings.Settings;

public class AuctionApplication extends Application implements ModelProvider {
    private static final String TAG = AuctionApplication.class.getSimpleName();

    private static final String DATABASE_NAME = "auction_browser.db";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_CREATES_SCRIPT = "sql/create.sql";
    private static final String DATABASE_UPDATE_SCRIPT_FORMAT = "sql/upgrade_%d.sql";

    private SqlConnection sqlConnection;
    private RequestQueue requestQueue;
    private ImageLoader imageLoader;

    private Settings settings;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());

        }

        this.sqlConnection = new SqlConnection(this, DATABASE_NAME, DATABASE_VERSION,
                DATABASE_CREATES_SCRIPT, DATABASE_UPDATE_SCRIPT_FORMAT);

        this.requestQueue = Volley.newRequestQueue(this, 10);
        this.imageLoader = new ImageLoader(this.requestQueue,
                new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> cache = new LruCache<>(20);
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }
        });

    }

    @Override
    public Settings getSettings() {
        return this.settings;
    }

    @Override
    public SqlConnection getSqlConnection() {
        return this.sqlConnection;
    }

    @Override
    public RequestQueue getRequestQueue() {
        return this.requestQueue;
    }

    @Override
    public ImageLoader getImageLoader() {
        return this.imageLoader;
    }

}
