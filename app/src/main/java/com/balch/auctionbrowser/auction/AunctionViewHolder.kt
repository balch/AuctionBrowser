package com.balch.auctionbrowser.auction

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import com.balch.auctionbrowser.R
import com.balch.auctionbrowser.auction.ext.inflate
import com.balch.auctionbrowser.auction.ext.loadUrl
import com.balch.auctionbrowser.auction.ext.toLongDateTimeString
import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.note.Note
import com.balch.auctionbrowser.ui.LabelTextView
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.auction_list_item.view.*

class AuctionViewHolder(parent: ViewGroup, private val clickAuctionObservable: PublishSubject<Auction>,
                   private val clickNoteObservable: PublishSubject<Auction>)
    : RecyclerView.ViewHolder(parent.inflate(R.layout.auction_list_item)) {

    private val itemImageView: ImageView by lazy { itemView.list_item_auction_img }
    private val titleTextView: LabelTextView by lazy { itemView.list_item_auction_title }
    private val priceTextView: LabelTextView by lazy { itemView.list_item_auction_price }
    private val endTimeTextView: LabelTextView by lazy { itemView.list_item_auction_end_time }
    private val noteEditButton: Button by lazy { itemView.list_item_auction_button_note }

    fun bind(auction: Auction, note: Note?) {

        itemImageView.loadUrl(auction.imageUrl) {request -> request}
        titleTextView.value = auction.title

        priceTextView.value = auction.currentPrice.getFormatted(2)
        endTimeTextView.value = auction.endTime.toLongDateTimeString()

        noteEditButton.setOnClickListener { clickNoteObservable.onNext(auction) }
        noteEditButton.visibility = if (note != null) View.VISIBLE else View.GONE

        itemView.setOnClickListener {
            if (auction.itemId != -1L) {
                clickAuctionObservable.onNext(auction)
            }
        }
    }
}

