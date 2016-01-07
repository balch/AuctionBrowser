package com.balch.auctionbrowser;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;
import android.util.Log;

import com.balch.auctionbrowser.auction.EBayModel;
import com.balch.auctionbrowser.note.NotesModel;

/**
 * Adapted From http://www.androiddesignpatterns.com/2012/08/implementing-loaders.html
 */
public class AuctionLoader extends AsyncTaskLoader<AuctionData> {
    private static final String TAG = AuctionLoader.class.getSimpleName();

    private static final int AUCTION_FETCH_COUNT = 30;

    private final EBayModel auctionModel;
    private final NotesModel notesModel;

    private int currentPage;
    private String searchText;
    private String sortOrder;

    private AuctionData mAuctionData;

    public AuctionLoader(Context context,
                         EBayModel auctionModel, NotesModel notesModel) {
        super(context);
        this.auctionModel = auctionModel;
        this.notesModel = notesModel;
    }

    @Override
    public AuctionData loadInBackground() {
        AuctionData auctionData = new AuctionData();
        try {
            if (!TextUtils.isEmpty(searchText)) {
                EBayModel.AuctionInfo info = this.auctionModel.getAuctions(
                        searchText,
                        currentPage,
                        AUCTION_FETCH_COUNT,
                        sortOrder);

                auctionData.setTotalPages(info.totalPages);
                auctionData.setAuctions(info.auctions);
                if (info.auctions != null) {
                    auctionData.setNotes(this.notesModel.getNotes(info.auctions));
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "error getting auctions", ex);
        }

        return auctionData;
    }

    @Override
    public void deliverResult(AuctionData data) {
        if (isReset()) {
            // The Loader has been reset; ignore the result and invalidate the data.
            return;
        }

        mAuctionData = data;

        if (isStarted()) {
            // If the Loader is in a started state, deliver the results to the
            // client. The superclass method does this for us.
            super.deliverResult(data);
        }

    }

    @Override
    protected void onStartLoading() {
        if (mAuctionData != null) {
            // Deliver any previously loaded data immediately.
            deliverResult(mAuctionData);
        }

        if (takeContentChanged() || mAuctionData == null) {
            // When the observer detects a change, it should call onContentChanged()
            // on the Loader, which will cause the next call to takeContentChanged()
            // to return true. If this is ever the case (or if the current data is
            // null), we force a new load.
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // The Loader is in a stopped state, so we should attempt to cancel the
        // current load (if there is one).
        cancelLoad();

        // Note that we leave the observer as is. Loaders in a stopped state
        // should still monitor the data source for changes so that the Loader
        // will know to force a new load if it is ever started again.
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader has been stopped.
        onStopLoading();

        // At this point we can release the resources associated with 'mData'.
        if (mAuctionData != null) {
            mAuctionData = null;
        }

    }

    public void update(int currentPage, String searchText, String sortOrder) {
        this.currentPage = currentPage;
        this.searchText = searchText;
        this.sortOrder = sortOrder;
        onContentChanged();
    }

}


