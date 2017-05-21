package com.balch.auctionbrowser;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.VisibleForTesting;

import com.balch.auctionbrowser.auction.AuctionAdapter;
import com.balch.auctionbrowser.auction.model.EBayModel;
import com.balch.auctionbrowser.note.Note;
import com.balch.auctionbrowser.note.NotesModel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class AuctionViewModel extends ViewModel {

    private static final int AUCTION_FETCH_COUNT = 30;

    private EBayModel auctionModel;
    private NotesModel notesModel;

    private AuctionAdapter auctionAdapter;

    private long totalPages;
    private int currentPage;
    private String searchText;
    private EBayModel.SortColumn sortColumn;

    private Disposable disposableGetAuction;

    private final MutableLiveData<AuctionData> auctionDataLive = new MutableLiveData<>();

    boolean isInitialized() {
        return ((auctionModel != null) && (notesModel != null));
    }

    public void setAuctionAdapter(AuctionAdapter auctionAdapter) {
        this.auctionAdapter = auctionAdapter;
    }

    public AuctionAdapter getAuctionAdapter() {
        return auctionAdapter;
    }

    void setAuctionModel(EBayModel auctionModel) {
        this.auctionModel = auctionModel;
    }

    void setNotesModel(NotesModel notesModel) {
        this.notesModel = notesModel;
    }

    LiveData<AuctionData> getAuctionData() {
        return auctionDataLive;
    }

    void loadAuctions(EBayModel.SortColumn sortColumn) {
        loadAuctions(searchText, sortColumn);
    }

    void loadAuctions(String searchText, EBayModel.SortColumn sortColumn) {
        this.totalPages = -1;
        this.currentPage = 1;
        this.searchText = searchText;
        this.sortColumn = sortColumn;
        getAuctionsAsync();
    }

    void loadAuctionsNextPage() {
        this.currentPage++;
        getAuctionsAsync();
    }

    String getSearchText() {
        return searchText;
    }

    boolean hasMoreAuctionPages(long page) {
        return ((totalPages == -1) || (page < totalPages));
    }

    void insertNote(Note note) {
        notesModel.insert(note);
    }

    void updateNote(Note note) {
        notesModel.update(note);
    }

    void deleteNote(Note note) {
        notesModel.delete(note);
    }

    private void getAuctionsAsync() {
        disposeGetAuctionDisposable();
        disposableGetAuction = auctionModel
                .getAuctions(searchText, currentPage, AUCTION_FETCH_COUNT, sortColumn)
                .subscribeOn(Schedulers.io())
                .map(new Function<AuctionData, AuctionData>() {
                    @Override
                    public AuctionData apply(@NonNull AuctionData auctionData) throws Exception {
                        if (auctionData.getAuctions() != null) {
                            auctionData.setNotes(notesModel.getNotes(auctionData.getAuctions()));
                        }
                        totalPages = auctionData.getTotalPages();

                        return auctionData;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
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

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public NotesModel getNotesModel() {
        return notesModel;
    }
}


