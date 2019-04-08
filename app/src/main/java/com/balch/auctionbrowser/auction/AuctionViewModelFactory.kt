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

package com.balch.auctionbrowser.auction

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.balch.auctionbrowser.dagger.BaseApplicationModule
import com.balch.auctionbrowser.note.NotesModel
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Named

@Suppress("UNCHECKED_CAST")
internal class AuctionViewModelFactory
@Inject constructor(@Named(BaseApplicationModule.APP_CONTEXT) private val context: Context,
                    private val networkExecutor: Executor,
                    private val notesModel: NotesModel) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuctionViewModel::class.java)) {
            return AuctionViewModel(context, notesModel, networkExecutor) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}