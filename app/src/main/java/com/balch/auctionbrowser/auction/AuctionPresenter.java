/*
 * Author: Balch
 * Created: 9/4/14 12:26 AM
 *
 * This file is part of MockTrade.
 *
 * MockTrade is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MockTrade is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MockTrade.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2014
 */

package com.balch.auctionbrowser.auction;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.balch.android.app.framework.BaseAppCompatActivity;
import com.balch.android.app.framework.BasePresenter;
import com.balch.auctionbrowser.AuctionApplication;
import com.balch.auctionbrowser.R;
import com.balch.auctionbrowser.note.Note;
import com.balch.auctionbrowser.note.NoteEditDialog;
import com.balch.auctionbrowser.note.NotesModel;

import java.util.List;
import java.util.Map;

public class AuctionPresenter extends BasePresenter<AuctionApplication>
        implements LoaderManager.LoaderCallbacks<AuctionPresenter.AuctionData> {
    private static final String TAG = AuctionPresenter.class.getSimpleName();

    protected final AuctionView auctionView;
    protected final EBayModel auctionModel;
    protected final NotesModel notesModel;

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


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public AuctionPresenter(AuctionView auctionView, EBayModel auctionModel, NotesModel notesModel) {
        this.auctionView = auctionView;
        this.auctionModel = auctionModel;
        this.notesModel = notesModel;
    }

    @Override
    public void initialize(Bundle savedInstanceState) {
        this.auctionView.setMainViewListener(new AuctionView.MainViewListener() {
            @Override
            public void onLoadMore(int currentPage) {
                boolean hasMore = ((totalPages == -1) ||
                                    (currentPage < totalPages));
                if (isLoadFinished && hasMore) {
                    auctionView.showBusy();
                    AuctionPresenter.this.currentPage = currentPage;
                    loaderManager.restartLoader(AUCTION_LOADER_ID, null, AuctionPresenter.this).forceLoad();
                }
            }

            @Override
            public void onChangeSort(int position) {
                if (isLoadFinished) {
                    sortPosition = position;
                    currentPage = 1;
                    totalPages = -1;

                    auctionView.clearAuctions();
                    loaderManager.restartLoader(AUCTION_LOADER_ID, null, AuctionPresenter.this).forceLoad();
                }
            }

            @Override
            public void onClickNoteButton(final Auction auction) {
                NoteEditDialog dialog = new NoteEditDialog();
                Bundle args = new Bundle();

                final Note note = auctionView.getNote(auction);
                if (note != null) {
                    args.putString(NoteEditDialog.ARG_NOTE, note.getNote());
                    args.putString(NoteEditDialog.ARG_TITLE, auction.getTitle());
                    dialog.setArguments(args);
                    dialog.setNoteEditDialogListener(new NoteEditDialog.NoteEditDialogListener() {
                        @Override
                        public void onSave(String text) {
                            note.setNote(text);
                            notesModel.update(note);
                        }

                        @Override
                        public void onClear() {
                            notesModel.delete(note);
                            auctionView.clearNote(auction);
                        }
                    });
                    dialog.show(((BaseAppCompatActivity) auctionView.getContext()).getSupportFragmentManager(),
                            "NoteEditDialog");
                }
            }

            @Override
            public void onClickAuction(final Auction auction) {
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
                        "NoteDetailDialog");
            }

        });

        this.auctionView.setSortStrings(R.array.auction_sort_col);
        this.auctionView.showBusy();
        this.loaderManager.initLoader(AUCTION_LOADER_ID, null, this).forceLoad();
    }

    @Override
    public Loader<AuctionData> onCreateLoader(int id, Bundle args) {
        return new MemberLoader(this.application, this.currentPage, this.sortColumns[this.sortPosition],
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

    protected static class MemberLoader extends AsyncTaskLoader<AuctionData> {
        protected int currentPage;
        protected EBayModel auctionModel;
        protected NotesModel notesModel;
        protected String sortOrder;

        public MemberLoader(Context context, int currentPage, String sortOrder,
                            EBayModel auctionModel, NotesModel notesModel) {
            super(context);
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
                        "multi-rotor",
                        currentPage  * AUCTION_FETCH_COUNT,
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
