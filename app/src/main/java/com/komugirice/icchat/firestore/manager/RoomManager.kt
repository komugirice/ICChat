package com.komugirice.icchat.firestore.manager

import com.komugirice.icchat.firestore.model.Room

object RoomManager {

    var myRooms = Room()
        set(value) {
            field = value
        }
}