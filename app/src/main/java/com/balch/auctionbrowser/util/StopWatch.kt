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

package com.balch.auctionbrowser.util

class StopWatch(startRunning: Boolean = true) {
    private var startTime: Long = 0
    private var stopTime: Long = 0
    private var running = false

    init {
        if (startRunning) {
            start()
        }
    }

    fun start(): StopWatch {
        startTime = System.currentTimeMillis()
        running = true
        return this
    }

    fun stop(): Long {
        stopTime = System.currentTimeMillis()
        running = false
        return elapsedTime
    }

    val elapsedTime: Long
        get() {
            val endTime = if (running) System.currentTimeMillis() else stopTime
            return endTime - startTime
        }

    val elapsedTimeSecs: Double
        get() = elapsedTime / 1000.0
}

