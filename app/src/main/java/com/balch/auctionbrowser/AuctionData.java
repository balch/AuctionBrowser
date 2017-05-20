package com.balch.auctionbrowser;

import com.balch.auctionbrowser.auction.model.Auction;
import com.balch.auctionbrowser.note.Note;

import java.util.List;
import java.util.Map;

public class AuctionData {
    private static final String TAG = AuctionData.class.getSimpleName();

    private List<Auction> mAuctions;
    private Map<Long, Note> mNotes;
    private int mTotalPages;

    public AuctionData() {
    }

    public AuctionData(List<Auction> auctions, Map<Long, Note> notes, int totalPages) {
        this.mAuctions = auctions;
        this.mNotes = notes;
        this.mTotalPages = totalPages;
    }

    public List<Auction> getAuctions() {
        return mAuctions;
    }

    public void setAuctions(List<Auction> auctions) {
        this.mAuctions = auctions;
    }

    public Map<Long, Note> getNotes() {
        return mNotes;
    }

    public void setNotes(Map<Long, Note> notes) {
        this.mNotes = notes;
    }

    public int getTotalPages() {
        return mTotalPages;
    }

    public void setTotalPages(int totalPages) {
        this.mTotalPages = totalPages;
    }
}
