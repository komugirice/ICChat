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

        fun registerDebugUsers(context: Context?) {
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
                    registerDebugUsers(context, currentUserPairs)
                }
        }

        private fun registerDebugUsers(context: Context?, currentUsers: List<Pair<String, User>>) {
            var thisUserlist: MutableList<User> = mutableListOf()
            //userIdが000000〜000009のユーザだけ作成。
            for (i in 0..9) {
                thisUserlist.add(
                    User().apply {
                        userId = "00000" + i.toString()
                        name = "ユーザ_" + "00000" + i.toString()
                        val birthString =
                            ("199" + i.toString() + "/" + (i + 1).toString() + "/" + (i + 1).toString())
                        birthDay = birthString.toDate("yyyy/MM/dd")
                    }
                )
            }
            val currentUserIds = currentUsers.map { it.second.userId }
            thisUserlist.forEach { user ->
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
            Toast.makeText(
                context,
                "デバッグユーザ登録が完了しました。",
                Toast.LENGTH_LONG
            ).show()
        }

        fun getDebugNotFriendIdArray(
            context: Context?,
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