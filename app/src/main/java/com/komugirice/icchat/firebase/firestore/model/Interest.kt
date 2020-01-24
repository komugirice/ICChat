package com.komugirice.icchat.firebase.firestore.model

import com.komugirice.icchat.enums.MessageType
import java.io.Serializable
import java.util.*

class Interest : Serializable {
    var documentId: String = ""
    var comment: String? = null
    var image: String? = null
    var isOgp = false
    var ogpUrl: String? = null
    var ogpTitle: String? = null
    var ogpImageUrl: String? = null
    var ogpDescription: String? = null
    var createdAt: Date = Date()
}