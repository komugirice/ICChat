package com.komugirice.icchat.firestore.model

import java.io.Serializable
import java.util.*

class Room: Serializable {
    var documentId: String = ""
    var name: String =""
    var createdAt: Date? = Date()
    var userIdList = mutableListOf<String>()
}