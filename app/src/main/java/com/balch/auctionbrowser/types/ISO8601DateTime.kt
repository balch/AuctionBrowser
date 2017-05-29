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

package com.balch.auctionbrowser.types

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object ISO8601DateTime {
    private val ISO_8601_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ"
    private val ISO_8601_DATE_TIME_FORMAT_1 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
    private val ISO_8601_DATE_FORMAT = "yyyy-MM-dd'T'00:00:00Z"

    @JvmOverloads fun toISO8601(date: Date, dateOnly: Boolean = false): String {
        val tz = if (dateOnly) TimeZone.getDefault() else TimeZone.getTimeZone("UTC")
        val df = SimpleDateFormat(getDateFormat(dateOnly, null), Locale.getDefault())
        df.timeZone = tz
        return df.format(date)
    }

    @Throws(ParseException::class)
    fun toDate(iso8601string: String): Date {
        val s = iso8601string.replace("Z", "+00:00")
        return SimpleDateFormat(getDateFormat(false, iso8601string), Locale.getDefault()).parse(s)
    }

    fun getDateFormat(dataOnly: Boolean, iso8601String: String?): String {
        return if (dataOnly)
            ISO_8601_DATE_FORMAT
        else if (iso8601String != null && iso8601String.contains("."))
            ISO_8601_DATE_TIME_FORMAT_1
        else
            ISO_8601_DATE_TIME_FORMAT
    }
}
