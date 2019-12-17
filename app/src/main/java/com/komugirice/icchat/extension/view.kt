package com.example.qiitaapplication.extension

import android.content.Context
import android.text.Layout
import android.view.View
import kotlinx.android.synthetic.main.friend_cell.view.*
import timber.log.Timber

fun View.toggle(isVisible: Boolean) {
    this.visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun View.loggingSize() {
    Timber.d("${resources.getResourceEntryName(this.getId())} height:${this.height} width:${this.width}")
}
