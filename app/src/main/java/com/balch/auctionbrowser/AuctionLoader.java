package com.balch.auctionbrowser;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.balch.auctionbrowser.auction.model.EBayModel;
import com.balch.auctionbrowser.note.NotesModel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class AuctionLoader extends ViewModel {

    private static final String TAG = AuctionLoader.class.getSimpleName();

    private static final int AUCTION_FETCH_COUNT = 30;

    private EBayModel auctionModel;
    private NotesModel notesModel;

    private int currentPage;
    private String searchText;
    private EBayModel.SortColumn sortColumn;

    private Disposable disposableGetAuction;

    private final MutableLiveData<AuctionData> auctionDataLive = new MutableLiveData<>();

    public void setAuctionModel(EBayModel auctionModel) {
        this.auctionModel = auctionModel;
    }

    public void setNotesModel(NotesModel notesModel) {
        this.notesModel = notesModel;
    }

    private void loadInBackground() {
        disposeGetAuctionDisposable();
        disposableGetAuction = auctionModel
                .getAuctions(searchText, currentPage, AUCTION_FETCH_COUNT, sortColumn)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Function<AuctionData, AuctionData>() {
                    @Override
                    public AuctionData apply(@NonNull AuctionData auctionData) throws Exception {
                        if (auctionData.getAuctions() != null) {
                            auctionData.setNotes(notesModel.getNotes(auctionData.getAuctions()));
                        }

                        return auctionData;
                    }
                })
                .subscribe(new Consumer<AuctionData>() {
                               @Override
                               public void accept(@NonNull AuctionData auctionData) throws Exception {
                                   auctionDataLive.setValue(auctionData);
                               }
                           },
                            new Consumer<Throwable>() {
                                @Override
                                public void accept(@NonNull Throwable throwable) throws Exception {
                                    auctionDataLive.setValue(null);
                                }
                            });
    }

    public LiveData<AuctionData> getAuctionData() {
        return auctionDataLive;
    }

    public void update(int currentPage, String searchText, EBayModel.SortColumn sortColumn) {
        this.currentPage = currentPage;
        this.searchText = searchText;
        this.sortColumn = sortColumn;
        loadInBackground();
    }

    void disposeGetAuctionDisposable() {
        if (disposableGetAuction != null) {
            disposableGetAuction.dispose();
            disposableGetAuction = null;
        }
    }

    @Override
    protected void onCleared() {
        disposeGetAuctionDisposable();
    }

}


