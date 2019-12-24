package com.komugirice.icchat.firestore.manager

import com.komugirice.icchat.firestore.model.Room
import com.komugirice.icchat.firestore.store.RoomStore

object RoomManager {

    var myRooms = listOf<Room>()
        set(value) {
            if (value.isEmpty())
                return
            field = value
        }

    /**
     * RoomManager初期化
     * FirebaseAuthのcurrentUserが取得出来る前提
     * @param onComplete: () -> Unit
     */
    fun initRoomManager(onSuccess: () -> Unit) {
        RoomStore.getLoginUserRooms {
            myRooms = it
            onSuccess.invoke()
        }
    }
}