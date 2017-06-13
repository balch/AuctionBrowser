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
import android.app.SearchManager
import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.VisibleForTesting
import android.support.design.widget.Snackbar
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import com.balch.auctionbrowser.R.id.*
import com.balch.auctionbrowser.R.menu.options_menu
import com.balch.auctionbrowser.auction.AuctionAdapter
import com.balch.auctionbrowser.auction.AuctionDetailDialog
import com.balch.auctionbrowser.auction.AuctionView
import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.auction.model.EBayModel
import com.balch.auctionbrowser.base.ModelProvider
import com.balch.auctionbrowser.base.PresenterActivity
import com.balch.auctionbrowser.note.Note
import com.balch.auctionbrowser.note.NotesModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class MainActivity : PresenterActivity<AuctionView>(), LifecycleRegistryOwner {

    private val lifecycleRegistry by lazy { LifecycleRegistry(this) }

    lateinit private var auctionViewModel: AuctionViewModel

    lateinit private var searchView: SearchView

    private val disposables = CompositeDisposable()

    private var disposableSaveNote: Disposable? = null
    private var disposableClearNote: Disposable? = null

    override fun createView(): AuctionView {
        return AuctionView(this)
    }

    @SuppressLint("VisibleForTests")
    override fun createModel(modelProvider: ModelProvider) {

        // Note: the ViewModel survives a ConfigChange event and may already be initialized
        auctionViewModel = getAuctionViewModel()
        if (!auctionViewModel.isInitialized) {
            val auctionModel = EBayModel(getString(R.string.ebay_app_id),
                    modelProvider.modelApiFactory.ebayApi)
            val notesModel = NotesModel(modelProvider.database.noteDao())

            auctionViewModel.inject(AuctionAdapter(), auctionModel, notesModel)
        }
    }

    @SuppressLint("VisibleForTests")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = ""
        onCreateInternal(savedInstanceState)
    }

    @VisibleForTesting
    fun onCreateInternal(savedInstanceState: Bundle?) {
        wrap("onCreateInternal") {
            view.auctionViewListener = object : AuctionView.AuctionViewListener {
                override fun onLoadMore(page: Int): Boolean {
                    return onLoadMorePages(page)
                }
            }

            auctionViewModel.auctionData.observe(this,
                    Observer<AuctionData> { auctionData -> showAuctions(auctionData) })

            val auctionAdapter = auctionViewModel.auctionAdapter
            view.setAuctionAdapter(auctionAdapter)

            disposables.add(
                auctionAdapter.onClickAuction
                    .subscribe({ auction -> showDetail(auction) })
            )

            disposables.add(
                auctionAdapter.onClickNote
                    .subscribe({ auction -> showDetail(auction) })
            )

            // Get the intent, verify the action and get the query
            handleIntent()
        }
    }

    override fun onNewIntent(intent: Intent) {
        wrap("OnNewIntent") {
            handleIntent(intent)
        }
    }

    @VisibleForTesting
    internal fun handleIntent(): Boolean {
        return handleIntent(intent)
    }

    private fun handleIntent(intent: Intent): Boolean {
        var handled = false
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            doSearch(query)

            handled = true
        }

        return handled
    }

    override fun onDestroy() {
        wrap("OnNewIntent") {
            auctionViewModel.auctionData.removeObservers(this)
            disposables.dispose()
        }
        super.onDestroy()
    }

    @VisibleForTesting
    internal fun onLoadMorePages(page: Int): Boolean {
        val hasMore = auctionViewModel.hasMoreAuctionPages(page.toLong())
        if (hasMore) {
            view.showBusy()
            auctionViewModel.loadAuctionsNextPage()
        }
        return hasMore
    }

    internal fun sortAuctions(sortColumn: EBayModel.SortColumn) {
        view.showBusy()
        view.clearAuctions()
        auctionViewModel.loadAuctions(sortColumn = sortColumn)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        wrap("onCreateOptionsMenu") {
            // Inflate the options menu from XML
            menuInflater.inflate(options_menu, menu)

            val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
            searchView = menu.findItem(menu_search).actionView as SearchView
            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            searchView.setIconifiedByDefault(false)
            searchView.setQuery(auctionViewModel.searchText, false)

            menu.findItem(menu_sort_best_match).isChecked = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var handled = false
        wrap("onOptionsItemSelected") {
            // Handle item selection
            when (item.itemId) {
                menu_sort_best_match -> {
                    sortAuctions(EBayModel.SortColumn.BEST_MATCH)
                    item.isChecked = true
                    handled =true
                }
                menu_sort_ending_soonest -> {
                    sortAuctions(EBayModel.SortColumn.ENDING_SOONEST)
                    item.isChecked = true
                    handled =true
                }
                menu_sort_lowest_price -> {
                    sortAuctions(EBayModel.SortColumn.LOWEST_PRICE)
                    item.isChecked = true
                    handled =true
                }
            }
        }

        return handled || super.onOptionsItemSelected(item)
    }

    private fun doSearch(keyword: String) {
        searchView.clearFocus()

        view.showBusy()
        view.clearAuctions()
        auctionViewModel.loadAuctions(keyword)
    }

    override fun getLifecycle(): LifecycleRegistry {
        return lifecycleRegistry
    }

    @VisibleForTesting
    internal fun showDetail(auction: Auction) {
        val note = view.getNote(auction)
        val dialog = AuctionDetailDialog(auction, note)

        if (disposableClearNote != null) disposables.remove(disposableClearNote)
        disposableClearNote = dialog.onClearNote
                .subscribe { _ -> clearNote(auction, note) }
        disposables.add(disposableClearNote)

        if (disposableSaveNote != null) disposables.remove(disposableSaveNote)
        disposableSaveNote = dialog.onSaveNote
                .subscribe { text -> saveNote(auction, note, text) }
        disposables.add(disposableSaveNote)

        dialog.show(supportFragmentManager, "AuctionDetailDialog")
    }

    @VisibleForTesting
    internal fun saveNote(auction: Auction, note: Note?, text: String) {
        if (note == null) {
            Single.just(Note(auction.itemId, text))
                    .subscribeOn(Schedulers.io())
                    .doOnSuccess { note1 ->  auctionViewModel.insertNote(note1) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { note1 ->
                        if (!isFinishing) {
                            view.addNote(auction, note1)
                        }}
        } else {
            note.noteText = text
            Single.just(note)
                    .subscribeOn(Schedulers.io())
                    .subscribe { note1 -> auctionViewModel.updateNote(note1) }
        }
    }

    @VisibleForTesting
    internal fun showAuctions(auctionData: AuctionData?) {
        view.hideBusy()

        if (auctionData?.hasError == false) {
            view.addAuctions(auctionData.auctions, auctionData.notes)
        } else {
            if (searchView.query.isNotEmpty()) {
                getSnackbar(view, getString(R.string.error_auction_get), Snackbar.LENGTH_LONG).show()
            }
        }

        view.doneLoading()
    }

    @VisibleForTesting
    internal fun clearNote(auction: Auction, note: Note?) {
        if (note != null) {
            Single.just(true)
                    .subscribeOn(Schedulers.io())
                    .doOnSuccess { _ -> auctionViewModel.deleteNote(note) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { _ ->
                        if (!isFinishing) {
                            view.clearNote(auction)
                        }
                    }
        }
    }

    @VisibleForTesting
    internal fun getAuctionViewModel(): AuctionViewModel {
        return ViewModelProviders.of(this).get(AuctionViewModel::class.java)
    }

}
