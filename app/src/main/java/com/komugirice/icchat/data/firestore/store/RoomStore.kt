package com.komugirice.icchat.data.firestore.store

import android.graphics.Insets.add
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.data.firestore.Room
import com.komugirice.icchat.data.firestore.User
import com.komugirice.icchat.data.firestore.manager.UserManager
import com.komugirice.icchat.util.FireStoreUtil
import java.util.*

class RoomStore {
    companion object {

        /**
         * getLoginUserRoomsメソッド
         *
         * @param pRooms ログインユーザのRoomペアオブジェクトを設定します。
         *
         */
        fun getLoginUserRooms(pRooms: MutableLiveData<MutableList<Room>>) {
            val userId = UserManager.myUserId
            var rooms: MutableList<Room> = mutableListOf()
            // rooms取得
            FirebaseFirestore.getInstance()
                .collection("rooms")
                .get()
                .addOnSuccessListener {

                    var tempRooms = it.toObjects(Room::class.java)

                    // roomsに紐づくfriends取得
                    tempRooms.forEach {
                        if (it.userIdList.contains(userId))
                            rooms.add(it)
                        // 最後の要素確認
                    }
                    pRooms.postValue(rooms)
                }
        }

        /**
         * ログインユーザと友だちのサシのチャットの重複チェックを行い、存在しなかったら登録する。
         * @param rooms: MutableList<Room>      ログインユーザのルームリスト
         * @param targetUserList: List<User>    デバッグユーザ登録リスト
         *
         *
         */
        fun registerDebugRooms(rooms: MutableList<Room>?, targetUserList: List<User>){
            val loginUserId = UserManager.myUserId
            targetUserList.forEach targetUserLoop@{ targetUser ->

                val tmpUserIdList = mutableListOf(loginUserId, targetUser.userId)

                // 全てのroomでサシチャットの重複対象チェック
                rooms?.forEach {
                    if(it.userIdList.size == tmpUserIdList.size
                        && it.userIdList.toList().containsAll(tmpUserIdList)) {
                        // 重複
                        return@targetUserLoop

                    }
                }
                // 重複しない場合、新規登録
                val room = Room().apply {
                    name = targetUser.name
                    documentId = UUID.randomUUID().toString()
                    userIdList = tmpUserIdList
                }
                FirebaseFirestore.getInstance()
                    .collection("rooms")
                    .document(room.documentId)
                    .set(room)
            }
        }

        /**
         * getTargetUserRoomメソッド
         *
         * @param userId 取得対象ユーザID
         * @return retRoom 取得対象ユーザのRoomを設定します
         */
        fun getTargetUserRoom(userId: String, retRoom: MutableLiveData<Room>) {
            // rooms取得
            FirebaseFirestore.getInstance()
                .collection("rooms")
                .get()
                .addOnSuccessListener {

                    var tempRooms = it.toObjects(Room::class.java)

                    // 引数のuserIdに紐づくRoom取得
                    tempRooms.forEach {
                        if (it.userIdList.contains(userId)) {
                            retRoom.postValue(it)
                            return@addOnSuccessListener
                        }
                    }

                }
        }
    }
}