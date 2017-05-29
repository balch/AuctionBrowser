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

import android.app.SearchManager
import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.VisibleForTesting
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import com.balch.auctionbrowser.R.id.*
import com.balch.auctionbrowser.R.menu.options_menu
import com.balch.auctionbrowser.auction.AuctionAdapter
import com.balch.auctionbrowser.auction.AuctionDetailDialog
import com.balch.auctionbrowser.auction.AuctionView
import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.auction.model.EBayApi
import com.balch.auctionbrowser.auction.model.EBayModel
import com.balch.auctionbrowser.note.Note
import com.balch.auctionbrowser.note.NotesModel
import io.reactivex.disposables.Disposable

open class MainActivity : BaseActivity<AuctionView, AuctionModelProvider>(),
        AuctionView.AuctionViewListener, LifecycleRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)

    @VisibleForTesting
    lateinit private var auctionViewModel: AuctionViewModel

    lateinit private var searchView: SearchView

    private var disposableClickAuction: Disposable? = null
    private var disposableClickNote: Disposable? = null

    private var disposableSaveNote: Disposable? = null
    private var disposableClearNote: Disposable? = null

    override fun createView(): AuctionView {
        return AuctionView(this)
    }

    override fun createModel(modelProvider: AuctionModelProvider) {

        auctionViewModel = getAuctionViewModel()
        if (!auctionViewModel.isInitialized) {
            val auctionModel = EBayModel(getString(R.string.ebay_app_id),
                    modelProvider.modelApiFactory.getModelApi(EBayApi::class.java)!!)
            val notesModel = NotesModel(modelProvider.sqlConnection)

            auctionViewModel.initialize(AuctionAdapter(), auctionModel, notesModel)
        }
    }

    override fun onCreate(bundle: Bundle?) {

        super.onCreate(bundle)
        trace("onCreate") {
            view.auctionViewListener = this
            auctionViewModel.auctionData.observe(this, auctionDataObserver)

            val auctionAdapter = auctionViewModel.auctionAdapter
            view.setAuctionAdapter(auctionAdapter)

            disposableClickAuction = auctionAdapter.onClickAuction
                    .subscribe({ auction -> showDetail(auction) })

            disposableClickNote = auctionAdapter.onClickNote
                    .subscribe({ auction -> showDetail(auction) })

            // Get the intent, verify the action and get the query
            handleIntent()
        }
    }

    override fun onNewIntent(intent: Intent) {
        trace("OnNewIntent") {
            handleIntent(intent)
        }
    }

    @VisibleForTesting
    internal fun handleIntent(): Boolean {
        return handleIntent(getIntent())
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
        trace("OnNewIntent") {
            auctionViewModel.auctionData.removeObserver(auctionDataObserver)
            disposableClickNote?.dispose()
            disposableClickAuction?.dispose()

            disposeClearNoteObserver()
            disposeSaveNoteObserver()
        }
        super.onDestroy()
    }

    private fun disposeClearNoteObserver() {
        disposableClearNote?.dispose()
        disposableClearNote = null
    }

    private fun disposeSaveNoteObserver() {
        disposableSaveNote?.dispose()
        disposableSaveNote = null
    }

    override fun onLoadMore(page: Int): Boolean {
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
        auctionViewModel.loadAuctions(sortColumn)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        trace("onCreateOptionsMenu") {
            // Inflate the options menu from XML
            val inflater = getMenuInflater()
            inflater.inflate(options_menu, menu)

            val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
            searchView = menu.findItem(menu_search).actionView as SearchView
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()))
            searchView.setIconifiedByDefault(false)
            searchView.setQuery(auctionViewModel.searchText, false)

            menu.findItem(menu_sort_best_match).isChecked = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var handled = false
        trace("onOptionsItemSelected") {
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

    fun doSearch(keyword: String) {
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

        disposeClearNoteObserver()
        disposableClearNote = dialog.onClearNote
                .subscribe { _ -> clearNote(auction, note) }

        disposeSaveNoteObserver()
        disposableSaveNote = dialog.onSaveNote
                .subscribe { text -> saveNote(auction, note, text) }

        dialog.show(getSupportFragmentManager(), "AuctionDetailDialog")
    }

    @VisibleForTesting
    internal fun saveNote(auction: Auction, note: Note?, text: String) {
        if (note == null) {
            val note1 = Note(auction.itemId, text)
            auctionViewModel.insertNote(note1)
            view.addNote(auction, note1)
        } else {
            note.note = text
            auctionViewModel.updateNote(note)
        }
    }

    @VisibleForTesting
    internal fun clearNote(auction: Auction, note: Note?) {
        if (note != null) {
            auctionViewModel.deleteNote(note)
            view.clearNote(auction)
        }
    }

    @VisibleForTesting
    internal val auctionDataObserver = Observer<AuctionData> {
        auctionData: AuctionData? ->
            view.hideBusy()

            if (auctionData?.hasError == false) {
                view.addAuctions(auctionData.auctions, auctionData.notes)
            } else {
                if (searchView.query.isNotEmpty()) {
                    Toast.makeText(getApplication(), R.string.error_auction_get, Toast.LENGTH_LONG).show()
                }
            }

            view.doneLoading()
        }

    @VisibleForTesting
    internal fun getAuctionViewModel(): AuctionViewModel {
        return ViewModelProviders.of(this).get(AuctionViewModel::class.java)
    }

}
