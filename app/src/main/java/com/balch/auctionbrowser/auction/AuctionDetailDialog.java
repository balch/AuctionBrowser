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

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.balch.auctionbrowser.AuctionModelProvider;
import com.balch.auctionbrowser.R;
import com.balch.auctionbrowser.ui.LabelTextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class AuctionDetailDialog extends DialogFragment {
    private static final String TAG = AuctionDetailDialog.class.getSimpleName();

    public static final String ARG_NOTE = "ARG_NOTE";
    public static final String ARG_AUCTION = "ARG_AUCTION";

    public static final DateFormat DATE_TIME_FORMAT = SimpleDateFormat.getDateTimeInstance();

    private NoteDetailDialogListener noteDetailDialogListener;
    private EditText noteEditText;

    public interface NoteDetailDialogListener {
        void onSave(String note);
        void onClear();
    }

    public void setNoteDetailDialogListener(NoteDetailDialogListener noteDetailDialogListener) {
        this.noteDetailDialogListener = noteDetailDialogListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.auction_detail_dialog, null));
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
        ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        noteEditText = (EditText)getDialog().findViewById(R.id.auction_detail_note);
        boolean enableSaveButtons = false;

        Bundle args = getArguments();

        if (args != null) {
            if (args.containsKey(ARG_NOTE)) {
                noteEditText.setText(args.getString(ARG_NOTE));
            }

            if (args.containsKey(ARG_AUCTION)) {
                Auction auction = (Auction)args.getSerializable(ARG_AUCTION);

                enableSaveButtons = (auction.getItemId() != -1L);

                TextView tv = (TextView) getDialog().findViewById(R.id.auction_detail_title);
                tv.setText(auction.getTitle());

                NetworkImageView profileImageView = (NetworkImageView) getDialog().findViewById(R.id.auction_detail_item_img);
                profileImageView.setImageUrl(auction.getImageUrl(), ((AuctionModelProvider) getActivity().getApplication()).getImageLoader());

                LabelTextView ltv = (LabelTextView) getDialog().findViewById(R.id.auction_detail_end_time);
                ltv.setValue(DATE_TIME_FORMAT.format(auction.getEndTime()));

                ltv = (LabelTextView) getDialog().findViewById(R.id.auction_detail_price);
                ltv.setValue(auction.getCurrentPrice().getFormatted(2));

                ltv = (LabelTextView) getDialog().findViewById(R.id.auction_detail_location);
                ltv.setValue(auction.getLocation());

                if (auction.getShippingCost() != null) {
                    ltv = (LabelTextView) getDialog().findViewById(R.id.auction_detail_shipping_cost);
                    ltv.setValue(auction.getShippingCost().getFormatted(2));
                }

                ltv = (LabelTextView) getDialog().findViewById(R.id.auction_detail_buy_it_now);
                ltv.setValue(auction.isBuyItNow()?"\u2714" : "\u2718");

            }
        }

        Button saveButton = (Button)getDialog().findViewById(R.id.member_detail_button_save);
        Button clearButton = (Button)getDialog().findViewById(R.id.member_detail_button_clear);
        if (enableSaveButtons) {
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (noteDetailDialogListener != null) {
                        noteDetailDialogListener.onSave(noteEditText.getText().toString());
                    }
                    dismiss();
                }
            });

            clearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (noteDetailDialogListener != null) {
                        noteDetailDialogListener.onClear();
                    }
                    dismiss();
                }
            });


        } else {
            saveButton.setVisibility(View.GONE);
            clearButton.setVisibility(View.GONE);
        }


    }


}
