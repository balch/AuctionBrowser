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

package com.balch.auctionbrowser.rule

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.rules.MethodRule
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement


class JsonLoaderRule(val jsonFile : String) : MethodRule {

    lateinit var json: String

    override fun apply(base: Statement, method: FrameworkMethod?, target: Any?): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                val ctx = InstrumentationRegistry.getInstrumentation().context
                val stream = ctx.assets.open(jsonFile)
                json =  String(stream.readBytes())
                base.evaluate()
            }
        }
    }
}