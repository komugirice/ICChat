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
        fun getLoginUserRooms(onComplete: (Task<QuerySnapshot>) -> Unit) {
            // rooms取得
            FirebaseFirestore.getInstance()
                .collection("rooms")
                .get()
                .addOnCompleteListener {
                    onComplete.invoke(it)
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

        /**
         * 対象のルームに所属するユーザ情報を取得する
         * （UserManagerのfriends以外から取得する可能性がある）
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
                    var room: Room? = it.toObject(
                        Room::class.java)
                    room?.userIdList?.apply{
                        this.remove(UserManager.myUserId)
                        this.forEach{
                            FirebaseFirestore.getInstance()
                                .collection("users")
                                .whereEqualTo("userId", it)
                                .get()
                                .addOnCompleteListener {
                                    it.result?.toObjects(User::class.java)?.firstOrNull().also {
                                        it?.apply{userList.add(it)}
                                    }
                                    if (userList.size == room.userIdList.size)
                                        onComplete.invoke(userList)
                                }

                        }
                    }

                }
        }
    }
}