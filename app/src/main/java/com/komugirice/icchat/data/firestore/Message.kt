package com.komugirice.icchat.data.firestore

import java.util.*

class Message {
    var documentId: String = ""
    var roomId: String = ""
    var userId: String =""
    var message: String=""
    var createdAt: Date = Date()
}