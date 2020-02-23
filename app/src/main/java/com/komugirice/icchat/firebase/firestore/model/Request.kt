package com.komugirice.icchat.firebase.firestore.model

import com.komugirice.icchat.enums.RequestStatus
import java.io.Serializable
import java.util.*

class Request : Serializable {
    var documentId = ""
    //var isGroup = false
    var requesterId = ""
    var beRequestedId = ""
    // 0:申請中, 1:承認, 2: 拒否
    var status = RequestStatus.REQUEST.id
    var createdAt = Date()
}