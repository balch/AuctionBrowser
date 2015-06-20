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

public class AuctionPresenter extends BasePresenter<AuctionApplication> implements LoaderManager.LoaderCallbacks<AuctionPresenter.MemberData> {
    private static final String TAG = AuctionPresenter.class.getSimpleName();

    protected final AuctionView auctionView;
    protected final EBayModel auctionModel;
    protected final NotesModel notesModel;

    protected static final int MEMBER_LOADER_ID = 0;

    protected static final int MEMBER_FETCH_COUNT = 10;

    // TODO: Serialize this so we can recover on Activity reload
    protected int currentPage = 1;
    protected int sortPosition = 0;
    protected boolean hasMoreMembers;
    protected boolean isLoadFinished = false;

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
                if (isLoadFinished && !hasMoreMembers) {
                    auctionView.showBusy();
                    AuctionPresenter.this.currentPage = currentPage;
                    loaderManager.restartLoader(MEMBER_LOADER_ID, null, AuctionPresenter.this).forceLoad();
                }
            }

            @Override
            public void onChangeSort(int position) {
                if (isLoadFinished) {
                    sortPosition = position;
                    currentPage = 1;
                    hasMoreMembers = true;

                    auctionView.clearMembers();
                    loaderManager.restartLoader(MEMBER_LOADER_ID, null, AuctionPresenter.this).forceLoad();
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
            public void onClickMember(final Auction auction) {
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

        this.auctionView.setSortStrings(R.array.member_sort_col);
        this.auctionView.showBusy();
        this.loaderManager.initLoader(MEMBER_LOADER_ID, null, this).forceLoad();
    }

    @Override
    public Loader<MemberData> onCreateLoader(int id, Bundle args) {
        return new MemberLoader(this.application, this.currentPage, this.sortPosition,
                this.auctionModel, this.notesModel);
    }

    @Override
    public void onLoadFinished(Loader<MemberData> loader, MemberData data) {
        auctionView.hideBusy();

        if (data.auctions != null) {
            hasMoreMembers = data.auctions.size() < MEMBER_FETCH_COUNT;
            auctionView.addMembers(data.auctions, data.notes);
        } else {
            Toast.makeText(getApplication(), R.string.error_auction_get, Toast.LENGTH_LONG).show();
        }
        
        isLoadFinished = true;
    }

    @Override
    public void onLoaderReset(Loader<MemberData> loader) {

    }

    protected static class MemberData {
        protected List<Auction> auctions;
        protected Map<Long, Note> notes;
        protected long totalAuctions;
    }

    protected static class MemberLoader extends AsyncTaskLoader<MemberData> {
        protected int currentPage;
        protected EBayModel auctionModel;
        protected NotesModel notesModel;
        protected int sortPosition;

        public MemberLoader(Context context, int currentPage, int sortPosition,
                            EBayModel auctionModel, NotesModel notesModel) {
            super(context);
            this.currentPage = currentPage;
            this.sortPosition = sortPosition;
            this.auctionModel = auctionModel;
            this.notesModel = notesModel;
        }

        @Override
        public MemberData loadInBackground() {
            MemberData memberData = new MemberData();
            try {

                EBayModel.AuctionInfo info = this.auctionModel.getAuctions(
                        "multi-rotor",
                        currentPage  * MEMBER_FETCH_COUNT,
                        MEMBER_FETCH_COUNT,
                        (sortPosition == 0) ? "userName" : "birthday",
                        (sortPosition == 0) ? EBayModel.SortDirection.ASC : EBayModel.SortDirection.DESC);


                memberData.totalAuctions = info.totalCount;
                memberData.auctions = info.auctions;
                if (memberData.auctions != null) {
                    memberData.notes = this.notesModel.getNotes(memberData.auctions);
                }
            } catch (Exception ex) {
                Log.e(TAG, "error getting auctions", ex);
            }

            return memberData;
        }
    }

}
