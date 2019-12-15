package com.komugirice.icchat.util

import androidx.lifecycle.MutableLiveData
import com.example.qiitaapplication.extension.getIdFromEmail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.BuildConfig
import com.komugirice.icchat.ICChatApplication
import com.komugirice.icchat.ICChatApplication.Companion.isDevelop
import com.komugirice.icchat.data.firestore.model.User
import com.komugirice.icchat.data.firestore.manager.UserManager
import java.util.*

class FireStoreUtil {
    companion object {

        /**
         * ログインユーザIDを返却します。
         *
         * @param ログインユーザID
         */
        fun createLoginUserId(): String {
            val user = FirebaseAuth.getInstance().currentUser
            user?.also {
            } ?: run {
                return ""
            }

            var userId = UUID.randomUUID().toString()
            if(BuildConfig.DEBUG) {
                val id = user.email?.getIdFromEmail() ?: ""
                userId = userId.replace("^.{${id.length}}".toRegex(), id)
            }

            return userId
        }

        /**
         * ログインユーザに紐づくFriendList返却します。
         *
         * @param List<FriendFragment.Friend>
         */
        fun getFriends(friendList:MutableLiveData<MutableList<String>>) {
            val myUserId = UserManager.myUserId
            FirebaseFirestore.getInstance()
                .collection("users")
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful)
                        it.result?.toObjects(User::class.java)?.also {

                            it.forEach {
                                if(it.userId.equals(myUserId))
                                    friendList.postValue(it.friendIdList.toMutableList())
                            }

                        }
                }
        }

    }
}