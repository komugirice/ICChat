package com.komugirice.icchat.data.firestore.manager

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.qiitaapplication.extension.toDate
import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.data.firestore.Friend
import com.komugirice.icchat.data.firestore.Room
import com.komugirice.icchat.data.firestore.User
import com.komugirice.icchat.util.FireStoreUtil
import java.util.*

class RoomManager {
    companion object {

        /**
         * getLoginUserRoomsメソッド
         *
         * @param pRooms ログインユーザのRoomペアオブジェクトを設定します。
         *
         */
        fun getLoginUserRooms(pRooms: MutableLiveData<MutableList<Pair<Room, List<Friend>>>>) {
            val userId = FireStoreUtil.getLoginUserId()
            var rooms: MutableList<Pair<Room, List<Friend>>> = mutableListOf()
            // rooms取得
            FirebaseFirestore.getInstance()
                .collection("rooms/$userId/rooms")
                .get()
                .addOnSuccessListener {

                    var tempRooms = it.toObjects(Room::class.java)

                    // roomsに紐づくfriends取得
                    tempRooms.also{
                        it.forEach {
                            val room = it
                            FirebaseFirestore.getInstance()
                                .collection("rooms/$userId/rooms/${it.documentId}/friends")
                                .get()
                                .addOnSuccessListener {
                                    var friends = it.toObjects(Friend::class.java)

                                    rooms.add(
                                        Pair(room, friends)
                                    )
                                    // 最後の要素確認
                                    if (tempRooms.size == rooms.size)
                                        pRooms.postValue(rooms)

                                }
                        }
                    }.run {
                        pRooms.postValue(rooms)
                    }

                }
        }

        /**
         * ログインユーザと友だちのサシのチャットの重複チェックを行い、存在しなかったら登録する。
         * @param rooms: MutableList<Pair<Room, List<Friend>>>      ログインユーザのルームリスト
         * @param targetUser: List<User>    デバッグユーザ登録リスト
         *
         *
         */
        fun registerDebugRooms(rooms: MutableList<Pair<Room, List<Friend>>>?, targetUser: List<User>){
            val loginUserId = FireStoreUtil.getLoginUserId()
            targetUser.forEach targetUserLoop@{ user ->

                // 全てのroomでサシチャットの重複対象チェック
                rooms?.forEach {

                    if(it.second.size == 1
                        && it.second[0].userId.equals(user.userId)) {
                        // 重複
                        return@targetUserLoop

                    }
                }
                // 重複しない場合、新規登録
                val room = Room().apply {
                    name = user.name
                    documentId = UUID.randomUUID().toString()
                }
                FirebaseFirestore.getInstance()
                    .collection("rooms/$loginUserId/rooms")
                    .document(room.documentId)
                    .set(room)

                val friend = Friend().apply { userId = user.userId }
                FirebaseFirestore.getInstance()
                    .collection("rooms/$loginUserId/rooms/${room.documentId}/friends")
                    .document(UUID.randomUUID().toString())
                    .set(friend)

            }
        }

        /**
         * getTargetUserRoomメソッド
         *
         * @param userId 取得対象ユーザID
         * @return retRoom 取得対象ユーザのRoomを設定します
         */
        fun getTargetUserRoom(userId: String, retRoom: MutableLiveData<Pair<Room, List<Friend>>>) {
            val loginUserId = FireStoreUtil.getLoginUserId()
            //var liveRoomPairs = MutableLiveData<MutableList<Pair<Room, List<Friend>>>>()
            var roomPairs: MutableList<Pair<Room, List<Friend>>> = mutableListOf()

            // rooms取得
            FirebaseFirestore.getInstance()
                .collection("rooms/$loginUserId/rooms")
                .get()
                .addOnSuccessListener{

                    var tempRooms = it.toObjects(Room::class.java)


                    // roomsに紐づくfriends取得
                    tempRooms.forEach {
                        if(retRoom.value != null) {
                            return@addOnSuccessListener
                        }

                        val room = it
                        FirebaseFirestore.getInstance()
                            .collection("rooms/$loginUserId/rooms/${it.documentId}/friends")
                            .get()
                            .addOnSuccessListener {
                                var friends = it.toObjects(Friend::class.java)

                                roomPairs.add(
                                    Pair(room, friends)
                                )
                                // 最後の要素確認
                                if (tempRooms.size == roomPairs.size) {
                                    //liveRoomPairs.postValue(roomPairs)

                                    // 該当のroomチェック
                                    roomPairs.forEach {

                                        if (it.second.size == 1
                                            && it.second[0].userId.equals(userId))

                                            retRoom.postValue(it)

                                    }
                                }

                            }

                     }
                }
        }
    }
}