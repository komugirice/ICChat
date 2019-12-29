package com.komugirice.icchat.firestore.manager

import androidx.databinding.library.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.komugirice.icchat.firestore.model.User
import com.komugirice.icchat.firestore.store.UserStore

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

    fun removeMyFriends (friendId: String) {
        myUser.friendIdList.remove(friendId)
        myFriends = allUsers.filter { myUser.friendIdList.contains(it.userId) }
    }

    /**
     * UserManager初期化
     * FirebaseAuthのcurrentUserが取得出来る前提
     * @param user: User
     */
    fun initUserManager(onComplete: () -> Unit) {
        UserStore.getLoginUser {
            it.result?.toObjects(User::class.java)?.firstOrNull().also {
                it?.also {
                    myUserId = it.userId
                    myUser = it
                    // TODO 非同期大丈夫？
                    UserStore.getAllUsers(){
                        it.result?.toObjects(User::class.java)?.also {
                            UserManager.allUsers = it
                        }
                        onComplete.invoke()
                    }
                }
            } ?: run {
                if(BuildConfig.DEBUG)
                    throw RuntimeException("initUserManager(): currentUserがnull")
            }

        }
    }
}