package com.komugirice.icchat.data.firestore

import java.io.Serializable
import java.util.*

class Group: Serializable {
    var roomId: String = ""
    var userId: String =""
    var createdAt: Date = Date()
    var delFlg: String="0"
}