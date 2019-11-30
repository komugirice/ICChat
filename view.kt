package com.example.qiitaapplication.extension

import android.view.View

fun View.toggle(isVisible: Boolean) {
    this.visibility = if (isVisible) View.VISIBLE else View.GONE
}