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

    fun getMyFriend (friendId: String): User?{
        return myFriends.filter { it.userId.equals(friendId) }.firstOrNull()
    }

    fun removeMyFriends (friendId: String) {
        myUser.friendIdList.remove(friendId)
        myFriends = allUsers.filter { myUser.friendIdList.contains(it.userId) }
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
                    myUserId = it.userId
                    myUser = it
                    // TODO 非同期大丈夫？
                    UserStore.getAllUsers(){
                        it.result?.toObjects(User::class.java)?.also {
                            UserManager.allUsers = it
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
}