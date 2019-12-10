package com.komugirice.icchat.data.firestore.store

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.example.qiitaapplication.extension.toDate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.data.firestore.User
import com.komugirice.icchat.data.firestore.manager.UserManager
import com.komugirice.icchat.util.FireStoreUtil
import java.util.*

class UserStore {
    companion object {

        /**
         * ログインユーザのUserオブジェクト取得
         *
         * @return user
         *
         */
        fun getLoginUser(user: MutableLiveData<User>) {
            val myUserId = UserManager.myUserId
            FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("userId", myUserId)
                .get()
                .addOnCompleteListener {
                    it.result?.toObjects(User::class.java)?.firstOrNull().also {
                        user.postValue(it)
                    }
                }
        }

        /**
         * デバッグ用ユーザリスト取得
         *
         * @return MutableList<User> デバッグユーザリスト
         *
         */
        fun getDebugUserList(): MutableList<User> {
            var debugUserlist: MutableList<User> = mutableListOf()
            var userIdList = mutableListOf<String>().apply {
                for (i in 0..9) {
                    this.add("00000" + i.toString())
                }
            }
            //userIdが000000〜000009のユーザだけ作成。
            userIdList.forEach {

                val tmp = User().apply {
                    userId = it
                    name = "ユーザ_" + it
                    val i = it.substring(it.length - 1, it.length)
                    val birthString =
                        ("199" + i + "/" + (i + 1).toString() + "/" + (i + 1).toString())
                    birthDay = birthString.toDate("yyyy/MM/dd")
                    friendIdList.add(UserManager.myUserId)
                    documentId = UUID.randomUUID().toString()
                }
                // ログインユーザID専用
                if (it.equals(UserManager.myUserId)) {
                    tmp.documentId = FirebaseAuth.getInstance().currentUser?.uid.toString()
                    tmp.friendIdList.clear()
                    //tmp.friendIdList = userIdList.filter{ it!= UserManager.myUserId}.toMutableList()
                }


                debugUserlist.add(tmp)

            }

            return debugUserlist
        }

        /**
         * デバッグ用ユーザリスト登録
         *
         */
        fun registerDebugUsers() {
            // まず登録済のユーザリストを取得
            FirebaseFirestore.getInstance()
                .collection("users")
                .get()
                .addOnCompleteListener {
                    var currentUserPairs = mutableListOf<Pair<String, User>>()
                    var userIdDocumentIdMap = mutableMapOf<String, String>()
                    if (it.isSuccessful) {
                        it.result?.documents?.forEach { documentSnapshot ->
                            // documentSnapshot.idはデータ更新の主キー
                            userIdDocumentIdMap[documentSnapshot.get("userId").toString()] =
                                documentSnapshot.id
                        }
                        it.result?.toObjects(User::class.java)?.also {
                            it.map {
                                // Userクラス自身のuserIdで紐付けている
                                val documentId = userIdDocumentIdMap[it.userId]
                                //documentIdが無い場合はmapに含めない
                                if (documentId == null)
                                    null
                                else
                                    Pair(documentId, it)
                            }.forEach {
                                if (it != null)
                                    currentUserPairs.add(it)
                            }
                        }
                    }
                    registerDebugUsers(currentUserPairs)
                }
        }

        /**
         * デバッグ用ユーザリスト登録
         *
         * @param currentUsers List<Pair<String, User>> 登録済
         *
         */
        private fun registerDebugUsers(currentUsers: List<Pair<String, User>>) {
            // 登録対象のデバッグユーザを取得
            var debugUserlist: MutableList<User> = getDebugUserList()

            val currentUserIds = currentUsers.map { it.second.userId }
            debugUserlist.forEach { user ->
                // 重複チェック
                if (currentUserIds.contains(user.userId)) {
                    // userIdが一致した先頭レコードのdocumentIdを設定する
                    val documentId =
                        currentUsers.firstOrNull { it.second.userId == user.userId }?.first
                            ?: UUID.randomUUID().toString()
                    // documentIdをキーにして更新
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(documentId)
                        .set(user)
                } else {
                    // 新規登録（documentIdが主キー）
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(user.documentId)
                        .set(user)
                }
            }

        }

        /**
         * フレンドではないユーザリスト取得
         *
         * @param friendIdList フレンドであるuserId配列
         * @param notFriendIdArray フレンドではないuserId配列
         *
         */
        fun getDebugNotFriendIdArray(
            friendIdList: MutableList<String>,
            notFriendIdArray: MutableLiveData<Array<CharSequence>>
        ) {
            var notFriendList: MutableList<User> = mutableListOf()
            // user情報取得
            FirebaseFirestore.getInstance()
                .collection("users")
                // TODO なぜかwhereがうまくいかない。
                //.whereGreaterThan("userId", userId)
                //.whereLessThan("userId", userId)
                .orderBy(User::userId.name)
                //.limit(10)
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result?.toObjects(User::class.java)?.also {
                            // とりあえずuser全件をnotFriendListに格納
                            notFriendList = it
                        }
                        var notFriendIdList: MutableList<String> =
                            notFriendList.map { it.userId }.toMutableList()
                        // notFriendIdListからfirendIdListを除外
                        notFriendIdList.removeAll(friendIdList)
                        // 自分のIDも除外
                        notFriendIdList.remove(UserManager.myUserId)

                        // Spinner.adapterがarrayしか受け付けないので変換
                        notFriendIdArray.postValue(notFriendIdList.toTypedArray())

                    }
                }
        }

        /**
         * 友だち追加
         * 追加していない友だちしか呼び出せない設計
         *
         * @return user
         *
         */
        fun addFriend(context: Context?, friendId: String) {
            val myUserId = UserManager.myUserId
            // UserManagerのMyUser出来たら消す。
            FirebaseFirestore.getInstance()
                .collection("users")
                .whereEqualTo("userId", myUserId)
                .get()
                .addOnCompleteListener {
                    it.result?.toObjects(User::class.java)?.firstOrNull().also {
                        it?.also {
                            it.friendIdList.contains(friendId).not().apply {
                                it.friendIdList.add(friendId)
                            }
                            // friend追加の後更新
                            FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(it.documentId)
                                .set(it)

                            // 友だち側登録
                            FirebaseFirestore.getInstance()
                                .collection("users")
                                .whereEqualTo("userId", friendId)
                                .get()
                                .addOnCompleteListener {
                                    it.result?.toObjects(User::class.java)?.firstOrNull().also {
                                        it?.also {
                                            it.friendIdList.contains(myUserId).not().apply {
                                                it.friendIdList.add(myUserId)
                                            }
                                            // friend追加の後更新
                                            FirebaseFirestore.getInstance()
                                                .collection("users")
                                                .document(it.documentId)
                                                .set(it)


                                            Toast.makeText(
                                                context,
                                                "友だち追加が完了しました。",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }

                                }
                        }

                    }
                }
        }
    }
}