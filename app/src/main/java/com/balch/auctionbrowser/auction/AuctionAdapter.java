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

package com.balch.auctionbrowser.auction;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.balch.auctionbrowser.R;
import com.balch.auctionbrowser.auction.model.Auction;
import com.balch.auctionbrowser.note.Note;
import com.balch.auctionbrowser.ui.LabelTextView;
import com.bumptech.glide.Glide;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class AuctionAdapter extends RecyclerView.Adapter<AuctionAdapter.MemberHolder>  {
    private static final DateFormat DATE_TIME_FORMAT =
            DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM);

    private List<Auction> auctions = new ArrayList<>();
    @SuppressLint("UseSparseArrays")
    private Map<Long,Note> notes = new HashMap<>();

    private final PublishSubject<Auction> clickAuctionObservable = PublishSubject.create();
    private final PublishSubject<Auction> clickNoteObservable = PublishSubject.create();

    public AuctionAdapter() {
    }

    public Observable<Auction> getClickAuctionObservable() {
        return clickAuctionObservable;
    }

    public Observable<Auction> getClickNoteObservable() {
        return clickNoteObservable;
    }

    @Override
    public MemberHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.auction_list_item, parent, false);
        return new MemberHolder(view, clickAuctionObservable, clickNoteObservable);
    }

    @Override
    public void onBindViewHolder(MemberHolder holder, int position) {
        Auction auction = auctions.get(position);
        holder.bind(auction, this.notes.get(auction.getItemId()));
    }

    @Override
    public int getItemCount() {
        return auctions.size();
    }

    void addAuctions(List<Auction> auctions, Map<Long, Note> notes) {
        this.auctions.addAll(auctions);
        this.notes.putAll(notes);
        notifyDataSetChanged();
    }

    void clearAuctions() {
        this.auctions.clear();
        this.notes.clear();
        notifyDataSetChanged();
    }

    Map<Long, Note> getNotes() {
        return notes;
    }

    static class MemberHolder extends RecyclerView.ViewHolder {

        private ImageView itemImageView;
        private LabelTextView titleTextView;
        private LabelTextView priceTextView;
        private LabelTextView endTimeTextView;
        private Button noteEditButton;

        private final PublishSubject<Auction> clickAuctionObservable;
        private final PublishSubject<Auction> clickNoteObservable;

        MemberHolder(View itemView, PublishSubject<Auction> clickAuctionObservable,
                     PublishSubject<Auction> clickNoteObservable) {
            super(itemView);

            itemImageView = (ImageView) itemView.findViewById(R.id.list_item_auction_img);
            titleTextView = (LabelTextView) itemView.findViewById(R.id.list_item_auction_title);
            endTimeTextView = (LabelTextView) itemView.findViewById(R.id.list_item_auction_end_time);
            priceTextView = (LabelTextView) itemView.findViewById(R.id.list_item_auction_price);
            noteEditButton = (Button) itemView.findViewById(R.id.list_item_auction_button_note);

            this.clickAuctionObservable = clickAuctionObservable;
            this.clickNoteObservable = clickNoteObservable;
        }

        void bind(final Auction auction, Note note) {

            Glide.with(itemView.getContext()).load(auction.getImageUrl()).into(itemImageView);
            titleTextView.setValue(auction.getTitle());

            priceTextView.setValue(auction.getCurrentPrice().getFormatted(2));
            endTimeTextView.setValue(DATE_TIME_FORMAT.format(auction.getEndTime()));

            noteEditButton.setOnClickListener(v -> clickNoteObservable.onNext(auction));
            noteEditButton.setVisibility((note != null) ? View.VISIBLE : View.GONE);

            itemView.setOnClickListener(v -> {
                if (auction.getItemId() != -1) {
                    clickAuctionObservable.onNext(auction);
                }
            });
        }
    }

}
