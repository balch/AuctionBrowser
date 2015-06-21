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

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.balch.auctionbrowser.ModelProvider;
import com.balch.auctionbrowser.R;
import com.balch.auctionbrowser.note.Note;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuctionAdapter extends RecyclerView.Adapter<AuctionAdapter.MemberHolder>  {
    private static final String TAG = AuctionAdapter.class.getSimpleName();

    public interface MembersAdapterListener {
        void onClickNoteButton(Auction auction);
        void onClickMember(Auction auction);
    }

    private MembersAdapterListener membersAdapterListener;
    private List<Auction> auctions = new ArrayList<>();
    private Map<Long,Note> notes = new HashMap<>();
    private ModelProvider modelProvider;

    public static class MemberHolder extends RecyclerView.ViewHolder {

        private MembersAdapterListener membersAdapterListener;

        private NetworkImageView itemImageView;
        private TextView titleTextView;
        private TextView priceTextView;
        private TextView locationTextView;
        private TextView shippingCostTextView;
        private Button noteEditButton;

        public MemberHolder(View itemView, MembersAdapterListener membersAdapterListener) {
            super(itemView);

            this.membersAdapterListener = membersAdapterListener;
            this.itemImageView = (NetworkImageView) itemView.findViewById(R.id.list_item_auction_img);
            this.titleTextView = (TextView) itemView.findViewById(R.id.list_item_auction_value_title);
            this.priceTextView = (TextView) itemView.findViewById(R.id.list_item_auction_value_end_time);
            this.locationTextView = (TextView) itemView.findViewById(R.id.list_item_auction_value_location);
            this.shippingCostTextView = (TextView) itemView.findViewById(R.id.list_item_auction_value_shipping_cost);
            this.noteEditButton = (Button) itemView.findViewById(R.id.list_item_auction_button_note);
        }

        public void bind(final Auction auction, Note note, ModelProvider modelProvider) {

            this.itemImageView.setImageUrl(auction.getImageUrl(), modelProvider.getImageLoader());
            this.titleTextView.setText(auction.getTitle());


            this.priceTextView.setText(auction.getCurrentPrice().getCurrency(2));
            this.locationTextView.setText(auction.getLocation());
            this.shippingCostTextView.setText(auction.getShippingCost().getCurrency(2));

            this.noteEditButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MemberHolder.this.membersAdapterListener.onClickNoteButton(auction);
                }
            });

            this.noteEditButton.setVisibility((note != null) ? View.VISIBLE : View.GONE);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (auction.getItemId() != -1) {
                        MemberHolder.this.membersAdapterListener.onClickMember(auction);
                    }
                }
            });
        }
    }
    public AuctionAdapter(ModelProvider modelProvider, MembersAdapterListener membersAdapterListener) {
        this.membersAdapterListener = membersAdapterListener;
        this.modelProvider = modelProvider;
    }

    @Override
    public MemberHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.auction_list_item, parent, false);
        return new MemberHolder(view, this.membersAdapterListener);
    }

    @Override
    public void onBindViewHolder(MemberHolder holder, int position) {
        Auction auction = auctions.get(position);
        holder.bind(auction, this.notes.get(auction.getItemId()), this.modelProvider);
    }

    @Override
    public int getItemCount() {
        return auctions.size();
    }

    public void addMembers(List<Auction> auctions, Map<Long, Note> notes) {
        this.auctions.addAll(auctions);
        this.notes.putAll(notes);
        notifyDataSetChanged();
    }

    public void clearMembers() {
        this.auctions.clear();
        this.notes.clear();
        notifyDataSetChanged();
    }

    public Map<Long, Note> getNotes() {
        return notes;
    }
}
