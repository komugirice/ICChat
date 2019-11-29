package com.komugirice.icchat.data.firestore

import java.util.*

class User {
    var userId: String = ""
    var name: String =""
    var birthDay: Date? = null
    var createdAt: Date = Date()
    var delFlg: String="0"
}