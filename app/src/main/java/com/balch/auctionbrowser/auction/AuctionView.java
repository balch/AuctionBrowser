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
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.balch.android.app.framework.BaseView;
import com.balch.auctionbrowser.R;
import com.balch.auctionbrowser.auction.model.Auction;
import com.balch.auctionbrowser.note.Note;

import java.util.List;
import java.util.Map;

public class AuctionView extends FrameLayout implements BaseView {
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private AuctionAdapter auctionAdapter;
    private RecyclerOnScrollListener recyclerOnScrollListener;

    public interface AuctionViewListener {
        boolean onLoadMore(int page);
    }

    protected AuctionViewListener auctionViewListener;

    public AuctionView(Context context) {
        super(context);
        initializeLayout();
    }

    public AuctionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeLayout();
    }

    public AuctionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initializeLayout();
    }

    private void initializeLayout() {
        inflate(getContext(), R.layout.auction_view, this);

        progressBar = (ProgressBar) findViewById(R.id.auction_view_progress_bar);

        recyclerView = (RecyclerView) findViewById(R.id.action_view_recycler);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        this.recyclerOnScrollListener = new RecyclerOnScrollListener(layoutManager,
                page -> (auctionViewListener == null) ||
                        auctionViewListener.onLoadMore(page));

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(this.recyclerOnScrollListener);
    }

    public void setAuctionAdapter(AuctionAdapter auctionAdapter) {
        this.auctionAdapter = auctionAdapter;
        recyclerView.setAdapter(this.auctionAdapter);
    }

    public void setAuctionViewListener(AuctionViewListener auctionViewListener) {
        this.auctionViewListener = auctionViewListener;
    }

    public void showBusy() {
        this.progressBar.setVisibility(View.VISIBLE);
    }

    public void hideBusy() {
        this.progressBar.setVisibility(View.GONE);
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

    public static class RecyclerOnScrollListener extends RecyclerView.OnScrollListener {

        private int currentPage = 1;
        private boolean loading = false;
        private boolean hasMore = true;

        private final LinearLayoutManager linearLayoutManager;
        private final LoadMoreListener loadMoreListener;

        RecyclerOnScrollListener(LinearLayoutManager linearLayoutManager,
                                        LoadMoreListener loadMoreListener) {
            this.linearLayoutManager = linearLayoutManager;
            this.loadMoreListener = loadMoreListener;
        }

        void reset() {
            currentPage = 1;
            hasMore = true;
            loading = false;
        }

        void doneLoading() {
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

        interface LoadMoreListener {
            boolean onLoadMore(int page);
        }
    }
}
