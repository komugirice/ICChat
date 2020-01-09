package com.komugirice.icchat.firebase.firestore.model

import java.io.Serializable

class GroupRequests: Serializable {
    constructor(): super()
    var room = Room()
    var requests = mutableListOf<Request>()
}