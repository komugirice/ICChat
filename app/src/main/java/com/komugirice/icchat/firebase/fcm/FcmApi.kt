package com.komugirice.icchat.firebase.fcm

import com.google.gson.annotations.SerializedName
import com.squareup.okhttp.ResponseBody
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import timber.log.Timber

object FcmApi {

    const val BASE_URL = "https://fcm.googleapis.com/"
    val fcmIF: FCMApiInterface by lazy {getClient().create(FCMApiInterface::class.java)}

    fun getClient(): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    interface FCMApiInterface {
        @Headers("Authorization: key=AAAA-rWMj7c:APA91bG5mSVkbyZAGBK4k20gruc8-weNrGXnXhE9GNT2WdxSk60ofZbW0vgmaYO-KA4a1fe2mCdaBdHoZ8qV9XghE362_kh74rpaRXBBySXvy17asn0HMSavJ9PkjYAjZBrLcxj34a31",
            "Content-Type:application/json"
        )
        @POST("fcm/send")
        fun sendNotification(@Body requestNotificaton: RequestNotificaton): Call<ResponseBody?>?
    }

    class RequestNotificaton {
        @SerializedName("token") //  "to" changed to token
        var token: String? = null
        @SerializedName("notification")
        var sendNotificationModel: FcmRequest? = null

        constructor(token: String?, request: FcmRequest?){
            this.token = token
            this.sendNotificationModel = request
        }

    }

    fun sendMessage(token: String?, message: String?, type: String){
        val fcmRequest = FcmRequest().apply {
            this.message = message ?: ""
            this.type = type
        }
        val requestNotification = RequestNotificaton(token, fcmRequest)
        requestNotification.token = token
        val responseBodyCall = fcmIF.sendNotification(requestNotification)
        responseBodyCall?.enqueue(object: Callback<ResponseBody?>{
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                Timber.e("FCMメッセージ送信成功")
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Timber.e(t, "FCMメッセージ送信エラー")
            }
        })

    }

}