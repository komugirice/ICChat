package com.komugirice.icchat.data.firestore.store

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.example.qiitaapplication.extension.toDate
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.komugirice.icchat.ICChatApplication.Companion.isDevelop
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
        fun getLoginUser(onComplete: (Task<QuerySnapshot>) -> Unit) {
            val myDocumentId = FirebaseAuth.getInstance().currentUser?.uid.toString()
            FirebaseFirestore.getInstance()
                .collection("users")
                .whereArrayContains("uids",myDocumentId)
                .get()
                .addOnCompleteListener {
                    onComplete.invoke(it)
                }
        }

        /**
         * 全Userオブジェクト取得
         * UserManagerに反映する
         *
         *
         */
        fun getAllUsers() {
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
                .document(UserManager.myUser.userId)
                .update("friendIdList", UserManager.myUser.friendIdList)


            val friend = UserManager.myFriends.filter {
                it.userId.equals(friendId)
            }.first()

            if(!friend.friendIdList.contains(UserManager.myUserId)) {
                friend.friendIdList.add(UserManager.myUserId)

                // 友だち側登録
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(friend.userId)
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
                userId = if(UserManager.myUserId.isNotEmpty()) UserManager.myUserId else FireStoreUtil.createLoginUserId()
                uids.add(loginUserUID)
            }

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.userId)
                .set(user)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        UserManager.myUser = user
                    }
                }

        }
    }
}