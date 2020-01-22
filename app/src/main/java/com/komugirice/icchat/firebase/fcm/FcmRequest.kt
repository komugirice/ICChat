package com.komugirice.icchat.firebase.fcm

import com.google.gson.annotations.SerializedName

data class FcmRequest (
    // var name: String = "",
    @SerializedName("message")
    var message: String = "",
    @SerializedName("type")
    var type: String =""
)