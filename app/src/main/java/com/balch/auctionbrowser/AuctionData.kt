package com.balch.auctionbrowser

import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.note.Note

class AuctionData {
    lateinit var auctions: List<Auction>
    lateinit var notes: Map<Long, Note>
    var totalPages: Int = 0
    var hasError = false
}
