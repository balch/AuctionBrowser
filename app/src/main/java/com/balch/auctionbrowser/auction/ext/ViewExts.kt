package com.balch.auctionbrowser.auction.ext

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.DrawableRequestBuilder
import com.bumptech.glide.DrawableTypeRequest
import com.bumptech.glide.Glide

fun ViewGroup.inflate(layoutId: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutId, this, attachToRoot)
}

fun Activity.inflate(layoutId: Int, root: ViewGroup? = null): View {
    return layoutInflater.inflate(layoutId, root)
}

fun ImageView.loadUrl(url: String, request: (DrawableTypeRequest<String>) -> DrawableRequestBuilder<String>) {
    request(Glide.with(context).load(url)).into(this)
}
