package com.komugirice.icchat.viewModel

import androidx.annotation.NonNull
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.firestore.manager.RoomManager
import com.komugirice.icchat.firestore.model.Room
import com.komugirice.icchat.firestore.model.User
import com.komugirice.icchat.util.FireStoreUtil
import com.komugirice.icchat.view.FriendsView


class FriendViewModel: ViewModel() {

    val items = MutableLiveData<List<FriendsView.FriendsViewData>>()
    val isException = MutableLiveData<Throwable>()

    fun initData(@NonNull owner: LifecycleOwner) {
        update()
    }

    fun update() {
        RoomManager.initRoomManager {
            val list = mutableListOf<FriendsView.FriendsViewData>()

            list.add(FriendsView.FriendsViewData(Room(), FriendsView.VIEW_TYPE_TITLE_GROUP))

            val groups = it.filter {it.isGroup == true}
            groups.forEach {
                list.add(FriendsView.FriendsViewData(it, FriendsView.VIEW_TYPE_ITEM))
            }

            list.add(FriendsView.FriendsViewData(Room(), FriendsView.VIEW_TYPE_TITLE_FRIEND))

            val friends = it.filter {it.isGroup == false}
            friends.forEach {
                list.add(FriendsView.FriendsViewData(it, FriendsView.VIEW_TYPE_ITEM))
            }

            items.postValue(list)
        }
    }

    companion object {
        private const val GROUP_FLAG_OFF = 0
        private const val GROUP_FLAG_MEMBER = 1
        private const val GROUP_FLAG_OWNER = 2
    }
}