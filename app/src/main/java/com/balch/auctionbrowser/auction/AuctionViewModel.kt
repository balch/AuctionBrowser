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

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.Transformations.switchMap
import androidx.lifecycle.ViewModel
import com.balch.auctionbrowser.auction.model.AuctionRepository
import com.balch.auctionbrowser.auction.model.EBayModel
import com.balch.auctionbrowser.note.Note
import com.balch.auctionbrowser.note.NotesModel


/**
 * This ViewModel exposes a LiveData object which emits AuctionData objects from the
 * EBay API. An AuctionData object contains the current page of found Auctions.
 *
 * The ViewModel also stores state data that should survive a ConfigChange operation. This
 * includes the AuctionAdapter that contains the entire Auction list and the info necessary
 * to retrieve the next page of Auctions.
 *
 * The class implements simple dependency injection using the `inject()` method to setter-inject
 * the Adapter and ModelApis.
 */
class AuctionViewModel(private val repository: AuctionRepository,
                       private val notesModel: NotesModel) : ViewModel() {

    companion object {
        private const val AUCTION_FETCH_COUNT = 30
    }

    private class SearchData(val searchText: String,
                             val sortColumn: EBayModel.SortColumn)

    private val searchQuery = MutableLiveData<SearchData>()
    private val auctionResult = map(searchQuery) {
        repository.searchAuctions(it.searchText, it.sortColumn, AUCTION_FETCH_COUNT)
    }
    val auctionData = switchMap(auctionResult) { it.pagedList }!!
    val networkState = switchMap(auctionResult) { it.networkState }!!

    val searchText: String
        get() = searchQuery.value?.searchText ?: ""

    val sortColumn: EBayModel.SortColumn
        get() = searchQuery.value?.sortColumn ?: EBayModel.SortColumn.BEST_MATCH

    fun loadAuctions(searchText: String, sortColumn: EBayModel.SortColumn)  {
        searchQuery.value = SearchData(searchText, sortColumn)
    }

    fun insertNote(note: Note) {
        notesModel.insert(note)
    }

    fun updateNote(note: Note) {
        notesModel.update(note)
    }

    fun deleteNote(note: Note) {
        notesModel.delete(note)
    }


}


