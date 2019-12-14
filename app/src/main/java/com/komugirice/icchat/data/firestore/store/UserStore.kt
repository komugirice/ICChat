package com.komugirice.icchat.data.firestore.store

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.example.qiitaapplication.extension.toDate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.data.firestore.model.User
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
         * 全Userオブジェクト取得
         * UserManagerに反映する
         *
         *
         */
        fun getAllUsers() {
            val myUserId = UserManager.myUserId
            FirebaseFirestore.getInstance()
                .collection("users")
                .get()
                .addOnCompleteListener {
                    it.result?.toObjects(User::class.java)?.also {
                        UserManager.allUsers = it
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
         */
        fun addFriend(context: Context?, friendId: String) {
            if(UserManager.myUser.friendIdList.contains(friendId)) {
                Toast.makeText(
                    context,
                    "既に登録済みです。",
                    Toast.LENGTH_LONG
                ).show()
                return
            }


            UserManager.addMyFriends(friendId)

            // ログインユーザ側登録
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(UserManager.myUser.documentId)
                .update("friendIdList", UserManager.myUser.friendIdList)


            val friend = UserManager.myFriends.filter {
                it.userId.equals(friendId)
            }.first()

            if(!friend.friendIdList.contains(UserManager.myUserId)) {
                friend.friendIdList.add(UserManager.myUserId)

                // 友だち側登録
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(friend.documentId)
                    .update("friendIdList", friend.friendIdList)
                    .addOnCompleteListener {

                    if (it.isSuccessful) {
                        Toast.makeText(
                            context,
                            "友だち追加が完了しました。",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }


        /**
         * ログインユーザ情報登録
         *
         *
         */
        fun registerLoginUser() {
            val loginUserUID = FirebaseAuth.getInstance().currentUser?.uid.toString()
            val user = User().apply {
                userId = FireStoreUtil.getLoginUserId()
                documentId = loginUserUID
            }

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(loginUserUID)
                .set(user)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        UserManager.myUser = user
                    }
                }

        }
    }
}