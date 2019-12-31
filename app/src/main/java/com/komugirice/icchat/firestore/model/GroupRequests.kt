package com.komugirice.icchat.firestore.model

import com.komugirice.icchat.enum.RequestStatus
import java.io.Serializable
import java.util.*

class GroupRequests: Serializable {
    constructor(): super()
    var room = Room()
    var requests = mutableListOf<Request>()
}