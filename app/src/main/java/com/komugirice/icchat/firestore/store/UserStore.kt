package com.komugirice.icchat.firestore.store

import android.content.Context
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.komugirice.icchat.firestore.model.User
import com.komugirice.icchat.firestore.manager.UserManager

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
        fun getAllUsers(onComplete: (Task<QuerySnapshot>) -> Unit) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .get()
                .addOnCompleteListener {
                    onComplete.invoke(it)
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
         * 友だち削除
         *
         */
        fun delFriend(friendId: String, onComplete: (Task<Void>) -> Unit) {
            // フレンドユーザ取得
            val friend = UserManager.myFriends.filter {
                it.userId.equals(friendId)
            }.first()

            UserManager.removeMyFriends(friendId)

            // ログインユーザ側更新
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(UserManager.myUser.userId)
                .update("friendIdList", UserManager.myUser.friendIdList)

            // フレンドユーザ側更新
            friend.apply{
                friend.friendIdList.remove(UserManager.myUserId)
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(friend.userId)
                    .update("friendIdList", friend.friendIdList)
                    .addOnCompleteListener {
                        onComplete.invoke(it)
                    }
            }
        }

        /**
         * ユーザ情報登録
         *
         *
         */
        fun registerUser(user: User, onSuccess: (Task<Void>) -> Unit) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.userId)
                .set(user)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        onSuccess.invoke(it)
                    }
                }
        }

        /**
         * uid追加
         *
         */
        fun addUid(context: Context?, onSuccess: (Void) -> Unit) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
            if (UserManager.myUser.uids.contains(uid)) {
                Toast.makeText(
                    context,
                    "既に連携済みです。",
                    Toast.LENGTH_LONG
                ).show()
                return
            }

            UserManager.myUser.uids.add(uid)

            // ログインユーザ側登録
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(UserManager.myUser.userId)
                .update("uids", UserManager.myUser.uids)
                .addOnSuccessListener {
                    onSuccess.invoke(it)
                }
        }

        /**
         * ターゲットユーザのUserオブジェクト取得
         *
         * @param userID
         * @param onSuccess
         *
         */
        fun getTargetUser(userId: String, onSuccess: (User) -> Unit) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result?.toObject(User::class.java)?.also {
                            onSuccess.invoke(it)
                        }

                    }
                }
        }

    }
}