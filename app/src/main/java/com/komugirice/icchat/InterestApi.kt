package com.komugirice.icchat

import com.google.gson.Gson
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory.create
import retrofit2.http.Url
import timber.log.Timber
import java.io.IOException
import java.net.URL

object InterestApi {

    fun getRequest(url: URL): Request =
        Request.Builder()
            .url(url)
            .build()

    fun call(url: URL){
        OkHttpClient().newCall(getRequest(url)).enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response) {
                Timber.d(response.code().toString())
                Timber.d(Gson().toJson(response.body()))

            }

            override fun onFailure(call: Call, e: IOException) {
                Timber.e(e)
            }
        })
    }
}