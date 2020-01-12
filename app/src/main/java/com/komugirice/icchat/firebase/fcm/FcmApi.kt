package com.komugirice.icchat.firebase.fcm

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.*
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONObject
import retrofit2.*
import retrofit2.Call
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import timber.log.Timber
import java.io.IOException

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
        fun sendNotification(@Body requestNotificaton: RequestData): Call<ResponseBody?>?

        @Headers("Authorization: key=AAAA-rWMj7c:APA91bG5mSVkbyZAGBK4k20gruc8-weNrGXnXhE9GNT2WdxSk60ofZbW0vgmaYO-KA4a1fe2mCdaBdHoZ8qV9XghE362_kh74rpaRXBBySXvy17asn0HMSavJ9PkjYAjZBrLcxj34a31",
            "Content-Type:application/json"
        )
        @POST("fcm/send")
        fun sendFcmLowBody(@Body body: String): Call<ResponseBody?>

        @Headers("Authorization: key=AAAA-rWMj7c:APA91bG5mSVkbyZAGBK4k20gruc8-weNrGXnXhE9GNT2WdxSk60ofZbW0vgmaYO-KA4a1fe2mCdaBdHoZ8qV9XghE362_kh74rpaRXBBySXvy17asn0HMSavJ9PkjYAjZBrLcxj34a31",
            "Content-Type:application/json"
        )
        @POST("fcm/send")
        fun sendFcm(@Body body: NotiicationSendData): Call<ResponseBody?>
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

    class NotiicationSendData() {
        var to = ""
        var data = Data()
        class Data {
            var message = ""
            var type = 0
        }
    }

    fun sendMessage(token: String?, message: String?, type: String){
        Timber.d("token:$token message:$message type:$type")

        val data = NotiicationSendData().apply {
            this.to = token ?: ""
            data = FcmApi.NotiicationSendData.Data().apply {
                this.message = message ?: ""
            }
        }

        val url = "https://fcm.googleapis.com/fcm/send"
        val client: OkHttpClient = OkHttpClient.Builder()
            .build()

        // post
//        val postBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), StringBuilder()
//            .append("{\"data\":{\"message\" :\"$message\",\"age\" : \"20\",\"address\" : \"Tokyo\"},\"to\" : \"$token\"}")
//            .toString())
        val sendJson = Gson().toJson(data)
        Timber.d("fcmBody:\n$sendJson")
        val postBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), sendJson)
        val request: Request = Request.Builder().url(url).post(postBody)
            .addHeader("Authorization", "key=AAAA-rWMj7c:APA91bG5mSVkbyZAGBK4k20gruc8-weNrGXnXhE9GNT2WdxSk60ofZbW0vgmaYO-KA4a1fe2mCdaBdHoZ8qV9XghE362_kh74rpaRXBBySXvy17asn0HMSavJ9PkjYAjZBrLcxj34a31")
            .addHeader("Content-Type", "application/json")
            .build()
        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Timber.d("えらー")
                e.printStackTrace()
//                Timber.d("response:${response.body()?.string()}")
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                Timber.d("成功")
                Timber.d("response:${response.body()?.string()}")
            }

        })
//        val response = client.newCall(request).execute()

    }

//    fun sendMessage(token: String?, message: String?, type: String){
//        Timber.d("token:$token message:$message type:$type")
//        if (token == null)
//            return
//        val request = StringBuilder()
//            .append("{\"data\":{\"message\" :\"$message\",\"age\" : \"20\",\"address\" : \"Tokyo\"},\"to\" : \"$token\"}")
//            .toString()
//        fcmIF.sendFcm(NotiicationSendData().apply {
//            this.token = token
//            data = FcmApi.NotiicationSendData.Data().apply {
//                this.message = message ?: "Null"
//            }
//        })
//            .enqueue(object: Callback<ResponseBody?>{
//            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
//                Timber.d("FCMメッセージ送信成功")
//                Timber.d(response.body()?.string())
//                Timber.d(response.code().toString())
//            }
//
//            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
//                t.printStackTrace()
//                Timber.d("FCMメッセージ送信失敗")
//            }
//        })
//    }

//    fun sendMessage(token: String?, message: String?, type: String){
//        Timber.d("token:$token message:$message type:$type")
//        val fcmRequest = FcmRequest().apply {
//            this.message = message ?: ""
//            this.type = type
//        }
//        val requestNotification = RequestData(token, fcmRequest)
//        requestNotification.token = token
//        val responseBodyCall = fcmIF.sendNotification(requestNotification)
//        responseBodyCall?.enqueue(object: Callback<ResponseBody?>{
//            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
//                Timber.d("FCMメッセージ送信成功")
//                Timber.d(response.message())
//                Timber.d(response.code().toString())
//            }
//
//            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
//                t.printStackTrace()
//            }
//        })
//
//    }

}