package com.komugirice.icchat.firebase.firestore.store

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.komugirice.icchat.firebase.firestore.model.Room
import com.komugirice.icchat.firebase.firestore.model.User
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import timber.log.Timber
import java.util.*

class RoomStore {
    companion object {
        const val ROOMS = "rooms"
        /**
         * getLoginUserRoomsメソッド
         *
         * @param pRooms ログインユーザのRoomリストオブジェクトを設定します。
         *
         */
        fun getLoginUserRooms(onSuccess: (List<Room>) -> Unit) {
            val myUserId = UserManager.myUserId
            var rooms = mutableListOf<Room>()
            // rooms取得
            FirebaseFirestore.getInstance()
                .collection("$ROOMS")
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result?.toObjects(Room::class.java)?.also {
                            if(it.isEmpty()) {
                                onSuccess.invoke(rooms)
                                return@addOnCompleteListener
                            }
                            // roomsに紐づくfriends取得
                            var index = 0
                            it.forEach { room->
                                index++
                                if (room.userIdList.contains(myUserId))
                                    rooms.add(room)

                                if(index == it.size) {
                                    rooms.sortBy { it.name }
                                    onSuccess.invoke(rooms)
                                }
                            }
                        }

                    }

                }
        }

        /**
         * getAllGroupRoomsメソッド
         *
         * @param onSuccess: (List<Room>)
         *
         */
        fun getAllGroupRooms(onSuccess: (List<Room>) -> Unit) {
            var rooms = mutableListOf<Room>()
            // rooms取得
            FirebaseFirestore.getInstance()
                .collection("$ROOMS")
                .whereEqualTo("group", true)
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result?.toObjects(Room::class.java)?.also {
                            rooms = it
                            rooms.sortBy { it.name }
                            onSuccess.invoke(rooms)
                        }
                    }

                }
        }

        /**
         * ログインユーザと友だちのシングルルームの重複チェックを行い、存在しなかったら登録する。
         * @param targetUserId: String    対象ユーザID
         * @param onSuccess
         *
         */
        fun registerSingleRoom(targetUserId: String, onFailed: () -> Unit, onSuccess: (Task<Void>) -> Unit){
            val loginUserId = UserManager.myUserId
            val サシリスト = mutableListOf(loginUserId, targetUserId)

            getLoginUserRooms(){
                // 全てのroomでサシチャットの重複対象チェック
                it?.forEach {
                    if(it.userIdList.size == サシリスト.size
                        && it.userIdList.toList().containsAll(サシリスト)) {
                        // 重複
                        Timber.d("registerSingleRoom 重複エラー ${Gson().toJson(it.userIdList)}")
                        onFailed.invoke()
                    }
                }
                // 重複しない場合、新規登録
                val room = Room().apply {
                    //name = targetUser.name
                    documentId = UUID.randomUUID().toString()
                    userIdList = サシリスト
                }
                FirebaseFirestore.getInstance()
                    .collection("$ROOMS")
                    .document(room.documentId)
                    .set(room)
                    .addOnCompleteListener {
                        if(it.isSuccessful) {
                            onSuccess.invoke(it)
                        }
                    }
            }


        }

        /**
         * グループルームの登録
         * @param room: Room
         * @param onComplete
         *
         */
        fun registerGroupRoom(room: Room, onComplete: (Task<Void>) -> Unit) {
            FirebaseFirestore.getInstance()
                .collection("$ROOMS")
                .document(room.documentId)
                .set(room)
                .addOnCompleteListener{
                    onComplete.invoke(it)
                }
        }

        /**
         * ログインユーザと友だちのサシのチャットの重複チェックを行い、存在したら削除する。
         * @param rooms: MutableList<Room>      ログインユーザのルームリスト
         * @param targetUserId: String    対象ユーザID
         *
         *
         */
        fun delSingleUserRooms(rooms: List<Room>?, targetUserId: String){
            val loginUserId = UserManager.myUserId
            val サシリスト = mutableListOf(loginUserId, targetUserId)
            var updFlg = false
            lateinit var targetRoom: Room

            // 全てのroomでサシチャットの重複対象チェック
            rooms?.forEach {
                if(it.userIdList.size == サシリスト.size
                    && it.userIdList.toList().containsAll(サシリスト)) {
                    // 存在
                    updFlg = true
                    targetRoom = it
                }
            }
            if(updFlg) {
                // 存在する場合、削除
                FirebaseFirestore.getInstance()
                    .collection("$ROOMS")
                    .document(targetRoom.documentId)
                    .delete()
            }

        }

        /**
         * getTargetUserRoomメソッド
         *
         * @param targetUser 取得対象ユーザ
         * @return retRoom 取得対象ユーザのRoomを設定します
         */
        fun getTargetUserRoom(targetUser: User, onSuccess: (Room) -> Unit) {
            val サシリスト = mutableListOf(UserManager.myUserId, targetUser.userId)
            // rooms取得
            FirebaseFirestore.getInstance()
                .collection("$ROOMS")
                .get()
                .addOnSuccessListener {

                    var tempRooms = it.toObjects(Room::class.java)

                    // 引数のuserIdに紐づくサシチャットのRoom取得
                    tempRooms.forEach {
                        if (it.userIdList.size == サシリスト.size
                            && it.userIdList.containsAll(サシリスト)) {
                            // サシチャットはルーム名を対象ユーザ名に設定する。
                            it.name = targetUser.name
                            onSuccess.invoke(it)
                            return@addOnSuccessListener
                        }
                    }

                }
        }

        /**
         * 対象のルームを削除する
         *
         * @param roomId 対象のルーム
         * @param onComplete
         */
        fun deleteRoom(roomId: String, onComplete: (Task<Void>) -> Unit) {
            FirebaseFirestore.getInstance()
                .collection("$ROOMS")
                .document(roomId)
                .delete()
                .addOnCompleteListener {
                    onComplete.invoke(it)
                }
        }

        /**
         * グループメンバーに追加する
         *
         * @param roomId 対象のルーム
         * @param userId 対象のユーザ
         * @param onComplete
         */
        fun acceptGroupMember(room: Room, userId: String, onComplete: (Task<Void>) -> Unit) {
            if(room.isGroup == false) return

            room.userIdList.add(userId)

            registerGroupRoom(room) {
                onComplete.invoke(it)
            }

        }

        /**
         * グループメンバーから外す
         *
         * @param roomId 対象のルーム
         * @param userId 対象のユーザ
         * @param onComplete
         */
        fun removeGroupMember(room: Room, userId: String, onComplete: (Task<Void>) -> Unit) {
            if(room.isGroup == false) return

            room.userIdList.remove(userId)

            registerGroupRoom(room) {
                onComplete.invoke(it)
            }

        }
    }
}