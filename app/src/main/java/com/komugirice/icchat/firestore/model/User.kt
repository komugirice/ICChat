package com.komugirice.icchat.firestore.model

import com.google.firebase.auth.FirebaseAuth
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
}