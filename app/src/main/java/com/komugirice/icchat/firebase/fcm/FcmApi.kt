package com.komugirice.icchat.firebase.fcm

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.squareup.okhttp.ResponseBody
import kotlinx.io.IOException
import okhttp3.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.create
import retrofit2.Call
import retrofit2.Retrofit
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
        fun sendNotification(@Body requestJson: String): Call<ResponseBody?>?
    }

    class RequestData {
        @SerializedName("to") //  "to" changed to token
        var token: String? = null
        @SerializedName("data")
        var sendDataModel: FcmRequest? = null

        constructor(token: String?, request: FcmRequest?){
            this.token = token
            this.sendDataModel = request
        }

    }

    fun sendMessageOkHttp(token: String?, message: String?, type: String){
        Timber.d("token:$token message:$message type:$type")

        val fcmRequest = FcmRequest().apply {
            this.message = message ?: ""
            this.type = type
        }
        val data = RequestData(token, fcmRequest)
        data.token = token

        val url = "https://fcm.googleapis.com/fcm/send"
        val client: OkHttpClient = OkHttpClient.Builder()
            .build()

        // post
//        val postBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), StringBuilder()
//            .append("{\"data\":{\"message\" :\"$message\",\"age\" : \"20\",\"address\" : \"Tokyo\"},\"to\" : \"$token\"}")
//            .toString())
        val sendJson = Gson().toJson(data)
        Timber.d("fcmBody:\n$sendJson")
        val postBody = create(MediaType.parse("application/json; charset=utf-8"), sendJson)
        val request: Request = Request.Builder().url(url).post(postBody)
            .addHeader("Authorization", "key=AAAA-rWMj7c:APA91bG5mSVkbyZAGBK4k20gruc8-weNrGXnXhE9GNT2WdxSk60ofZbW0vgmaYO-KA4a1fe2mCdaBdHoZ8qV9XghE362_kh74rpaRXBBySXvy17asn0HMSavJ9PkjYAjZBrLcxj34a31")
            .addHeader("Content-Type", "application/json")
            .build()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Timber.d("えらー")
                e.printStackTrace()
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                Timber.d("成功")
                Timber.d("response:${response.body()?.string()}")
                Timber.d(response.code().toString())
            }

        })
//        val response = client.newCall(request).execute()

    }

    /**
     * JsonConvertに不具合があるらしい
     */
    fun sendMessageRetrofit(token: String?, message: String?, type: String){
        val fcmRequest = FcmRequest().apply {
            this.message = message ?: ""
            this.type = type
        }
        val requestNotification = RequestData(token, fcmRequest)
        requestNotification.token = token
        val requestJson = Gson().toJson(requestNotification)
        Timber.d(requestJson)
        val responseBodyCall = fcmIF.sendNotification(requestJson)
        responseBodyCall?.enqueue(object: retrofit2.Callback<ResponseBody?>{
            override fun onResponse(call: Call<ResponseBody?>, response: retrofit2.Response<ResponseBody?>) {
                Timber.d("FCMメッセージ送信成功")
                Timber.d(response.message())
                Timber.d(response.code().toString())
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Timber.e(t, "FCMメッセージ送信エラー")
            }
        })

    }

}