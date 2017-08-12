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

import android.annotation.SuppressLint
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.annotation.VisibleForTesting
import android.view.View
import com.balch.auctionbrowser.auction.AuctionAdapter
import com.balch.auctionbrowser.auction.AuctionView
import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.auction.model.EBayModel
import com.balch.auctionbrowser.base.BasePresenter
import com.balch.auctionbrowser.base.ModelProvider
import com.balch.auctionbrowser.note.Note
import com.balch.auctionbrowser.note.NotesModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class AuctionPresenter(view: AuctionView,
                       private var auctionViewModel: AuctionViewModel,
                       private var lifecycleOwner: LifecycleOwner,
                       private val ebayAppId: String,
                       private val listener: AuctionPresenterListener) : BasePresenter<AuctionView>(view) {

    interface AuctionPresenterListener {
        val isActivityFinishing: Boolean
        fun showDetail(auction: Auction, note: Note?)
        fun showSearchError(view: View)
    }

    val searchText = auctionViewModel.searchText

    private val disposables = CompositeDisposable()

    @SuppressLint("VisibleForTests")
    override fun createModel(modelProvider: ModelProvider) {

        // Note: the ViewModel survives a ConfigChange event and may already be initialized
        if (!auctionViewModel.isInitialized) {
            val auctionModel = EBayModel(ebayAppId,
                    modelProvider.modelApiFactory.ebayApi)
            val notesModel = NotesModel(modelProvider.database.noteDao())

            auctionViewModel.inject(AuctionAdapter(), auctionModel, notesModel)
        }
    }

    @SuppressLint("VisibleForTests")
    fun initialize(savedInstanceState: Bundle?) {
        auctionViewModel.auctionData.observe(lifecycleOwner,
                Observer<AuctionData> { auctionData -> showAuctions(auctionData) })

        val auctionAdapter = auctionViewModel.auctionAdapter
        view.setAuctionAdapter(auctionAdapter)

        disposables.addAll(
                view.onLoadMore
                        .subscribe({ onLoadMorePages() },
                                { throwable -> Timber.e(throwable, "onLoadMorePages error") }),
                auctionAdapter.onClickAuction
                        .subscribe({ auction -> showDetail(auction) },
                                { throwable -> Timber.e(throwable, "onClickAuction error") }),
                auctionAdapter.onClickNote
                        .subscribe({ auction -> showDetail(auction) },
                                { throwable -> Timber.e(throwable, "onClickNote error") })
        )
    }

    fun doSearch(keyword: String) {
        view.showBusy = true
        view.clearAuctions()
        auctionViewModel.loadAuctions(keyword)
    }

    @VisibleForTesting
    internal fun onLoadMorePages(): Unit {
        view.showBusy = true
        auctionViewModel.loadAuctionsNextPage()
    }

    internal fun sortAuctions(sortColumn: EBayModel.SortColumn) {
        view.showBusy = true
        view.clearAuctions()
        auctionViewModel.loadAuctions(sortColumn = sortColumn)
    }

    @VisibleForTesting
    internal fun showDetail(auction: Auction) {
        val note = view.getNote(auction)
        listener.showDetail(auction, note)
    }

    @VisibleForTesting
    internal fun saveNote(auction: Auction, note: Note?, text: String) {
        if (note == null) {
            disposables.add(
                    Single.just(Note(auction.itemId, text))
                            .subscribeOn(Schedulers.io())
                            .doOnSuccess { note1 -> auctionViewModel.insertNote(note1) }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ note1 -> if (!listener.isActivityFinishing) view.addNote(auction, note1) },
                                    { throwable -> Timber.e(throwable, "insertNote error") })
            )
        } else {
            note.noteText = text
            disposables.add(
                    Single.just(note)
                            .subscribeOn(Schedulers.io())
                            .subscribe({ note1 -> auctionViewModel.updateNote(note1) },
                                    { throwable -> Timber.e(throwable, "updateNote error") })
            )
        }
    }

    @VisibleForTesting
    internal fun showAuctions(auctionData: AuctionData?) {
        view.showBusy = false

        if (auctionData?.hasError == false) {
            view.addAuctions(auctionData.auctions, auctionData.notes)
        } else {
            listener.showSearchError(view)
        }

        view.doneLoading(auctionViewModel.hasMoreAuctionPages)

    }

    fun clearNote(auction: Auction, note: Note?) {
        if (note != null) {
            disposables.add(
                    Single.just(true)
                            .subscribeOn(Schedulers.io())
                            .doOnSuccess { _ -> auctionViewModel.deleteNote(note) }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ _ -> if (!listener.isActivityFinishing) view.clearNote(auction) },
                                    { throwable -> Timber.e(throwable, "deleteNote error") })
            )
        }
    }

    override fun cleanup() {
        super.cleanup()

        auctionViewModel.auctionData.removeObservers(lifecycleOwner)
        disposables.dispose()
    }
}