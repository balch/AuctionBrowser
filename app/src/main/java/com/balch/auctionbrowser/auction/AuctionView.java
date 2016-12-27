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

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.balch.android.app.framework.BaseView;
import com.balch.android.app.framework.domain.EditView;
import com.balch.auctionbrowser.ModelProvider;
import com.balch.auctionbrowser.R;
import com.balch.auctionbrowser.note.Note;

import java.util.List;
import java.util.Map;

public class AuctionView extends LinearLayout
        implements BaseView, AdapterView.OnItemSelectedListener {
    private static final String TAG = EditView.class.getName();

    private ProgressBar progressBar;
    private Spinner sortSpinner;
    private AuctionAdapter auctionAdapter;
    private RecyclerOnScrollListener recyclerOnScrollListener;
    private EditText searchEditText;


    public interface MainViewListener {
        boolean onLoadMore(int currentPage);
        void onChangeSort(int position);
        void onClickNoteButton(Auction auction);
        void onClickAuction(Auction auction);
        void onClickSearch(String keyword);
    }

    protected MainViewListener mainViewListener;

    public AuctionView(Context context) {
        super(context);
    }

    public AuctionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AuctionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void showBusy() {
        this.progressBar.setVisibility(View.VISIBLE);
    }

    public void hideBusy() {
        this.progressBar.setVisibility(View.INVISIBLE);
    }

    public void addAuctions(List<Auction> auctions, Map<Long, Note> notes) {
        this.auctionAdapter.addAuctions(auctions, notes);
    }

    public void clearAuctions() {
        this.auctionAdapter.clearAuctions();
        this.recyclerOnScrollListener.reset();
    }

    public void doneLoading() {
        this.recyclerOnScrollListener.doneLoading();
    }

    public Note getNote(Auction auction) {
        return this.auctionAdapter.getNotes().get(auction.getItemId());
    }

    public void clearNote(Auction auction) {
        this.auctionAdapter.getNotes().remove(auction.getItemId());
        this.auctionAdapter.notifyDataSetChanged();
    }

    public void addNote(Auction auction, Note note) {
        this.auctionAdapter.getNotes().put(auction.getItemId(), note);
        this.auctionAdapter.notifyDataSetChanged();
    }

    public void setSortStrings(int textArrayResId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                textArrayResId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.sortSpinner.setAdapter(adapter);
        this.sortSpinner.setOnItemSelectedListener(this);
    }

    public void setSearchString(String searchString) {
        this.searchEditText.setText(searchString);
    }

    @Override
    public void initializeLayout() {
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setOrientation(VERTICAL);
        inflate(getContext(), R.layout.auction_view, this);
        setupToolbar();

        this.searchEditText = (EditText)findViewById(R.id.auction_view_search_text);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.action_view_recycler);
        recyclerView.setHasFixedSize(true);

        final InputMethodManager inputMethodManager = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        findViewById(R.id.auction_view_search_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainViewListener != null) {
                    mainViewListener.onClickSearch(searchEditText.getText().toString());
                }
                inputMethodManager.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
            }
        });


        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        this.recyclerOnScrollListener = new RecyclerOnScrollListener(layoutManager,
                new RecyclerOnScrollListener.LoadMoreListener() {
                    @Override
                    public boolean onLoadMore(int currentPage) {
                        return (mainViewListener == null) ||
                            mainViewListener.onLoadMore(currentPage);
                    }
                });

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(this.recyclerOnScrollListener);

        this.auctionAdapter = new AuctionAdapter((ModelProvider) getContext().getApplicationContext(),
                new AuctionAdapter.MembersAdapterListener() {
                    @Override
                    public void onClickNoteButton(Auction auction) {
                        if (mainViewListener != null) {
                            mainViewListener.onClickNoteButton(auction);
                        }
                    }

                    @Override
                    public void onClickMember(Auction auction) {
                        if (mainViewListener != null) {
                            mainViewListener.onClickAuction(auction);
                        }
                    }
                });
        recyclerView.setAdapter(this.auctionAdapter);
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar)findViewById(R.id.auction_view_toolbar);
        this.progressBar = (ProgressBar)toolbar.findViewById(R.id.view_auction_toolbar_progress_bar);
        this.sortSpinner = (Spinner)toolbar.findViewById(R.id.view_auction_toolbar_spinner_sort_col);
    }

    public void setMainViewListener(MainViewListener mainViewListener) {
        this.mainViewListener = mainViewListener;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (this.mainViewListener != null) {
            this.mainViewListener.onChangeSort(position);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public static class RecyclerOnScrollListener extends RecyclerView.OnScrollListener {

        private int currentPage = 1;
        private boolean loading = false;
        private boolean hasMore = true;

        private final LinearLayoutManager linearLayoutManager;
        private final LoadMoreListener loadMoreListener;

        public RecyclerOnScrollListener(LinearLayoutManager linearLayoutManager,
                                        LoadMoreListener loadMoreListener) {
            this.linearLayoutManager = linearLayoutManager;
            this.loadMoreListener = loadMoreListener;
        }

        protected void reset() {
            currentPage = 1;
            hasMore = true;
            loading = false;
        }

        protected void doneLoading() {
            loading = false;
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            if (hasMore && !loading) {
                int visibleItemCount = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();
                int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();

                if ((visibleItemCount + firstVisibleItem) >= totalItemCount) {
                    loading = hasMore = loadMoreListener.onLoadMore(++currentPage);
                }
            }
        }

        public interface LoadMoreListener {
            boolean onLoadMore(int currentPage);
        }
    }
}
