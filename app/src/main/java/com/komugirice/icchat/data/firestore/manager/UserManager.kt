package com.komugirice.icchat.data.firestore.manager

import androidx.databinding.library.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.komugirice.icchat.data.firestore.model.User
import com.komugirice.icchat.data.firestore.store.UserStore

object UserManager {

    var myUserId = "" // ここにSharedPreferencesから取得する

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
            value.firstOrNull { it.uids.contains(FirebaseAuth.getInstance().currentUser?.uid) }?.also {
                myUser = it
            }
        }

    var myFriends = listOf<User>()

    fun addMyFriends (friendId: String){
        myUser.friendIdList.add(friendId)
        myFriends = allUsers.filter { myUser.friendIdList.contains(it.userId) }
    }

    /**
     * UserManager初期化
     * @param user: User
     */
    fun initUserManager(user: User) {
        UserManager.myUserId = user.userId
        UserManager.myUser = user
        // TODO 非同期大丈夫？
        UserStore.getAllUsers()
    }
}