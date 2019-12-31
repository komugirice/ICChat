package com.komugirice.icchat.firestore.model

import java.io.Serializable
import java.util.*

open class Room: Serializable {
    var documentId: String = ""
    var name: String =""
    var isGroup: Boolean = false
    var createdAt: Date? = Date()
    var ownerId: String = ""    // isGroup = trueの場合だけ使用
    var userIdList = mutableListOf<String>()
}