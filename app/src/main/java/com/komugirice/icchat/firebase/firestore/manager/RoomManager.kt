package com.komugirice.icchat.firebase.firestore.manager

import com.komugirice.icchat.firebase.firestore.model.Room
import com.komugirice.icchat.firebase.firestore.store.RoomStore

object RoomManager {
    
    /** マイルーム（シングル、グループ） */
    var myRooms = listOf<Room>()
        set(value) {
            // 0件の時も初期化するため
            field = value
            mySingleRooms = value.filter { it.isGroup == false }
            myGroupRooms = value.filter { it.isGroup == true }
        }

    /** シングルルーム */
    var mySingleRooms = listOf<Room>()

    /** グループルーム */
    var myGroupRooms = listOf<Room>()

    /**
     * RoomManager初期化
     * FirebaseAuthのcurrentUserが取得出来る前提
     * @param onComplete: () -> Unit
     */
    fun initRoomManager(onSuccess: (List<Room>) -> Unit) {
        RoomStore.getLoginUserRooms {
            myRooms = it
            onSuccess.invoke(it)
        }
    }

    fun getTargetRoom(roomId: String): Room? {
        return myRooms.filter{ it.documentId == roomId}.firstOrNull()
    }

    fun clear() {
        myRooms = listOf<Room>()
        mySingleRooms = listOf<Room>()
        myGroupRooms = listOf<Room>()
    }

}