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

package com.balch.auctionbrowser.test

import com.balch.auctionbrowser.AuctionDatabase
import com.balch.auctionbrowser.ModelApiFactory
import com.balch.auctionbrowser.ModelProvider
import com.balch.auctionbrowser.auction.model.EBayApi
import com.balch.auctionbrowser.note.NoteDao
import org.mockito.Mockito
import org.mockito.Mockito.mock

class TestModelProvider: ModelProvider {

    // mock versions of app scoped objects used in test to verify interactions
    val mockNotesDao: NoteDao by lazy { mock(NoteDao::class.java) }
    val ebayApi: EBayApi by lazy { mock(EBayApi::class.java) }

    // app scope delegation objects to create api and dao objects
    override val database: AuctionDatabase = mock(AuctionDatabase::class.java)
    override val modelApiFactory: ModelApiFactory = mock(ModelApiFactory::class.java)

    init {
        Mockito.`when`(database.noteDao()).thenReturn(mockNotesDao)
        Mockito.`when`(modelApiFactory.ebayApi).thenReturn(ebayApi)
    }

}
