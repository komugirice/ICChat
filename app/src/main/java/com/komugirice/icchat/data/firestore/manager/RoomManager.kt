package com.komugirice.icchat.data.firestore.manager

import com.komugirice.icchat.data.firestore.model.Room

object RoomManager {

    var myRooms = Room()
        set(value) {
            field = value
        }
}