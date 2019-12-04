package com.komugirice.icchat.data.firestore

import java.util.*

class Room {
    var documentId: String = UUID.randomUUID().toString()
    var name: String =""
    var createdAt: Date? = Date()
}