package com.balch.auctionbrowser;

import com.android.volley.Request;

public interface NetworkRequestProvider {
    <T> Request<T> addRequest(Request<T> request);

    <T> Request<T> addRequest(Request<T> request, boolean customRetryPolicy);
}
