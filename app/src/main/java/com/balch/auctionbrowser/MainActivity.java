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
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.balch.android.app.framework.PresenterActivity;
import com.balch.auctionbrowser.auction.Auction;
import com.balch.auctionbrowser.auction.AuctionDetailDialog;
import com.balch.auctionbrowser.auction.AuctionView;
import com.balch.auctionbrowser.auction.EBayModel;
import com.balch.auctionbrowser.note.Note;
import com.balch.auctionbrowser.note.NotesModel;

public class MainActivity extends PresenterActivity<AuctionView, AuctionModelProvider>
        implements AuctionView.AuctionViewListener, LifecycleRegistryOwner {
    private static final String TAG = MainActivity.class.getSimpleName();

    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    private AuctionLoader auctionViewModel;

    @VisibleForTesting EBayModel auctionModel;
    @VisibleForTesting NotesModel notesModel;

    // TODO: Serialize this so we can recover on Activity reload
    protected int currentPage = 1;
    protected EBayModel.SortColumn sortColumn = EBayModel.SortColumn.BEST_MATCH;
    protected String searchString = "";
    @VisibleForTesting long totalPages = -1;

    @Override
    public AuctionView createView() {
        return new AuctionView(this);
    }

    @Override
    protected void createModel(AuctionModelProvider modelProvider) {
        auctionModel = new EBayModel(getString(R.string.ebay_app_id),
                modelProvider.getNetworkRequest());
        notesModel = new NotesModel(modelProvider.getSqlConnection());
    }

    @Override
    public void onCreateBase(Bundle bundle) {
        view.setAuctionViewListener(this);

        setupAuctionViewModel();

        // Get the intent, verify the action and get the query
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doSearch(query);
        }
    }

    @Override
    public void onDestroyBase() {
        auctionViewModel.getAuctionData().removeObserver(auctionDataObserver);
    }

    @Override
    public boolean onLoadMore(int currentPage) {
        boolean hasMore = ((totalPages == -1) || (currentPage < totalPages));
        if (hasMore) {
            view.showBusy();
            MainActivity.this.currentPage = currentPage;
            updateView();
        }
        return hasMore;
    }

    void doSort(EBayModel.SortColumn sortColumn) {
        this.sortColumn = sortColumn;
        currentPage = 1;
        totalPages = -1;

        view.showBusy();
        view.clearAuctions();
        updateView();
    }

    @Override
    public void onClickNoteButton(Auction auction) {
        showDetail(auction);
    }

    @Override
    public void onClickAuction(Auction auction) {
        showDetail(auction);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);

        MenuItem bestMatchItem = menu.findItem(R.id.menu_sort_best_match);
        bestMatchItem.setChecked(true);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_sort_best_match:
                doSort(EBayModel.SortColumn.BEST_MATCH);
                item.setChecked(true);
                return true;
            case R.id.menu_sort_ending_soonest:
                doSort(EBayModel.SortColumn.ENDING_SOONEST);
                item.setChecked(true);
                return true;
            case R.id.menu_sort_lowest_price:
                doSort(EBayModel.SortColumn.LOWEST_PRICE);
                item.setChecked(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void doSearch(String keyword) {
        searchString = keyword;
        currentPage = 1;
        totalPages = -1;

        view.showBusy();
        view.clearAuctions();
        updateView();
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
            notesModel.insert(note1);
            view.addNote(auction, note1);
        } else {
            note.setNote(text);
            notesModel.update(note);
        }
    }

    @VisibleForTesting
    void clearNote(Auction auction, Note note) {
        if (note != null) {
            notesModel.delete(note);
            view.clearNote(auction);
        }
    }

    @VisibleForTesting
    void setupAuctionViewModel() {
        auctionViewModel = ViewModelProviders.of(this).get(AuctionLoader.class);
        auctionViewModel.setAuctionModel(auctionModel);
        auctionViewModel.setNotesModel(notesModel);
        auctionViewModel.getAuctionData().observe(this, auctionDataObserver);
    }

    @VisibleForTesting
    Observer<AuctionData> auctionDataObserver = new Observer<AuctionData>() {
        @Override
        public void onChanged(@Nullable AuctionData data) {
            view.hideBusy();

            if (data != null) {
                if (data.getAuctions() != null) {
                    if (totalPages == -1) {
                        totalPages = data.getTotalPages();
                    }
                    view.addAuctions(data.getAuctions(), data.getNotes());
                } else {
                    if (!TextUtils.isEmpty(searchString)) {
                        Toast.makeText(getApplication(), R.string.error_auction_get, Toast.LENGTH_LONG).show();
                    }
                }
            }

            view.doneLoading();
        }
    };

    @VisibleForTesting
    void updateView() {
        auctionViewModel.update(currentPage, searchString, sortColumn);
    }
}
