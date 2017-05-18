package com.balch.auctionbrowser;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Log;

import com.balch.auctionbrowser.auction.EBayModel;
import com.balch.auctionbrowser.note.NotesModel;

public class AuctionLoader extends ViewModel {

    private static final String TAG = AuctionLoader.class.getSimpleName();

    private static final int AUCTION_FETCH_COUNT = 30;

    private EBayModel auctionModel;
    private NotesModel notesModel;

    private int currentPage;
    private String searchText;
    private String sortOrder;

    private final MutableLiveData<AuctionData> auctionDataLive = new MutableLiveData<>();

    public void setAuctionModel(EBayModel auctionModel) {
        this.auctionModel = auctionModel;
    }

    public void setNotesModel(NotesModel notesModel) {
        this.notesModel = notesModel;
    }

    public LiveData<AuctionData> getAuctionData() {
        return auctionDataLive;
    }

    @VisibleForTesting
    void loadInBackground() {
        try {
            if (!TextUtils.isEmpty(searchText)) {
                auctionModel.getAuctions(searchText, currentPage, AUCTION_FETCH_COUNT, sortOrder,
                        new EBayModel.AuctionListener() {
                            @Override
                            public void onAuctionInfo(EBayModel.AuctionInfo info) {
                                AuctionData auctionData = new AuctionData();
                                auctionData.setTotalPages(info.totalPages);
                                auctionData.setAuctions(info.auctions);
                                if (info.auctions != null) {
                                    auctionData.setNotes(notesModel.getNotes(info.auctions));
                                }

                                auctionDataLive.setValue(auctionData);
                            }
                        });
            } else {
                auctionDataLive.setValue(null);
            }
        } catch (Exception ex) {
            Log.e(TAG, "error getting auctions", ex);
        }
    }

    public void update(int currentPage, String searchText, String sortOrder) {
        this.currentPage = currentPage;
        this.searchText = searchText;
        this.sortOrder = sortOrder;
        loadInBackground();
    }
}


