package com.komugirice.icchat.util

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.qiitaapplication.extension.getIdFromEmail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.data.firestore.Friend
import com.komugirice.icchat.fragment.FriendFragment

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
            val myUserId = FireStoreUtil.getLoginUserId()
            var retFriendList: MutableList<Friend> = mutableListOf()
            FirebaseFirestore.getInstance()
                .collection("friends/$myUserId/friends")
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful)
                        it.result?.toObjects(Friend::class.java)?.also {
//                            it.forEach {
//                                Log.d("getFriend", "friendUserId:${it.userId}")
//                            }
                            friendList.postValue(it.map{it.userId}.toMutableList())
                        }
                }
        }

    }
}