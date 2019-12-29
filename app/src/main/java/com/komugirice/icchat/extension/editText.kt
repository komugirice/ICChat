package com.komugirice.icchat.extension

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import timber.log.Timber

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}

fun EditText.loggingSize() {
    Timber.d("${resources.getResourceEntryName(this.getId())} height:${this.height} width:${this.width}")
}