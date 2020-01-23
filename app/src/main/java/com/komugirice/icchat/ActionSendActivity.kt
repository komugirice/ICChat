package com.komugirice.icchat

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import com.google.gson.Gson
import timber.log.Timber
import java.net.URL


class ActionSendActivity: BaseActivity() {

    var url: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        val action = intent.action
        if (Intent.ACTION_SEND == action) {
            val extras = intent.extras
            if (extras != null) {
                val ext = extras.getCharSequence(Intent.EXTRA_TEXT)
                if (ext != null) {
                    Timber.d(ext.toString())
                    url = extractURL(ext.toString())
                    Timber.d(url)
                }
            }
        }
        finish()
    }

    fun extractURL(text: String): String? {
        return Patterns.WEB_URL.toRegex().find(text)?.value
    }
}