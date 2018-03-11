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
 * Copyright (C) 2018
 *
 */

package com.balch.auctionbrowser

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.balch.auctionbrowser.auction.AuctionAdapter
import com.balch.auctionbrowser.auction.model.EBayModel
import com.balch.auctionbrowser.note.NotesModel
import javax.inject.Inject


@Suppress("UNCHECKED_CAST")
internal class AuctionViewModelFactory
    @Inject constructor(private val adapter: AuctionAdapter,
                        private val eBayModel: EBayModel,
                        private val notesModel: NotesModel) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuctionViewModel::class.java)) {
            return AuctionViewModel(adapter, eBayModel, notesModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}