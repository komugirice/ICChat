package com.komugirice.icchat.firebase.firestore.manager

import androidx.databinding.library.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.komugirice.icchat.firebase.firestore.model.User
import com.komugirice.icchat.firebase.firestore.store.UserStore

object UserManager {

    var myUserId = "" // ここにSharedPreferencesから取得する

    var myUser = User()
        set(value) {
            field = value
            myUserId = value.userId
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

    fun getMyFriend (friendId: String): User?{
        return myFriends.filter { it.userId.equals(friendId) }.firstOrNull()
    }

    fun removeMyFriends (friendId: String) {
        myUser.friendIdList.remove(friendId)
        myFriends = allUsers.filter { myUser.friendIdList.contains(it.userId) }
    }

    /**
     * グループのメンバー画像取得処理で必要になった
     */
    fun getTargetUser (userId: String): User?{
        return allUsers.filter { it.userId.equals(userId) }.firstOrNull()
    }

    /**
     * UserManager初期化
     * FirebaseAuthのcurrentUserが取得出来る前提
     * @param onSuccess: () -> Unit
     */
    fun initUserManager(onSuccess: () -> Unit) {
        UserStore.getLoginUser {
            it.result?.toObjects(User::class.java)?.firstOrNull().also {
                it?.also {
                    // ↓なぜかmyUserIdの値が入らない潜在バグ
                    myUser = it
                    UserStore.getAllUsers {
                        it.result?.toObjects(User::class.java)?.also {
                            allUsers = it
                        }
                        onSuccess.invoke()
                    }
                }
            } ?: run {
                if(BuildConfig.DEBUG)
                    throw RuntimeException("initUserManager(): currentUserがnull")
            }

        }
    }

    fun clear() {
        myUserId = ""
        myUser = User()
        allUsers = listOf<User>()
        myFriends = listOf<User>()
    }
}