package com.balch.auctionbrowser.auction.ext

import java.text.DateFormat
import java.util.*

private val LONG_DATE_TIME_FORMAT = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM)
fun Date.toLongDateTimeString(): String {
    return LONG_DATE_TIME_FORMAT.format(this)
}

