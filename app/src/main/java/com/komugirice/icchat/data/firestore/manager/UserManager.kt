package com.komugirice.icchat.data.firestore.manager

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.example.qiitaapplication.extension.toDate
import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.R
import com.komugirice.icchat.data.firestore.User
import com.komugirice.icchat.util.FireStoreUtil
import kotlinx.android.synthetic.main.fragment_debug.*
import java.util.*

class UserManager {
    companion object {

        fun getDebugUserList(): MutableList<User> {
            var debugUserlist: MutableList<User> = mutableListOf()
            //userIdが000000〜000009のユーザだけ作成。
            for (i in 0..9) {

                val userIdStr = "00000" + i.toString()
                // ログインユーザIDは除外
                if (userIdStr.equals(FireStoreUtil.getLoginUserId()))
                    continue

                debugUserlist.add(
                    User().apply {
                        userId = userIdStr
                        name = "ユーザ_" + "00000" + i.toString()
                        val birthString =
                            ("199" + i.toString() + "/" + (i + 1).toString() + "/" + (i + 1).toString())
                        birthDay = birthString.toDate("yyyy/MM/dd")
                    }
                )
            }

            return debugUserlist
        }

        fun registerDebugUsers() {
            // まず登録済のユーザリストを取得
            FirebaseFirestore.getInstance()
                .collection("user")
                .get()
                .addOnCompleteListener {
                    var currentUserPairs = mutableListOf<Pair<String, User>>()
                    var userIdDocumentIdMap = mutableMapOf<String, String>()
                    if (it.isSuccessful) {
                        it.result?.documents?.forEach { documentSnapshot ->
                            // documentSnapshot.idがデータ更新できる主キー
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

        private fun registerDebugUsers(currentUsers: List<Pair<String, User>>) {
            // 登録対象のデバッグユーザを取得
            var thisUserlist: MutableList<User> = getDebugUserList()

            val currentUserIds = currentUsers.map { it.second.userId }
            thisUserlist.forEach { user ->
                // 重複チェック
                if (currentUserIds.contains(user.userId)) {
                    // userIdが一致した先頭レコードのdocumentIdを設定する
                    val documentId =
                        currentUsers.firstOrNull { it.second.userId == user.userId }?.first
                            ?: UUID.randomUUID().toString()
                    // documentIdをキーにして更新
                    FirebaseFirestore.getInstance()
                        .collection("user")
                        .document(documentId)
                        .set(user)
                } else {
                    // 新規登録（documentIdが主キー）
                    FirebaseFirestore.getInstance()
                        .collection("user")
                        .document(user.documentId)
                        .set(user)
                }
            }

        }

        fun getDebugNotFriendIdArray(
            friendIdList: MutableList<String>,
            notFriendIdArray: MutableLiveData<Array<CharSequence>>
        ) {
            var notFriendList: MutableList<User> = mutableListOf()
            // user情報取得
            FirebaseFirestore.getInstance()
                .collection("user")
                // TODO なぜかwhereがうまくいかない。
                //.whereGreaterThan("userId", userId)
                //.whereLessThan("userId", userId)
                .orderBy(User::userId.name)
                .limit(10)
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result?.toObjects(User::class.java)?.also {
                            // とりあえずuser全件をnotFriendListに格納
                            notFriendList = it
                        }
                        // notFriendList: MutableList<User> → notFriendIdList: MutableList<String>
                        var notFriendIdList: MutableList<String> =
                            notFriendList.map { it.userId }.toMutableList()
                        // notFriendIdListからfirendIdListを除外
                        notFriendIdList.removeAll(friendIdList)
                        // 自分のIDも除外
                        notFriendIdList.remove(FireStoreUtil.getLoginUserId())

                        // Spinner.adapterがarrayしか受け付けないので変換
                        notFriendIdArray.postValue(notFriendIdList.toTypedArray())

                    }
                }
        }

    }
}