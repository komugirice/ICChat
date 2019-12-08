package com.komugirice.icchat.data.firestore.manager

import com.komugirice.icchat.data.firestore.Room

object RoomManager {

    var myRooms = Room()
        set(value) {
            field = value
        }
}