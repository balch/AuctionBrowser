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

package com.balch.auctionbrowser.note;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.balch.auctionbrowser.R;

public class NoteEditDialog extends DialogFragment {
    private static final String TAG = NoteEditDialog.class.getSimpleName();

    public static final String ARG_NOTE = "ARG_NOTE";
    public static final String ARG_TITLE = "ARG_TITLE";

    private NoteEditDialogListener noteEditDialogListener;
    private EditText noteEditText;

    public interface NoteEditDialogListener {
        void onSave(String note);
        void onClear();
    }

    public void setNoteEditDialogListener(NoteEditDialogListener noteEditDialogListener) {
        this.noteEditDialogListener = noteEditDialogListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.note_edit_dialog, null));
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
        ((AlertDialog)getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        noteEditText = (EditText) getDialog().findViewById(R.id.dialog_note_edit_note);
        Bundle args = getArguments();

        if (args != null) {
            if (args.containsKey(ARG_NOTE)) {
                noteEditText.setText(args.getString(ARG_NOTE));
            }

            TextView tv = (TextView) getDialog().findViewById(R.id.dialog_note_edit_name);
            if (args.containsKey(ARG_TITLE)) {
                tv.setText(getResources().getString(R.string.edit_note_label, args.getString(ARG_TITLE)));
            } else {
                tv.setVisibility(View.GONE);
            }

        }

        getDialog().findViewById(R.id.dialog_note_edit_button_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noteEditDialogListener != null) {
                    noteEditDialogListener.onSave(noteEditText.getText().toString());
                }
                dismiss();
            }
        });

        getDialog().findViewById(R.id.dialog_note_edit_button_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noteEditDialogListener != null) {
                    noteEditDialogListener.onClear();
                }
                dismiss();
            }
        });


    }


}
