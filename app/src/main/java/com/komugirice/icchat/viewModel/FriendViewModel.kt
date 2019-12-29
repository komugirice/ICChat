package com.komugirice.icchat.viewModel

import androidx.annotation.NonNull
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.firestore.model.User
import com.komugirice.icchat.util.FireStoreUtil


class FriendViewModel: ViewModel() {

    val items = MutableLiveData<List<User>>()
    val isException = MutableLiveData<Throwable>()


//    fun initData(friendList: MutableLiveData<MutableList<String>>) {
//        updateFriends(friendList)
//    }
    fun initData(@NonNull owner: LifecycleOwner) {
        val friendList = MutableLiveData<MutableList<String>>()
            FireStoreUtil.getFriends(friendList)
            friendList.observe(owner, androidx.lifecycle.Observer {
                updateFriends(friendList)
            })
    }

    fun updateFriends(friendList: MutableLiveData<MutableList<String>>) {
            var userList: MutableList<User> = mutableListOf()

            // ユーザ情報取得
            friendList.value?.also {
                it.forEach {
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .whereEqualTo("userId", it)
                        .get()
                        .addOnCompleteListener {
                            // IOスレッドなので、レイアウト関係を扱えないことを注意
                            if (!it.isSuccessful)
                                return@addOnCompleteListener
                            // TODO 取得は複数件しか扱えないのか
                            it.result?.toObjects(User::class.java)?.also { users ->
                                if(users.isNotEmpty()) {
                                    userList.add(users[0])
                                    items.postValue(userList)
                                }
                            }
                        }
                }
                // 0件の場合もpostValueするように
                if(it.isEmpty())
                    items.postValue(userList)


            }
    }
}