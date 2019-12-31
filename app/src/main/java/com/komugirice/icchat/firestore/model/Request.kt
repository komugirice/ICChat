package com.komugirice.icchat.firestore.model

import com.komugirice.icchat.enum.RequestStatus
import java.io.Serializable
import java.util.*

class Request : Serializable {
    var documentId = ""
    //var isGroup = false
    var requestId = ""
    var beRequestedId = ""
    // 0:申請中, 1:承認, 2: 拒否
    var status = RequestStatus.REQUEST.id
    var createdAt = Date()
}