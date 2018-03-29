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

import android.annotation.SuppressLint
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.annotation.VisibleForTesting
import android.support.v4.app.FragmentManager
import android.view.View
import android.widget.SearchView
import com.balch.auctionbrowser.R
import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.auction.model.AuctionData
import com.balch.auctionbrowser.auction.model.EBayModel
import com.balch.auctionbrowser.base.BasePresenter
import com.balch.auctionbrowser.dagger.ActivityScope
import com.balch.auctionbrowser.note.Note
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.internal.operators.completable.CompletableFromAction
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@ActivityScope
class AuctionPresenter
@Inject constructor(override val view: AuctionView,
                    private val auctionViewModel: AuctionViewModel,
                    private val activityBridge: ActivityBridge) : BasePresenter() {

    interface ActivityBridge {
        val fragmentManager: FragmentManager
        val lifecycleOwner: LifecycleOwner
        fun showSnackBar(view: View, @StringRes msg: Int)
    }

    var searchView: SearchView? = null
        set(value) {
            field = value
            searchView!!.setQuery(auctionViewModel.searchText, false)
        }

    private val lifecycleOwner: LifecycleOwner
        get() = activityBridge.lifecycleOwner

    private val auctionAdapter: AuctionAdapter
        get() = auctionViewModel.auctionAdapter

    private val disposables = CompositeDisposable()
    private var disposableSaveNote: Disposable? = null
    private var disposableClearNote: Disposable? = null

    @SuppressLint("VisibleForTests")
    override fun initialize(savedInstanceState: Bundle?) {
        auctionViewModel.auctionData.observe(lifecycleOwner, Observer<AuctionData>(this::showAuctions))

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
        searchView?.clearFocus()

        view.showBusy = true
        view.clearAuctions()
        auctionViewModel.loadAuctions(keyword)
    }

    @VisibleForTesting
    fun onLoadMorePages() {
        view.showBusy = true
        auctionViewModel.loadAuctionsNextPage()
    }

    internal fun sortAuctions(sortColumn: EBayModel.SortColumn) {
        view.showBusy = true
        view.clearAuctions()
        auctionViewModel.loadAuctions(sortColumn = sortColumn)
    }

    @SuppressLint("VisibleForTests")
    private fun showDetail(auction: Auction) {
        val note = auctionAdapter.getNote(auction)

        val dialog = AuctionDetailDialog.newInstance(auction, note)

        disposableClearNote?.dispose()
        disposableClearNote = dialog.onClearNote
                .subscribe({ _ -> clearNote(auction, note) },
                        { throwable -> Timber.e(throwable, "clearNote error") })

        disposableSaveNote?.dispose()
        disposableSaveNote = dialog.onSaveNote
                .subscribe({ text -> saveNote(auction, note, text) },
                        { throwable -> Timber.e(throwable, "saveNote error") })

        dialog.show(activityBridge.fragmentManager, "AuctionDetailDialog")
    }

    @VisibleForTesting
    fun saveNote(auction: Auction, note: Note?, text: String) {
        if (note == null) {
            disposables.add(
                    Single.just(Note(auction.itemId, text))
                            .subscribeOn(Schedulers.io())
                            .doOnSuccess { note1 -> auctionViewModel.insertNote(note1) }
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ note1 -> auctionAdapter.addNote(auction, note1) },
                                    { throwable -> Timber.e(throwable, "insertNote error") })
            )
        } else {
            note.noteText = text
            disposables.add(
                    CompletableFromAction({ auctionViewModel.updateNote(note) })
                            .subscribeOn(Schedulers.io())
                            .subscribe({ /* no-op */ },
                                    { throwable -> Timber.e(throwable, "updateNote error") })
            )
        }
    }

    @VisibleForTesting
    fun showAuctions(auctionData: AuctionData?) {
        view.showBusy = false

        if (auctionData?.hasError == false) {
            auctionAdapter.addAuctions(auctionData.auctions, auctionData.notes)
        } else {
            if (searchView?.query?.isNotEmpty() != false) {
                activityBridge.showSnackBar(view, R.string.error_auction_get)
            }
        }

        view.doneLoading(auctionViewModel.hasMoreAuctionPages)

    }

    fun clearNote(auction: Auction, note: Note?) {
        if (note != null) {
            disposables.add(
                    CompletableFromAction({ auctionViewModel.deleteNote(note) })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ auctionAdapter.clearNote(auction) },
                                    { throwable -> Timber.e(throwable, "deleteNote error") })
            )
        }
    }

    override fun cleanup() {

        disposableSaveNote?.dispose()
        disposableClearNote?.dispose()
        disposables.dispose()

        auctionViewModel.auctionData.removeObservers(lifecycleOwner)

        view.cleanup()
    }
}