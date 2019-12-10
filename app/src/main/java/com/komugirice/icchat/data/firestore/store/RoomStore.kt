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
                    }
                    pRooms.postValue(rooms)
                }
        }

        /**
         * ログインユーザと友だちのサシのチャットの重複チェックを行い、存在しなかったら登録する。
         * @param rooms: MutableList<Room>      ログインユーザのルームリスト
         * @param targetUserId: String    対象ユーザID
         *
         *
         */
        fun registerSingleUserRooms(rooms: MutableList<Room>?, targetUserId: String){
            val loginUserId = UserManager.myUserId

            val サシリスト = mutableListOf(loginUserId, targetUserId)

            // 全てのroomでサシチャットの重複対象チェック
            rooms?.forEach {
                if(it.userIdList.size == サシリスト.size
                    && it.userIdList.toList().containsAll(サシリスト)) {
                    // 重複
                    return@forEach

                }
                // 重複しない場合、新規登録
                val room = Room().apply {
                    //name = targetUser.name
                    documentId = UUID.randomUUID().toString()
                    userIdList = サシリスト
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
         * @param targetUser 取得対象ユーザ
         * @return retRoom 取得対象ユーザのRoomを設定します
         */
        fun getTargetUserRoom(targetUser: User, retRoom: MutableLiveData<Room>) {
            val サシリスト = mutableListOf(UserManager.myUserId, targetUser.userId)
            // rooms取得
            FirebaseFirestore.getInstance()
                .collection("rooms")
                .get()
                .addOnSuccessListener {

                    var tempRooms = it.toObjects(Room::class.java)

                    // 引数のuserIdに紐づくサシチャットのRoom取得
                    tempRooms.forEach {
                        if (it.userIdList.size == サシリスト.size
                            && it.userIdList.containsAll(サシリスト)) {
                            // サシチャットはルーム名を対象ユーザ名に設定する。
                            it.name = targetUser.name
                            retRoom.postValue(it)
                            return@addOnSuccessListener
                        }
                    }

                }
        }
    }
}