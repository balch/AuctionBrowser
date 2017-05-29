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

class AuctionViewModel : ViewModel() {

    private val AUCTION_FETCH_COUNT = 30

    var isInitialized = false
        private set

    lateinit private var auctionModel: EBayModel
    @get:VisibleForTesting(otherwise = VisibleForTesting.NONE)
    lateinit var notesModel: NotesModel
        private set
    lateinit var auctionAdapter: AuctionAdapter
        private set

    var searchText: String? = null
        private set

    private var totalPages: Long = 0
    private var currentPage: Int = 0

    private var sortColumn: EBayModel.SortColumn = EBayModel.SortColumn.BEST_MATCH
    private var disposableGetAuction: Disposable? = null
    private val auctionDataLive = MutableLiveData<AuctionData>()

    val auctionData: LiveData<AuctionData>
        get() = auctionDataLive

    fun initialize(adapter: AuctionAdapter, eBayModel: EBayModel, notesModel: NotesModel) {
        isInitialized = true
        this.auctionAdapter = adapter
        this.auctionModel = eBayModel
        this.notesModel = notesModel
    }

    fun loadAuctions(sortColumn: EBayModel.SortColumn) {
        loadAuctions(searchText?:"", sortColumn)
    }

    fun loadAuctions(searchText: String) {
        loadAuctions(searchText, sortColumn)
    }

    private fun loadAuctions(searchText: String, sortColumn: EBayModel.SortColumn) {
        this.totalPages = -1
        this.currentPage = 1
        this.searchText = searchText
        this.sortColumn = sortColumn
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


