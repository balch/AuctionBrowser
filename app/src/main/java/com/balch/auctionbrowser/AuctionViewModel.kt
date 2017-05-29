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

package com.balch.auctionbrowser

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.support.annotation.VisibleForTesting
import android.util.Log
import com.balch.auctionbrowser.auction.AuctionAdapter
import com.balch.auctionbrowser.auction.model.EBayModel
import com.balch.auctionbrowser.note.Note
import com.balch.auctionbrowser.note.NotesModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

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
class AuctionViewModel : ViewModel() {

    private val AUCTION_FETCH_COUNT = 30

    // public properties
    var isInitialized = false
        private set

    val auctionData: LiveData<AuctionData>
        get() = auctionDataLive

    var searchText: String? = null
        private set

    // injected models
    lateinit private var auctionModel: EBayModel
    @get:VisibleForTesting(otherwise = VisibleForTesting.NONE)
    lateinit var notesModel: NotesModel
        private set
    lateinit var auctionAdapter: AuctionAdapter
        private set

    // paging vars
    private var totalPages: Long = 0
    private var currentPage: Int = 0
    private var sortColumn: EBayModel.SortColumn = EBayModel.SortColumn.BEST_MATCH

    // LiveData<AuctionData>
    private val auctionDataLive = MutableLiveData<AuctionData>()

    // disposables
    private var disposableGetAuction: Disposable? = null

    fun inject(adapter: AuctionAdapter, eBayModel: EBayModel, notesModel: NotesModel) {
        isInitialized = true
        this.auctionAdapter = adapter
        this.auctionModel = eBayModel
        this.notesModel = notesModel
    }

    fun loadAuctions(searchText: String? = null, sortColumn: EBayModel.SortColumn? = null) {
        this.totalPages = -1
        this.currentPage = 1
        this.searchText = searchText ?: this.searchText ?: ""
        this.sortColumn = sortColumn ?: this.sortColumn
        getAuctionsAsync()
    }

    fun loadAuctionsNextPage() {
        this.currentPage++
        getAuctionsAsync()
    }

    fun hasMoreAuctionPages(page: Long): Boolean {
        return totalPages == -1L || page < totalPages
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

    private fun getAuctionsAsync() {
        disposeGetAuctionDisposable()
        disposableGetAuction = auctionModel
                .getAuctions(searchText!!, currentPage.toLong(), AUCTION_FETCH_COUNT, sortColumn)
                .subscribeOn(Schedulers.io())
                .doOnNext { auctionData ->
                    auctionData.notes = notesModel.getNotes(auctionData.auctions)
                    totalPages = auctionData.totalPages.toLong()
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({auctionData -> auctionDataLive.setValue(auctionData)},
                            { throwable ->
                                Log.e("AuctionViewModel", "Error", throwable)
                                auctionDataLive.setValue(null)
                            })
    }

    fun disposeGetAuctionDisposable() {
        disposableGetAuction?.dispose()
        disposableGetAuction = null
    }

    override fun onCleared() {
        disposeGetAuctionDisposable()
    }

}


