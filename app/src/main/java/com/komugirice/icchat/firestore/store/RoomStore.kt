package com.komugirice.icchat.firestore.store

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.proto.MutationQueueOrBuilder
import com.komugirice.icchat.firestore.model.Room
import com.komugirice.icchat.firestore.model.User
import com.komugirice.icchat.firestore.manager.UserManager
import java.util.*

class RoomStore {
    companion object {

        /**
         * getLoginUserRoomsメソッド
         *
         * @param pRooms ログインユーザのRoomペアオブジェクトを設定します。
         *
         */
        fun getLoginUserRooms(onSuccess: (List<Room>) -> Unit) {
            var rooms = mutableListOf<Room>()
            // rooms取得
            FirebaseFirestore.getInstance()
                .collection("rooms")
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result?.toObjects(Room::class.java)?.also {
                            // roomsに紐づくfriends取得
                            it.forEach {
                                if (it.userIdList.contains( UserManager.myUserId)
                                    || it.inviteIdList.contains(UserManager.myUserId)
                                    || it.denyIdList.contains(UserManager.myUserId))
                                    rooms.add(it)
                            }

                        }
                        rooms.sortBy { it.name }
                        onSuccess.invoke(rooms)
                    }

                }
        }

        /**
         * ログインユーザと友だちのサシのチャットの重複チェックを行い、存在しなかったら登録する。
         * @param rooms: MutableList<Room>      ログインユーザのルームリスト
         * @param targetUserId: String    対象ユーザID
         *
         *
         */
        fun registerSingleUserRooms(rooms: List<Room>?, targetUserId: String){
            val loginUserId = UserManager.myUserId
            val サシリスト = mutableListOf(loginUserId, targetUserId)
            var updFlg = true

            // 全てのroomでサシチャットの重複対象チェック
            rooms?.forEach {
                if(it.userIdList.size == サシリスト.size
                    && it.userIdList.toList().containsAll(サシリスト)) {
                    // 重複
                    updFlg = false
                }
            }
            if(updFlg) {
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
         * グループルームの登録
         * @param room: Room
         * @param onComplete
         *
         */
        fun registerGroupRoom(room: Room, onComplete: (Task<Void>) -> Unit) {
            FirebaseFirestore.getInstance()
                .collection("rooms")
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
                    .collection("rooms")
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
                            onSuccess.invoke(it)
                            return@addOnSuccessListener
                        }
                    }

                }
        }

        /**
         * 対象のルームに所属するユーザ情報を取得する
         * （ログインユーザのfriends以外から取得する可能性がある）
         * ChatViewModelで使用
         *
         * @param roomId 対象のルーム
         */
        fun getTargetRoomUsers(roomId: String, onComplete: (MutableList<User>) -> Unit) {
            val userList = mutableListOf<User>()
            // rooms取得
            FirebaseFirestore.getInstance()
                .collection("rooms")
                .document(roomId)
                .get()
                .addOnSuccessListener {
                    // Room.documentId検索なので必ず成功する
                    var room: Room? = it.toObject(
                        Room::class.java
                    )
                    room?.userIdList?.apply {
                        this.remove(UserManager.myUserId)

                        if (this.isNotEmpty()) {
                            this.forEach {
                                FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .whereEqualTo("userId", it)
                                    .get()
                                    .addOnCompleteListener {
                                        it.result?.toObjects(User::class.java)?.firstOrNull().also {
                                            it?.apply { userList.add(it) }
                                        }
                                        if (userList.size == room.userIdList.size)
                                            onComplete.invoke(userList)
                                    }

                            }
                        } else {
                            // userListが0件の可能性はある
                            onComplete.invoke(mutableListOf())
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
                .collection("rooms")
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

            room.inviteIdList.remove(userId)
            room.userIdList.add(userId)

            registerGroupRoom(room) {
                onComplete.invoke(it)
            }

        }

        /**
         * 拒否リストに追加する
         *
         * @param roomId 対象のルーム
         * @param userId 対象のユーザ
         * @param onComplete
         */
        fun denyGroup(room: Room, userId: String, onComplete: (Task<Void>) -> Unit) {
            if(room.isGroup == false) return

            room.inviteIdList.remove(userId)
            room.denyIdList.add(userId)

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

        /**
         * 拒否リストから削除する
         *
         * @param roomId 対象のルーム
         * @param userId 対象のユーザ
         * @param onComplete
         */
        fun cancelDenyGroup(room: Room, userId: String, onComplete: (Task<Void>) -> Unit) {
            if(room.isGroup == false) return

            room.denyIdList.remove(userId)

            registerGroupRoom(room) {
                onComplete.invoke(it)
            }

        }
    }
}