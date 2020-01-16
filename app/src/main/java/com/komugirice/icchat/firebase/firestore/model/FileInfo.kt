package com.komugirice.icchat.firebase.firestore.model

import java.io.Serializable
import java.util.*

class FileInfo: Serializable {
    var documentId: String = ""
    var roomId: String = ""
    var name: String = ""
    var convertName: String = ""
    var createdAt: Date? = Date()
}