/*
 * Author: Balch
 *
 * This file is part of AuctionBrowser.
 *
 * AuctionBrowser is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AuctionBrowser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MockTrade.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2017
 *
 */

package com.balch.auctionbrowser.auction

import android.app.Dialog
import android.content.DialogInterface.BUTTON_NEGATIVE
import android.content.DialogInterface.BUTTON_POSITIVE
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.EditText
import com.balch.auctionbrowser.R.layout.auction_detail_dialog
import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.ext.inflate
import com.balch.auctionbrowser.ext.loadUrl
import com.balch.auctionbrowser.ext.toLongDateTimeString
import com.balch.auctionbrowser.note.Note
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.auction_detail_dialog.*

class AuctionDetailDialog constructor(auction: Auction, note: Note?): DialogFragment() {
    private val ARG_NOTE = "ARG_NOTE"
    private val ARG_AUCTION = "ARG_AUCTION"

    init {
        val args = Bundle()
        if (note != null) {
            args.putString(ARG_NOTE, note.noteText)
        }
        args.putSerializable(ARG_AUCTION, auction)
        arguments = args
    }

    private val noteEditText: EditText by lazy { dialog.auction_detail_note }

    private val saveNoteSubject: PublishSubject<String> = PublishSubject.create()
    private val clearNoteSubject: PublishSubject<Unit> = PublishSubject.create()

    val onSaveNote: Observable<String>
        get() = saveNoteSubject

    val onClearNote: Observable<Unit>
        get() = clearNoteSubject

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity)
                .setView(inflate(auction_detail_dialog))
                .create()
    }

    override fun onStart() {
        super.onStart()

        (dialog as AlertDialog).getButton(BUTTON_NEGATIVE).isEnabled = false
        (dialog as AlertDialog).getButton(BUTTON_POSITIVE).isEnabled = false

        var enableSaveButtons = false

        val args = arguments

        if (args != null) {
            if (args.containsKey(ARG_NOTE)) {
                noteEditText.setText(args.getString(ARG_NOTE))
            }

            if (args.containsKey(ARG_AUCTION)) {
                val auction = args.getSerializable(ARG_AUCTION) as Auction

                enableSaveButtons = auction.itemId != -1L

                with (auction) {
                    dialog.auction_detail_title.text = title
                    dialog.auction_detail_item_img.loadUrl(imageUrl) {request -> request}
                    dialog.auction_detail_end_time.value = endTime.toLongDateTimeString()
                    dialog.auction_detail_price.value = currentPrice.getFormatted(2)
                    dialog.auction_detail_location.value = location
                    dialog.auction_detail_shipping_cost.value = shippingCost.getFormatted(2)
                    dialog.auction_detail_buy_it_now.value = if (isBuyItNow) "\u2714" else "\u2718"
                }
            }
        }

        val saveButton = dialog.member_detail_button_save
        val clearButton = dialog.member_detail_button_clear
        if (enableSaveButtons) {
            saveButton.setOnClickListener { _ ->
                saveNoteSubject.onNext(noteEditText.text.toString())
                dismiss()
            }

            clearButton.setOnClickListener { _ ->
                clearNoteSubject.onNext(Unit)
                dismiss()
            }
        } else {
            saveButton.visibility = View.GONE
            clearButton.visibility = View.GONE
        }
    }
}
