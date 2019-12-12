package com.komugirice.icchat.data.firestore.manager

import com.google.firebase.auth.FirebaseAuth
import com.komugirice.icchat.BuildConfig
import com.komugirice.icchat.data.firestore.User
import com.komugirice.icchat.util.FireStoreUtil

object UserManager {

    val myUserId = FireStoreUtil.getLoginUserId() // ここにSharedPreferencesから取得する

    var myUser = User()
        set(value) {
            field = value
            myFriends = allUsers.filter { value.friendIdList.contains(it.userId) }
        }

    var allUsers = listOf<User>()
        set(value) {
            if (value.isEmpty())
                return
            if (myUserId.isEmpty() && BuildConfig.DEBUG)
                throw RuntimeException("myUserIdを入れてからじゃないとダメ!!")
            field = value
            value.firstOrNull { it.documentId == FirebaseAuth.getInstance().currentUser?.uid ?: "" }?.also {
                myUser = it
            }
        }

    var myFriends = listOf<User>()
}