package com.komugirice.icchat.data.firestore.model

import com.google.firebase.auth.FirebaseAuth
import java.io.Serializable
import java.util.*

class User: Serializable {
    var userId: String = ""
    var name: String =""
    var birthDay: Date? = null
    var createdAt: Date = Date()
    var documentId = ""
    var friendIdList = mutableListOf<String>()
}