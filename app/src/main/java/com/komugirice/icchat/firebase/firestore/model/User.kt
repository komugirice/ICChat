package com.komugirice.icchat.firebase.firestore.model

import java.io.Serializable
import java.util.*

class User: Serializable {
    var userId: String = ""
    var name: String = ""
    var email: String = ""
    var birthDay: Date? = null
    var createdAt: Date = Date()
    var uids = mutableListOf<String>()
    var friendIdList = mutableListOf<String>()
    var fcmToken: String? = null
    var loginDateTime: Date? = null
}