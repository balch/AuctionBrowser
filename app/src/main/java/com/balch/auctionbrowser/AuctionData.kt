package com.balch.auctionbrowser

import com.balch.auctionbrowser.auction.model.Auction
import com.balch.auctionbrowser.note.Note

class AuctionData {
    var auctions: List<Auction>? = null
    var notes: Map<Long, Note>? = null
    var totalPages: Int = 0
}
