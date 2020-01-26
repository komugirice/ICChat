package com.komugirice.icchat.extension

import android.view.View
import timber.log.Timber

fun View.toggle(isVisible: Boolean) {
    this.visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun View.loggingSize() {
    Timber.d("${resources.getResourceEntryName(this.id)} height:${this.height} width:${this.width}")
}
