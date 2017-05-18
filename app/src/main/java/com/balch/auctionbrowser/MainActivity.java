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

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
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

    // keep in sync with auction_sort_col string array
    private final String[] sortColumns =
            new String[]{"BestMatch", "EndTimeSoonest", "PricePlusShippingLowest"};

    private AuctionLoader auctionViewModel;

    @VisibleForTesting EBayModel auctionModel;
    @VisibleForTesting NotesModel notesModel;

    // TODO: Serialize this so we can recover on Activity reload
    protected int currentPage = 1;
    protected int sortPosition = 0;
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
        this.view.setAuctionViewListener(this);

        this.view.setSortStrings(R.array.auction_sort_col);
        this.view.showBusy();

        auctionViewModel = ViewModelProviders.of(this).get(AuctionLoader.class);
        auctionViewModel.setAuctionModel(auctionModel);
        auctionViewModel.setNotesModel(notesModel);
        auctionViewModel.getAuctionData().observe(this,
                new Observer<AuctionData>() {
                    @Override
                    public void onChanged(@Nullable AuctionData data) {
                        view.hideBusy();

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

                        view.doneLoading();

                    }
                });
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

    @Override
    public void onChangeSort(int position) {
        sortPosition = position;
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
    public void onClickSearch(String keyword) {
        searchString = keyword;
        currentPage = 1;
        totalPages = -1;

        view.showBusy();
        view.clearAuctions();
        updateView();
    }

    private void showDetail(final Auction auction) {
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

    private void clearNote(Auction auction, Note note) {
        if (note != null) {
            notesModel.delete(note);
            view.clearNote(auction);
        }
    }

    @VisibleForTesting
    void updateView() {
        this.auctionViewModel.update(this.currentPage, this.searchString, this.sortColumns[this.sortPosition]);
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }
}
