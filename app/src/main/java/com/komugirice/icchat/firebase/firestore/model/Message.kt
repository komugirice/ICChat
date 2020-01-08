package com.komugirice.icchat.firebase.firestore.model

import java.io.Serializable
import java.util.*

class Message: Serializable {
    var documentId: String = ""
    var roomId: String = ""
    var userId: String =""
    var message: String=""
    var createdAt: Date = Date()
}