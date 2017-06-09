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

import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyTest {

    @Test
    @Throws(Exception::class)
    fun testSetDollars() {
        assertTrue(Money().dollars == 0.0)
        assertTrue(Money("").dollars == 0.0)
        assertTrue(Money("$12.05").dollars == 12.05)
        assertTrue(Money("12.05").dollars == 12.05)
        assertTrue(Money(12.05).dollars == 12.05)
    }

    @Test
    @Throws(Exception::class)
    fun testFormatted() {
        assertTrue(Money().formatted == "$0.00")
        assertTrue(Money("$12.05").formatted == "$12.05")
        assertTrue(Money(12.05).formatted == "$12.05")
        assertTrue(Money().getFormatted(0) == "$0")
        assertTrue(Money("$12.05").getFormatted(0) == "$12")
        assertTrue(Money(12.05).getFormatted(0) == "$12")
    }

    @Test
    @Throws(Exception::class)
    fun testTimes() {
        assertTrue((Money("$12.05") * 4) == Money("$48.20"))
        assertTrue((Money("$12.05") * 0) == Money("$0"))
        assertTrue((Money("$12.05") * -1) == Money("$-12.05"))
    }

    @Test
    @Throws(Exception::class)
    fun testPlus() {
        assertTrue((Money("$12.05") + Money("$24.10")) == Money("$36.15"))
        assertTrue((Money("$12.05") + Money("$-12.05")) == Money("$0"))
        assertTrue((Money("$12.05") + Money("$-24.10")) == Money("$-12.05"))
        assertTrue((Money("$12.05") + Money("0")) == Money("$12.05"))
    }

    @Test
    @Throws(Exception::class)
    fun testMinus() {
        assertTrue((Money("$12.05") - Money("$24.10")) == Money("$-12.05"))
        assertTrue((Money("$12.05") - Money("$12.05")) == Money("$0"))
        assertTrue((Money("$12.05") - Money("$-24.10")) == Money("$36.15"))
        assertTrue((Money("$12.05") - Money("0")) == Money("$12.05"))
    }

    @Test
    @Throws(Exception::class)
    fun testTimesAssign() {
        assertTrue(runTimesAssign("$12.05", 4, "$48.20"))
        assertTrue(runTimesAssign("$12.05", 0, "$0"))
        assertTrue(runTimesAssign("$12.05", -1, "$-12.05"))
    }

    private fun runTimesAssign(amount: String, mult: Long, expected: String): Boolean {
        val money = Money(amount)
        money *= mult
        return money == Money(expected)
    }

    @Test
    @Throws(Exception::class)
    fun testPlusAssign() {
        assertTrue(runPlusAssign("$12.05", "$24.10", "36.15"))
        assertTrue(runPlusAssign("$12.05", "$-12.05", "$0"))
        assertTrue(runPlusAssign("$12.05", "$-24.10", "$-12.05"))
        assertTrue(runPlusAssign("$12.05", "0", "$12.05"))
    }

    private fun runPlusAssign(amount: String, add: String, expected: String): Boolean {
        val money = Money(amount)
        money += Money(add)
        return money == Money(expected)
    }

    @Test
    @Throws(Exception::class)
    fun testMinusAssign() {
        assertTrue(runMinusAssign("$12.05", "$24.10", "$-12.05"))
        assertTrue(runMinusAssign("$12.05", "$12.05", "$0"))
        assertTrue(runMinusAssign("$12.05", "$-24.10", "$36.15"))
        assertTrue(runMinusAssign("$12.05", "0", "$12.05"))
    }

    private fun runMinusAssign(amount: String, add: String, expected: String): Boolean {
        val money = Money(amount)
        money -= Money(add)
        return money == Money(expected)
    }

}