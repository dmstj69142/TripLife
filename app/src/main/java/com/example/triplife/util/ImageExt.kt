package com.example.triplife.util

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

fun ImageView.loadImageUrl(url: String, isCircle: Boolean = false) {
    Glide.with(context)
        .load(url)
        .run {
            if (isCircle) {
                apply(RequestOptions().circleCrop())
            } else {
                this
            }
        }
        .into(this)
}

