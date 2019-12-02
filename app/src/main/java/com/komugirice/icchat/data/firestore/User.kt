package com.komugirice.icchat.data.firestore

import com.google.firebase.auth.FirebaseAuth
import java.util.*

class User {
    var userId: String = ""
    var name: String =""
    var birthDay: Date? = null
    var createdAt: Date = Date()
    var delFlg: String="0"
    var documentId = FirebaseAuth.getInstance().currentUser?.uid ?: UUID.randomUUID().toString()
}