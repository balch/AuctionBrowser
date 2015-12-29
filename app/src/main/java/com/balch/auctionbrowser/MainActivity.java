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

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.widget.Toast;

import com.balch.android.app.framework.BaseAppCompatActivity;
import com.balch.auctionbrowser.auction.Auction;
import com.balch.auctionbrowser.auction.AuctionDetailDialog;
import com.balch.auctionbrowser.auction.AuctionView;
import com.balch.auctionbrowser.auction.EBayModel;
import com.balch.auctionbrowser.note.Note;
import com.balch.auctionbrowser.note.NotesModel;

import java.util.List;
import java.util.Map;


public class MainActivity extends BaseAppCompatActivity<AuctionView>
        implements LoaderManager.LoaderCallbacks<MainActivity.AuctionData>{
    private static final String TAG = MainActivity.class.getSimpleName();

    protected AuctionView auctionView;
    protected EBayModel auctionModel;
    protected NotesModel notesModel;

    protected static final int AUCTION_LOADER_ID = 0;
    protected static final int AUCTION_FETCH_COUNT = 30;

    protected boolean isLoadFinished = false;

    // keep in sync with auction_sort_col string array
    private final String[] sortColumns =
            new String[]{"BestMatch", "EndTimeSoonest", "PricePlusShippingLowest"};

    // TODO: Serialize this so we can recover on Activity reload
    protected int currentPage = 1;
    protected int sortPosition = 0;
    protected long totalPages = -1;
    protected String searchString = "";

    @Override
    protected void onCreateBase(Bundle bundle) {
        ModelProvider modelProvider = (ModelProvider)getApplication();
        auctionModel = new EBayModel(getString(R.string.ebay_app_id_production), modelProvider);
        notesModel = new NotesModel(modelProvider);

        this.auctionView.setMainViewListener(new AuctionView.MainViewListener() {
            @Override
            public boolean onLoadMore(int currentPage) {
                boolean hasMore = ((totalPages == -1) ||
                        (currentPage < totalPages));
                if (isLoadFinished && hasMore) {
                    auctionView.showBusy();
                    MainActivity.this.currentPage = currentPage;
                    getSupportLoaderManager().restartLoader(AUCTION_LOADER_ID, null, MainActivity.this).forceLoad();
                }
                return hasMore;
            }

            @Override
            public void onChangeSort(int position) {
                if (isLoadFinished) {
                    sortPosition = position;
                    currentPage = 1;
                    totalPages = -1;

                    auctionView.showBusy();
                    auctionView.clearAuctions();
                    getSupportLoaderManager().restartLoader(AUCTION_LOADER_ID, null, MainActivity.this).forceLoad();
                }
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
                if (isLoadFinished) {
                    searchString = keyword;
                    currentPage = 1;
                    totalPages = -1;

                    auctionView.showBusy();
                    auctionView.clearAuctions();
                    getSupportLoaderManager().restartLoader(AUCTION_LOADER_ID, null, MainActivity.this).forceLoad();
                }
            }
        });

        this.auctionView.setSortStrings(R.array.auction_sort_col);
        this.auctionView.showBusy();
        this.getSupportLoaderManager().initLoader(AUCTION_LOADER_ID, null, this).forceLoad();

    }

    @Override
    protected AuctionView createView() {
        auctionView = new AuctionView(this);
        return auctionView;
    }

    private void showDetail(final Auction auction) {
        AuctionDetailDialog dialog = new AuctionDetailDialog();
        Bundle args = new Bundle();

        final Note note = auctionView.getNote(auction);
        if (note != null) {
            args.putString(AuctionDetailDialog.ARG_NOTE, note.getNote());
        }
        args.putSerializable(AuctionDetailDialog.ARG_AUCTION, auction);
        dialog.setArguments(args);
        dialog.setNoteDetailDialogListener(new AuctionDetailDialog.NoteDetailDialogListener() {
            @Override
            public void onSave(String text) {
                if (note == null) {
                    Note note1 = new Note();
                    note1.setNote(text);
                    note1.setItemId(auction.getItemId());
                    notesModel.insert(note1);
                    auctionView.addNote(auction, note1);
                } else {
                    note.setNote(text);
                    notesModel.update(note);
                }
            }

            @Override
            public void onClear() {
                if (note != null) {
                    notesModel.delete(note);
                    auctionView.clearNote(auction);
                }
            }
        });
        dialog.show(((BaseAppCompatActivity) auctionView.getContext()).getSupportFragmentManager(),
                "AuctionDetailDialog");
    }

    @Override
    public Loader<AuctionData> onCreateLoader(int id, Bundle args) {
        return new AuctionLoader(this, this.searchString,
                this.currentPage, this.sortColumns[this.sortPosition],
                this.auctionModel, this.notesModel);
    }

    @Override
    public void onLoadFinished(Loader<AuctionData> loader, AuctionData data) {
        auctionView.hideBusy();

        if (data.auctions != null) {
            if (totalPages == -1) {
                totalPages = data.totalPages;
            }
            auctionView.addAuctions(data.auctions, data.notes);
        } else {
            Toast.makeText(getApplication(), R.string.error_auction_get, Toast.LENGTH_LONG).show();
        }

        auctionView.doneLoading();
        isLoadFinished = true;
    }

    @Override
    public void onLoaderReset(Loader<AuctionData> loader) {

    }

    protected static class AuctionData {
        protected List<Auction> auctions;
        protected Map<Long, Note> notes;
        protected int totalPages;
    }

    protected static class AuctionLoader extends AsyncTaskLoader<AuctionData> {
        protected int currentPage;
        protected EBayModel auctionModel;
        protected NotesModel notesModel;
        protected String searchText;
        protected String sortOrder;

        public AuctionLoader(Context context, String searchText,
                             int currentPage, String sortOrder,
                             EBayModel auctionModel, NotesModel notesModel) {
            super(context);
            this.searchText = searchText;
            this.currentPage = currentPage;
            this.sortOrder = sortOrder;
            this.auctionModel = auctionModel;
            this.notesModel = notesModel;
        }

        @Override
        public AuctionData loadInBackground() {
            AuctionData auctionData = new AuctionData();
            try {

                EBayModel.AuctionInfo info = this.auctionModel.getAuctions(
                        searchText,
                        currentPage,
                        AUCTION_FETCH_COUNT,
                        sortOrder);

                auctionData.totalPages = info.totalPages;
                auctionData.auctions = info.auctions;
                if (auctionData.auctions != null) {
                    auctionData.notes = this.notesModel.getNotes(auctionData.auctions);
                }
            } catch (Exception ex) {
                Log.e(TAG, "error getting auctions", ex);
            }

            return auctionData;
        }
    }

}
