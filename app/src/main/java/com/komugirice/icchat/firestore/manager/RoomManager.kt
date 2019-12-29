package com.komugirice.icchat.firestore.manager

import com.komugirice.icchat.firestore.model.Room
import com.komugirice.icchat.firestore.store.RoomStore

object RoomManager {

    /** 全ルーム */
    var allRooms = listOf<Room>()
        set(value) {
            if (value.isEmpty())
                return
            field = value
            myRooms = value.filter {it.userIdList.contains(UserManager.myUserId)}
            myInviteRooms = value.filter {it.inviteIdList.contains(UserManager.myUserId)}
            myDenyRooms = value.filter {it.denyIdList.contains(UserManager.myUserId)}
        }

    /** マイルーム（シングル、グループ） */
    var myRooms = listOf<Room>()
        set(value) {
            if (value.isEmpty())
                return
            field = value
            mySingleRooms = value.filter { it.isGroup == false }
            myGroupRooms = value.filter { it.isGroup == true }
        }

    /** シングルルーム */
    var mySingleRooms = listOf<Room>()

    /** グループルーム */
    var myGroupRooms = listOf<Room>()

    /** 招待されているルーム */
    var myInviteRooms = listOf<Room>()

    /** 拒否したルーム */
    var myDenyRooms = listOf<Room>()

    /**
     * RoomManager初期化
     * FirebaseAuthのcurrentUserが取得出来る前提
     * @param onComplete: () -> Unit
     */
    fun initRoomManager(onSuccess: (List<Room>) -> Unit) {
        RoomStore.getLoginUserAllRooms {
            allRooms = it
            onSuccess.invoke(it)
        }
    }

    fun getTargetRoom(roomId: String): Room? {
        return allRooms.filter{ it.documentId == roomId}.firstOrNull()
    }
}