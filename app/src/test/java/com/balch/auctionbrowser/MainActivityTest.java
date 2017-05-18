package com.balch.auctionbrowser;

import com.balch.android.app.framework.sql.SqlConnection;
import com.balch.auctionbrowser.auction.Auction;
import com.balch.auctionbrowser.auction.AuctionView;
import com.balch.auctionbrowser.note.Note;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class MainActivityTest {

    @Mock AuctionView mockView;
    @Mock SqlConnection sqlConnection;

    private MainActivity activity;
    private AuctionModelProvider modelProvider;

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        modelProvider = spy(new AuctionApplication() {
            @Override
            public SqlConnection getSqlConnection() {
                return sqlConnection;
            }
        });

        activity = spy(new MainActivity() {
            @Override
            public AuctionView createView() {
                view = mockView;
                return mockView;
            }
        });

        doReturn("").when(activity).getString(eq(R.string.ebay_app_id));

        activity.createView();
        activity.createModel(modelProvider);
    }

    @Test
    public void testOnCreateBase() throws Exception {
        activity.onCreateBase(null);

        verify(mockView).setAuctionViewListener(eq(activity));
        verify(mockView).setSortStrings(eq(R.array.auction_sort_col));
        verify(mockView).showBusy();
    }

    @Test
    public void testOnLoadMore() throws Exception {
        activity.totalPages = 5;
        doNothing().when(activity).updateView();

        assertTrue(activity.onLoadMore(2));

        verify(mockView).showBusy();
        verify(activity).updateView();
    }

    @Test
    public void testOnLoadMoreNoMore() throws Exception {
        activity.totalPages = 5;
        doNothing().when(activity).updateView();

        assertFalse(activity.onLoadMore(5));

        verify(mockView, never()).showBusy();
        verify(activity, never()).updateView();
    }

    @Test
    public void testSaveNote() throws Exception {
        Auction auction = mock(Auction.class);
        Note note = mock(Note.class);
        String text = "test text";

        activity.saveNote(auction, note, text);

        verify(note).setNote(eq(text));
        verify(sqlConnection).update(eq(activity.notesModel), eq(note));
    }

}