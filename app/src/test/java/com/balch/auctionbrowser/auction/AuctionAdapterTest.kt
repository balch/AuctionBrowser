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

package com.balch.auctionbrowser.auction

import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.note.Note
import com.balch.auctionbrowser.test.BaseTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import kotlin.test.assertTrue

class AuctionAdapterTest : BaseTest() {
    private lateinit var adapter: AuctionAdapter

    @Before
    fun setUp() {

        MockitoAnnotations.initMocks(this)

        adapter = spy(AuctionAdapter())
        doNothing().`when`(adapter).notifyDataSetChanged()
    }

    @Test
    fun testClearAuctions() {
        val auctions: List<Auction> = List(5, { mock(Auction::class.java) })
        val notes: Map<Long, Note> = mapOf(0L to mock(Note::class.java),
                1L to mock(Note::class.java),
                3L to mock(Note::class.java))
        adapter.addAuctions(auctions, notes)
        clearInvocations(adapter)

        //region Execute Test
        adapter.clearAuctions()
        //endregion

        assertTrue(adapter.notes.isEmpty())
        assertThat(adapter.itemCount).isEqualTo(0)
        verify(adapter).notifyDataSetChanged()
    }

    @Test
    fun testAddAuctions() {
        val auctions: List<Auction> = List(5, { mock(Auction::class.java) })
        val notes: Map<Long, Note> = mapOf(0L to mock(Note::class.java),
                1L to mock(Note::class.java),
                3L to mock(Note::class.java))
        clearInvocations(adapter)

        //region Execute Test
        adapter.addAuctions(auctions, notes)
        //endregion

        assertThat(adapter.notes.size).isEqualTo(3)
        assertThat(adapter.itemCount).isEqualTo(5)
        verify(adapter).notifyDataSetChanged()
    }
}

