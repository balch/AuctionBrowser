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
import android.os.Bundle
import android.widget.SearchView
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.auction.model.EBayRepository
import com.balch.auctionbrowser.base.BasePresenter
import com.balch.auctionbrowser.base.NetworkState
import com.balch.auctionbrowser.dagger.ActivityScope
import com.balch.auctionbrowser.note.Note
import com.balch.auctionbrowser.note.NotesRepository
import com.uber.autodispose.lifecycle.LifecycleScopeProvider
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

@ActivityScope
class AuctionPresenter
@Inject constructor(override val view: AuctionView,
                    private val auctionViewModel: AuctionViewModel,
                    private val notesRepository: NotesRepository,
                    private val fragmentManager: FragmentManager,
                    private val lifecycleOwner: LifecycleOwner,
                    private val auctionAdapter: AuctionAdapter,
                    private val scope: LifecycleScopeProvider<*>
                    ) : BasePresenter() {

    var searchView: SearchView? = null
        set(value) {
            field = value
            searchView!!.setQuery(auctionViewModel.searchText, false)
        }

    @SuppressLint("VisibleForTests")
    override fun initialize(savedInstanceState: Bundle?) {

        view.setAuctionAdapter(auctionAdapter)

        auctionViewModel.auctionData.observe(lifecycleOwner, Observer<PagedList<Auction>> {
            auctionAdapter.submitList(it)
        })

        auctionViewModel.networkState.observe(lifecycleOwner, Observer<NetworkState> {
            auctionAdapter.setNetworkState(it)
        })

        auctionAdapter.onClickAuction
                .autoDisposable(scope)
                .subscribe({ auction -> showDetail(auction) },
                        { throwable -> Timber.e(throwable, "onClickAuction error") })

        auctionAdapter.onClickNote
                .autoDisposable(scope)
                .subscribe({ auction -> showDetail(auction) },
                        { throwable -> Timber.e(throwable, "onClickNote error") })
    }

    fun doSearch(keyword: String) {
        searchView?.clearFocus()

        auctionViewModel.loadAuctions(keyword, auctionViewModel.sortColumn)
    }

    internal fun sortAuctions(sortColumn: EBayRepository.SortColumn) {
        auctionViewModel.loadAuctions(auctionViewModel.searchText, sortColumn)
    }

    @SuppressLint("VisibleForTests")
    private fun showDetail(auction: Auction) {
        val note = auction.note

        val dialog = AuctionDetailDialog.newInstance(auction, note)

        dialog.onClearNote
                .autoDisposable(scope)
                .subscribe({ clearNote(auction, note) },
                        { throwable -> Timber.e(throwable, "clearNote error") })

        dialog.onSaveNote
                .autoDisposable(scope)
                .subscribe({ text -> saveNote(auction, note, text) },
                        { throwable -> Timber.e(throwable, "saveNote error") })

        dialog.show(fragmentManager, "AuctionDetailDialog")
    }

    @VisibleForTesting
    fun saveNote(auction: Auction, note: Note?, text: String) {
        if (note == null) {
            val newNote = Note(auction.itemId, text)
            notesRepository.insert(newNote)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .autoDisposable(scope)
                .subscribe({
                    auction.note = newNote
                    auctionAdapter.notifyDataSetChanged()},
                        { throwable -> Timber.e(throwable, "insertNote error") })
        } else {
            note.noteText = text
            notesRepository.update(note)
                .subscribeOn(Schedulers.io())
                .autoDisposable(scope)
                .subscribe({ /* no-op */ },
                        { throwable -> Timber.e(throwable, "updateNote error") })
        }
    }


    fun clearNote(auction: Auction, note: Note?) {
        if (note != null) {
            notesRepository.delete(note)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .autoDisposable(scope)
                    .subscribe({ auction.note = null
                                 auctionAdapter.notifyDataSetChanged()},
                            { throwable -> Timber.e(throwable, "deleteNote error") })
        }
    }

    override fun cleanup() {
        view.cleanup()
    }
}