package com.komugirice.icchat.util

import androidx.lifecycle.MutableLiveData
import com.example.qiitaapplication.extension.getIdFromEmail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.data.firestore.model.User
import com.komugirice.icchat.data.firestore.manager.UserManager

class FireStoreUtil {
    companion object {

        /**
         * ログインユーザIDを返却します。
         *
         * @param ログインユーザID
         */
        fun getLoginUserId(): String {
            val user = FirebaseAuth.getInstance().currentUser
            user?.also {
            } ?: run {
                return ""
            }
            val userId = user.email?.also{return it.getIdFromEmail()} ?: ""
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