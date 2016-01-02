package com.balch.auctionbrowser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.balch.auctionbrowser.auction.EBayModel;
import com.balch.auctionbrowser.note.NotesModel;

/**
 * Adapted From http://www.androiddesignpatterns.com/2012/08/implementing-loaders.html
 */
public class AuctionLoader extends AsyncTaskLoader<AuctionData> {
    private static final String TAG = AuctionLoader.class.getSimpleName();

    private static final String EXTRA_CURRENT_PAGE = "EXTRA_CURRENT_PAGE";
    private static final String EXTRA_SEARCH_TEXT = "EXTRA_SEARCH_TEXT";
    private static final String EXTRA_SORT_ORDER = "EXTRA_SORT_ORDER";

    private static final int AUCTION_FETCH_COUNT = 30;

    private final EBayModel auctionModel;
    private final NotesModel notesModel;

    private int currentPage;
    private String searchText;
    private String sortOrder;

    private AuctionData mAuctionData;
    private UpdateReceiver mUpdateReceiver;

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
            releaseResources(data);
            return;
        }

        // Hold a reference to the old data so it doesn't get garbage collected.
        // We must protect it until the new data has been delivered.
        AuctionData oldData = mAuctionData;
        mAuctionData = data;

        if (isStarted()) {
            // If the Loader is in a started state, deliver the results to the
            // client. The superclass method does this for us.
            super.deliverResult(data);
        }

        // Invalidate the old data as we don't need it any more.
        if (oldData != null && oldData != data) {
            releaseResources(oldData);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mAuctionData != null) {
            // Deliver any previously loaded data immediately.
            deliverResult(mAuctionData);
        }

        // Begin monitoring the underlying data source.
        if (mUpdateReceiver == null) {
            mUpdateReceiver = new UpdateReceiver();

            LocalBroadcastManager.getInstance(getContext())
                    .registerReceiver(mUpdateReceiver, new IntentFilter(UpdateReceiver.class.getName()));
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
            releaseResources(mAuctionData);
            mAuctionData = null;
        }

        // The Loader is being reset, so we should stop monitoring for changes.
        if (mUpdateReceiver != null) {
            LocalBroadcastManager.getInstance(getContext())
                    .unregisterReceiver(mUpdateReceiver);
            mUpdateReceiver = null;
        }
    }

    @Override
    public void onCanceled(AuctionData data) {
        // Attempt to cancel the current asynchronous load.
        super.onCanceled(data);

        // The load has been canceled, so we should release the resources
        // associated with 'data'.
        releaseResources(data);
    }

    private void releaseResources(AuctionData data) {
        // For a simple List, there is nothing to do. For something like a Cursor, we
        // would close it in this method. All resources associated with the Loader
        // should be released here.
    }

    static public void update(Context context, int currentPage, String searchText, String sortOrder) {
        Intent intent = new Intent(UpdateReceiver.class.getName());
        intent.putExtra(EXTRA_CURRENT_PAGE, currentPage);
        intent.putExtra(EXTRA_SEARCH_TEXT, searchText);
        intent.putExtra(EXTRA_SORT_ORDER, sortOrder);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


    private class UpdateReceiver extends BroadcastReceiver {
        private UpdateReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.hasExtra(EXTRA_CURRENT_PAGE)) {
                currentPage = intent.getIntExtra(EXTRA_CURRENT_PAGE, 0);
            }

            if (intent.hasExtra(EXTRA_SEARCH_TEXT)) {
                searchText = intent.getStringExtra(EXTRA_SEARCH_TEXT);
            }

            if (intent.hasExtra(EXTRA_SORT_ORDER)) {
                sortOrder = intent.getStringExtra(EXTRA_SORT_ORDER);
            }

            onContentChanged();
        }

    }



}


