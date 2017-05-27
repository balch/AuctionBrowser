package com.balch.auctionbrowser

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.support.annotation.VisibleForTesting
import com.balch.auctionbrowser.auction.AuctionAdapter
import com.balch.auctionbrowser.auction.model.EBayModel
import com.balch.auctionbrowser.note.Note
import com.balch.auctionbrowser.note.NotesModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class AuctionViewModel : ViewModel() {

    companion object {
        private val AUCTION_FETCH_COUNT = 30
    }

    var auctionModel: EBayModel? = null

    @get:VisibleForTesting(otherwise = VisibleForTesting.NONE)
    var notesModel: NotesModel? = null
        set

    var auctionAdapter: AuctionAdapter? = null

    var searchText: String? = null
        private set

    private var totalPages: Long = 0
    private var currentPage: Int = 0

    private var sortColumn: EBayModel.SortColumn = EBayModel.SortColumn.BEST_MATCH
    private var disposableGetAuction: Disposable? = null
    private val auctionDataLive = MutableLiveData<AuctionData>()

    val isInitialized: Boolean
        get() = auctionModel != null && notesModel != null

    val auctionData: LiveData<AuctionData>
        get() = auctionDataLive

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
        notesModel!!.insert(note)
    }

    fun updateNote(note: Note) {
        notesModel!!.update(note)
    }

    fun deleteNote(note: Note) {
        notesModel!!.delete(note)
    }

    private fun getAuctionsAsync() {
        disposeGetAuctionDisposable()
        disposableGetAuction = auctionModel!!
                .getAuctions(searchText, currentPage.toLong(), AUCTION_FETCH_COUNT, sortColumn)
                .subscribeOn(Schedulers.io())
                .doOnNext { auctionData ->
                    if (auctionData.auctions != null) {
                        auctionData.notes = notesModel!!.getNotes(auctionData.auctions)
                    }
                    totalPages = auctionData.totalPages.toLong()
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({auctionData -> auctionDataLive.setValue(auctionData)},
                            { _ -> auctionDataLive.setValue(null)})
    }

    fun disposeGetAuctionDisposable() {
        disposableGetAuction?.dispose()
        disposableGetAuction = null
    }

    override fun onCleared() {
        disposeGetAuctionDisposable()
    }

}


