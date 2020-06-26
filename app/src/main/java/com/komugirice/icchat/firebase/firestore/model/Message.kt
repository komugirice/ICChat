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

    fun copy(message: Message) {
        documentId = message.documentId
        roomId = message.roomId
        userId = message.userId
        this.message = message.message
        type = message.type
        createdAt = message.createdAt

    }
}