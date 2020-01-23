package com.komugirice.icchat.firebase.firestore.model

import com.komugirice.icchat.enums.MessageType
import java.io.Serializable
import java.util.*

class Interest : Serializable {
    var documentId: String = ""
    var text: String = ""
    var url: String = ""
    var image: String = ""
    var createdAt: Date = Date()
}