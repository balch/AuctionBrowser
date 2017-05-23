/*
 *  Author: Balch
 *  Created: 6/14/15 11:21 AM
 *
 *  This file is part of BB_Challenge.
 *
 *  BB_Challenge is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  BB_Challenge is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MockTrade.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Copyright (C) 2015
 *
 */

package com.balch.auctionbrowser;

import android.app.SearchManager;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.balch.android.app.framework.PresenterActivity;
import com.balch.auctionbrowser.auction.AuctionAdapter;
import com.balch.auctionbrowser.auction.AuctionDetailDialog;
import com.balch.auctionbrowser.auction.AuctionView;
import com.balch.auctionbrowser.auction.model.Auction;
import com.balch.auctionbrowser.auction.model.EBayModel;
import com.balch.auctionbrowser.auction.model.EbayApi;
import com.balch.auctionbrowser.note.Note;
import com.balch.auctionbrowser.note.NotesModel;

import io.reactivex.disposables.Disposable;

public class MainActivity extends PresenterActivity<AuctionView, AuctionModelProvider>
        implements AuctionView.AuctionViewListener, LifecycleRegistryOwner {

    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    @VisibleForTesting AuctionViewModel auctionViewModel;

    SearchView searchView;

    Disposable disposableClickAuction = null;
    Disposable disposableClickNote = null;

    @Override
    public AuctionView createView() {
        return new AuctionView(this);
    }

    @Override
    protected void createModel(AuctionModelProvider modelProvider) {

        auctionViewModel = getAuctionViewModel();
        if (!auctionViewModel.isInitialized()) {
            EBayModel auctionModel = new EBayModel(getString(R.string.ebay_app_id),
                    modelProvider.getModelApiFactory().getModelApi(EbayApi.class));
            NotesModel notesModel = new NotesModel(modelProvider.getSqlConnection());

            auctionViewModel.setAuctionModel(auctionModel);
            auctionViewModel.setNotesModel(notesModel);
            auctionViewModel.setAuctionAdapter(new AuctionAdapter());
        }
    }

    @Override
    public void onCreateBase(Bundle bundle) {
        view.setAuctionViewListener(this);
        auctionViewModel.getAuctionData().observe(this, auctionDataObserver);

        AuctionAdapter auctionAdapter = auctionViewModel.getAuctionAdapter();
        view.setAuctionAdapter(auctionAdapter);

        disposableClickAuction = auctionAdapter.getClickAuctionObservable()
                .subscribe(this::showDetail);

        disposableClickNote = auctionAdapter.getClickNoteObservable()
                .subscribe(this::showDetail);

        // Get the intent, verify the action and get the query
        handleIntent();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    @VisibleForTesting
    boolean handleIntent() {
        return handleIntent(getIntent());
    }

    private boolean handleIntent(Intent intent) {
        boolean handled = false;
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doSearch(query);

            handled = true;
        }

        return handled;
    }

    @Override
    public void onDestroyBase() {
        auctionViewModel.getAuctionData().removeObserver(auctionDataObserver);
        if (disposableClickNote != null) {
            disposableClickNote.dispose();
        }
        if (disposableClickAuction != null) {
            disposableClickAuction.dispose();
        }
    }

    @Override
    public boolean onLoadMore(int page) {
        boolean hasMore = auctionViewModel.hasMoreAuctionPages(page);
        if (hasMore) {
            view.showBusy();
            auctionViewModel.loadAuctionsNextPage();
        }
        return hasMore;
    }

    void sortAuctions(EBayModel.SortColumn sortColumn) {
        view.showBusy();
        view.clearAuctions();
        auctionViewModel.loadAuctions(sortColumn);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.setQuery(auctionViewModel.getSearchText(), false);

        MenuItem bestMatchItem = menu.findItem(R.id.menu_sort_best_match);
        bestMatchItem.setChecked(true);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_sort_best_match:
                sortAuctions(EBayModel.SortColumn.BEST_MATCH);
                item.setChecked(true);
                return true;
            case R.id.menu_sort_ending_soonest:
                sortAuctions(EBayModel.SortColumn.ENDING_SOONEST);
                item.setChecked(true);
                return true;
            case R.id.menu_sort_lowest_price:
                sortAuctions(EBayModel.SortColumn.LOWEST_PRICE);
                item.setChecked(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void doSearch(String keyword) {
        searchView.clearFocus();

        view.showBusy();
        view.clearAuctions();
        auctionViewModel.loadAuctions(keyword);
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }

    @VisibleForTesting
    void showDetail(final Auction auction) {
        AuctionDetailDialog dialog = new AuctionDetailDialog();
        Bundle args = new Bundle();

        final Note note = view.getNote(auction);
        if (note != null) {
            args.putString(AuctionDetailDialog.ARG_NOTE, note.getNote());
        }
        args.putSerializable(AuctionDetailDialog.ARG_AUCTION, auction);
        dialog.setArguments(args);
        dialog.setNoteDetailDialogListener(new AuctionDetailDialog.NoteDetailDialogListener() {
            @Override
            public void onSave(String text) {
                saveNote(auction, note, text);
            }

            @Override
            public void onClear() {
                clearNote(auction, note);
            }
        });
        dialog.show(getSupportFragmentManager(), "AuctionDetailDialog");
    }

    @VisibleForTesting
    void saveNote(Auction auction, Note note, String text) {
        if (note == null) {
            Note note1 = new Note();
            note1.setNote(text);
            note1.setItemId(auction.getItemId());
            auctionViewModel.insertNote(note1);
            view.addNote(auction, note1);
        } else {
            note.setNote(text);
            auctionViewModel.updateNote(note);
        }
    }

    @VisibleForTesting
    void clearNote(Auction auction, Note note) {
        if (note != null) {
            auctionViewModel.deleteNote(note);
            view.clearNote(auction);
        }
    }

    @VisibleForTesting
    Observer<AuctionData> auctionDataObserver = auctionData -> {
        view.hideBusy();

        if (auctionData != null) {
            if (auctionData.getAuctions() != null) {
                view.addAuctions(auctionData.getAuctions(), auctionData.getNotes());
            } else {
                if (searchView.getQuery().length() > 0) {
                    Toast.makeText(getApplication(), R.string.error_auction_get, Toast.LENGTH_LONG).show();
                }
            }
        }

        view.doneLoading();
    };

    @VisibleForTesting
    AuctionViewModel getAuctionViewModel() {
        return ViewModelProviders.of(this).get(AuctionViewModel.class);
    }

}
