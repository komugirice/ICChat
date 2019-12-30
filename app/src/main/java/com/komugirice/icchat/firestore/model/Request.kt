package com.komugirice.icchat.firestore.model

import java.io.Serializable
import java.util.*

class Request : Serializable {
    var documentId = ""
    var isGroup = false
    var requestIdList = mutableListOf<String>()
    var denyIdList = mutableListOf<String>()
    var createdAt = Date()
}