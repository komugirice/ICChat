package com.komugirice.icchat.firebase.firestore.store

import android.content.Context
import android.widget.Toast
import com.komugirice.icchat.extension.removeAllSpace
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.komugirice.icchat.R
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import com.komugirice.icchat.firebase.firestore.model.User
import timber.log.Timber
import java.util.*

class UserStore {
    companion object {

        const val USERS = "users"

        /**
         * ログインユーザのUserオブジェクト取得
         *
         * @return user
         *
         */
        fun getLoginUser(onComplete: (Task<QuerySnapshot>) -> Unit) {
            val myDocumentId = FirebaseAuth.getInstance().currentUser?.uid.toString()
            FirebaseFirestore.getInstance()
                .collection("$USERS")
                .whereArrayContains("uids",myDocumentId)
                .get()
                .addOnCompleteListener {
                    onComplete.invoke(it)
                }
        }

        /**
         * ログイン済みチェック
         *
         */
        fun isAlreadyLogin(onFailuer: () -> kotlin.Unit, onSuccess: (Boolean) -> Unit) {
            getLoginUser {
                it.result?.toObjects(User::class.java)?.firstOrNull().also {
                    it?.also {
                        // 現在時刻 - 1日
                        val cal = Calendar.getInstance()
                        cal.add(Calendar.DAY_OF_MONTH, -1)
                        it.loginDateTime?.also {
                            // 現在時刻 - 1日 > loginDateTIme ならばログイン許可
                            if (cal.time.after(it)) {
                                Timber.d("isAlreadyLogin:現在時刻 - 1日 > loginDateTIme ログイン可")
                                onSuccess.invoke(false)
                            } else {
                                Timber.d("isAlreadyLogin:現在時刻 - 1日 <= loginDateTIme ログイン可")
                                onSuccess.invoke(true)
                            }
                        } ?: run {
                            // loginDateTime == null ならばログイン許可
                            Timber.d("loginDateTime == null ログイン可")
                            onSuccess.invoke(false)
                        }
                    }
                } ?: run {
                    Timber.e("isAlreadyLogin(): currentUserがユーザに紐付いてない")
                    onFailuer.invoke()
                }
            }
        }


        /**
         * ログイン日時更新
         * @param date ログイン日時
         *
         */
        fun updateLoginDateTime(date: Date?, onFailed: () -> Unit = {}, onSuccess: () -> Unit) {
            getLoginUser {
                it.result?.toObjects(User::class.java)?.firstOrNull().also {
                    it?.also {
                        FirebaseFirestore.getInstance()
                            .collection("$USERS")
                            .document(it.userId)
                            .update("loginDateTime", date)
                            .addOnSuccessListener {
                                onSuccess.invoke()
                            }
                    }
                } ?: run {
                    // 存在しないdocumentIdが出現した不具体対応
                    Timber.e("updateLoginDateTime(): 存在しないdocumentIdが出現")
                    onFailed.invoke()
                }
            }
        }

        /**
         * 全Userオブジェクト取得
         * UserManagerに反映する
         *
         */
        fun getAllUsers(onComplete: (Task<QuerySnapshot>) -> Unit) {
            FirebaseFirestore.getInstance()
                .collection("$USERS")
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
        fun addFriend(friendId: String, onFailed: () -> Unit, onSuccess: (Task<Void>) -> Unit) {
            if(UserManager.myUser.friendIdList.contains(friendId)) {
                onFailed.invoke()
                return
            }

            UserManager.addMyFriends(friendId)

            // ログインユーザ側登録
            FirebaseFirestore.getInstance()
                .collection("$USERS")
                .document(UserManager.myUser.userId)
                .update("friendIdList", UserManager.myUser.friendIdList)

            // UserManager.myFriendsが更新されていない不具合対応
            UserManager.initUserManager() {

                val friend = UserManager.myFriends.filter {
                    it.userId.equals(friendId)
                }.first()

                if(!friend.friendIdList.contains(UserManager.myUserId)) {
                    friend.friendIdList.add(UserManager.myUserId)

                    // 友だち側登録
                    FirebaseFirestore.getInstance()
                        .collection("$USERS")
                        .document(friend.userId)
                        .update("friendIdList", friend.friendIdList)
                        .addOnCompleteListener {

                            if (it.isSuccessful) {
                                onSuccess.invoke(it)
                            }
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
                .collection("$USERS")
                .document(UserManager.myUser.userId)
                .update("friendIdList", UserManager.myUser.friendIdList)
                .addOnCompleteListener {

                    // フレンドユーザ側更新
                    friend.apply {
                        friend.friendIdList.remove(UserManager.myUserId)
                        FirebaseFirestore.getInstance()
                            .collection("$USERS")
                            .document(friend.userId)
                            .update("friendIdList", friend.friendIdList)
                            .addOnCompleteListener {
                                onComplete.invoke(it)
                            }
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
                .collection("$USERS")
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
        fun addUid(onFailuer: () -> Unit, onSuccess: () -> Unit) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
            if (UserManager.myUser.uids.contains(uid)) {
                onFailuer.invoke()
                return
            }

            UserManager.myUser.uids.add(uid)

            // ログインユーザ側登録
            FirebaseFirestore.getInstance()
                .collection("$USERS")
                .document(UserManager.myUser.userId)
                .update("uids", UserManager.myUser.uids)
                .addOnSuccessListener {
                    onSuccess.invoke()
                }
        }

        /**
         * uid削除
         *
         */
        fun removeUid(uid: String?, onSuccess: () -> Unit) {


            UserManager.myUser.uids.remove(uid)
            // ログインユーザ側登録
            FirebaseFirestore.getInstance()
                .collection("$USERS")
                .document(UserManager.myUser.userId)
                .update("uids", UserManager.myUser.uids)
                .addOnSuccessListener {
                    onSuccess.invoke()
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
                .collection("$USERS")
                .document(userId)
                .get()
                // エラーになることはまずない
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result?.toObject(User::class.java)?.also {
                            onSuccess.invoke(it)
                        }

                    }
                }
        }

        fun searchNotFriendUserEmail(email: String, onFailuer:()->Unit, onSuccess: (List<User>) -> Unit) {
            if (email.isEmpty()) return
            FirebaseFirestore.getInstance()
                .collection("$USERS")
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result?.toObjects(User::class.java)?.also {
                            val ret = it.filter {
                                !UserManager.myUserId.equals(it.userId) &&  // 自分は対象外
                                        !UserManager.myUser.friendIdList.contains(it.userId) && //　friendIdListは対象外
                                        email.removeAllSpace().equals(it.email.removeAllSpace())
                            }
                            if(ret.size > 0)
                                onSuccess.invoke(ret)
                            else
                                onFailuer.invoke()
                        }
                    } else {
                        onFailuer.invoke()
                    }
                }
        }

        fun searchNotFriendUserName(name: String, onFailuer:()->Unit, onSuccess: (List<User>) -> Unit) {
            if (name.isEmpty()) return
            FirebaseFirestore.getInstance()
                .collection("$USERS")
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result?.toObjects(User::class.java)?.also {
                            val ret = it.filter {
                                !UserManager.myUserId.equals(it.userId) &&
                                        !UserManager.myUser.friendIdList.contains(it.userId) &&
                                        Regex(name.removeAllSpace()).containsMatchIn(it.name.removeAllSpace())
                            }
                            if(ret.size > 0)
                                onSuccess.invoke(ret)
                            else
                                onFailuer.invoke()
                        }
                    } else {
                        onFailuer.invoke()
                    }
                }
        }

        fun isExistUidInOtherUser(uid: String?, onSuccess: (Boolean, User?) -> Unit) {
            FirebaseFirestore.getInstance()
                .collection("$USERS")
                .whereArrayContains("uids",uid.toString())
                .get()
                .addOnCompleteListener {
                    it.result?.toObjects(User::class.java)?.firstOrNull().also {
                        it?.also {
                            it.uids.forEach { uid ->
                                if(UserManager.myUser.uids.contains(uid)) {
                                    // 自ユーザが保持
                                    onSuccess.invoke(false, null)
                                    return@addOnCompleteListener
                                }
                                if(uid == it.uids.last()) {
                                    // 他ユーザが保持
                                    onSuccess.invoke(true, it)
                                }

                            }

                        }
                    } ?: run {
                        // 誰も保持していない
                        onSuccess.invoke(false, null)
                    }
                }
        }

        fun removeOtherUserUid(uid: String?, user: User, onSuccess: () -> Unit) {
            user.uids.remove(uid)
            // uid削除
            FirebaseFirestore.getInstance()
                .collection("$USERS")
                .document(user.userId)
                .update("uids", user.uids)
                .addOnSuccessListener {
                    onSuccess.invoke()
            }
        }


        /**
         * FCMトークンの更新
         *
         * @param token
         * @param onSuccess
         *
         */
        fun updateFcmToken(token: String?, onSuccess: () -> Unit) {
            // Managerでエラーが出るのでgetLoginUserに修正
            getLoginUser {
                it.result?.toObjects(User::class.java)?.firstOrNull().also {
                    it?.also {
                        FirebaseFirestore.getInstance()
                            .collection("$USERS")
                            .document(it.userId)
                            .update("fcmToken", token)
                            .addOnSuccessListener {
                                onSuccess.invoke()
                            }
                    }
                }
            }
        }
    }
}