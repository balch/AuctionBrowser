package com.balch.auctionbrowser;

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.balch.auctionbrowser.auction.EBayModel;
import com.balch.auctionbrowser.note.NotesModel;

public class AuctionLoader extends ViewModel implements LifecycleRegistryOwner {

    private static final String TAG = AuctionLoader.class.getSimpleName();

    private static final int AUCTION_FETCH_COUNT = 30;

    private EBayModel auctionModel;
    private NotesModel notesModel;

    private int currentPage;
    private String searchText;
    private String sortOrder;

    private final MutableLiveData<AuctionData> auctionDataLive = new MutableLiveData<>();

    private final LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    public void setAuctionModel(EBayModel auctionModel) {
        this.auctionModel = auctionModel;
    }

    public void setNotesModel(NotesModel notesModel) {
        this.notesModel = notesModel;
    }

    private void loadInBackground() {
        try {
            if (!TextUtils.isEmpty(searchText)) {
                auctionModel.getAuctions(
                        searchText,
                        currentPage,
                        AUCTION_FETCH_COUNT,
                        sortOrder).observe(this,
                        new Observer<EBayModel.AuctionInfo>() {
                            @Override
                            public void onChanged(@Nullable EBayModel.AuctionInfo info) {
                                AuctionData auctionData = new AuctionData();
                                auctionData.setTotalPages(info.totalPages);
                                auctionData.setAuctions(info.auctions);
                                if (info.auctions != null) {
                                    auctionData.setNotes(notesModel.getNotes(info.auctions));
                                }

                                auctionDataLive.setValue(auctionData);
                            }
                        });

            }
        } catch (Exception ex) {
            Log.e(TAG, "error getting auctions", ex);
        }
    }

    public LiveData<AuctionData> getAuctionData() {
        return auctionDataLive;
    }

    public void update(int currentPage, String searchText, String sortOrder) {
        this.currentPage = currentPage;
        this.searchText = searchText;
        this.sortOrder = sortOrder;
        loadInBackground();
    }
    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }
}


