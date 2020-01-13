package com.komugirice.icchat.firebase.firestore.model

import com.komugirice.icchat.enums.MessageType
import java.io.Serializable
import java.util.*

class Message : Serializable {
    var documentId: String = ""
    var roomId: String = ""
    var userId: String = ""
    var message: String = ""
    var type = MessageType.TEXT.id
    var createdAt: Date = Date()
}