package com.komugirice.icchat

import android.content.Intent
import android.os.Bundle
import com.example.qiitaapplication.extension.extractURL
import com.google.gson.Gson
import kotlinx.io.InputStream
import okhttp3.*
import timber.log.Timber
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
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
                    url = ext.toString().extractURL()
                    Timber.d(url)
                }
            }
        }
        if(url == null) finish()

        InterestApi.call(URL(url))

        finish()
    }

    object InterestApi {

        fun getRequest(url: URL): Request =
            Request.Builder()
                .url(url)
                .build()

        fun call(url: URL){
            OkHttpClient().newCall(getRequest(url)).enqueue(object: Callback {
                override fun onResponse(call: Call, response: Response) {
                    Timber.d(response.code().toString())
                    val data = response.body()?.string()
                    Timber.d(Gson().toJson(data))
                }

                override fun onFailure(call: Call, e: IOException) {
                    Timber.e(e)
                }
            })
        }
    }
}