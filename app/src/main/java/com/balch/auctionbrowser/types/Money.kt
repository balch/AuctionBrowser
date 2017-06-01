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

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import java.text.DecimalFormat
import java.util.*

// $1 = 10000mc
data class Money(private var microCents: Long = 0,
                 private var currency: Currency = Currency.getInstance("USD")) {
    private val DOLLAR_TO_MICRO_CENT = 10000

    constructor(dollars: Double) : this() {
        this.dollars = dollars
    }

    constructor(dollars: String) : this() {
        this.setDollars(dollars)
    }

    var dollars: Double
        get() = microCents / DOLLAR_TO_MICRO_CENT.toDouble()
        set(dollars) {
            microCents = (dollars * DOLLAR_TO_MICRO_CENT).toLong()
        }

    fun setDollars(input: String) {
        var dollarString = input
        var dollarValue: Double = 0.0

        if (dollarString.isNotEmpty()) {
            val symbol = symbol
            if (dollarString.startsWith(symbol)) {
                dollarString = dollarString.substring(symbol.length)
            }

            dollarValue = java.lang.Double.valueOf(dollarString.replace(",", ""))
        }
        dollars = dollarValue
    }

    val formatted: String
        get() = getFormatted(2)

    val formattedWithColor: Spannable
        get() {
            val sign = if (microCents >= 0) "+" else "-"
            val spanColor = ForegroundColorSpan(if (microCents >= 0) Color.GREEN else Color.RED)

            val `val` = String.format(Locale.getDefault(), "%s%s%.02f", sign, symbol, Math.abs(microCents) / 1000f)
            val spanString = SpannableStringBuilder(`val`)
            spanString.setSpan(spanColor, 0, `val`.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

            return spanString
        }

    fun getFormatted(decimalPlaces: Int): String {
        val dollars = dollars

        val patternBuilder = StringBuilder(symbol)
        patternBuilder.append("#,##0")

        if (decimalPlaces == 1) {
            patternBuilder.append(".0")
        } else if (decimalPlaces >= 2) {
            patternBuilder.append(".00")
            for (x in 0..decimalPlaces - 2 - 1) {
                patternBuilder.append("#")
            }
        }

        val pattern = patternBuilder.toString()
        val format = DecimalFormat(pattern + ";-" + pattern)
        return format.format(dollars)
    }

    val symbol: String
        get() = currency.symbol

    fun getCurrencyNoGroupSep(decimalPlaces: Int): String {
        val dollars = dollars
        return String.format("%1$.0" + decimalPlaces + "f", dollars)
    }

    operator fun times(quantity: Long): Money {
        return Money(microCents * quantity)
    }

    operator fun plus(other: Money): Money {
        return Money(microCents + other.microCents)
    }

    operator fun minus(other: Money): Money {
        return Money(microCents - other.microCents)
    }

    operator fun timesAssign(value: Long) {
        this.microCents *= value
    }

    operator fun plusAssign(money: Money) {
        this.microCents += money.microCents
    }

    operator fun minusAssign(money: Money) {
        this.microCents -= money.microCents
    }
}
