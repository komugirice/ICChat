package com.komugirice.icchat.data.firestore.store

import com.example.qiitaapplication.extension.toDate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.data.firestore.manager.UserManager
import com.komugirice.icchat.data.firestore.model.User
import java.util.*

class DebugUserStore {
    companion object {
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
                    //friendIdList.add(UserManager.myUserId)
                    // TODO authのUIDの取得が必要

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
    }
}