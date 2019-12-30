package com.komugirice.icchat.viewModel

import androidx.annotation.NonNull
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.firestore.manager.RoomManager
import com.komugirice.icchat.firestore.manager.UserManager
import com.komugirice.icchat.firestore.model.Room
import com.komugirice.icchat.firestore.model.User
import com.komugirice.icchat.firestore.store.RoomStore
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

            // グループタイトル
            list.add(FriendsView.FriendsViewData(Room(), FriendsView.VIEW_TYPE_TITLE_GROUP))
            // グループアイテム
            RoomManager.myGroupRooms.forEach {
                list.add(FriendsView.FriendsViewData(it, FriendsView.VIEW_TYPE_ITEM_GROUP))
            }
            // 友だちタイトル
            list.add(FriendsView.FriendsViewData(Room(), FriendsView.VIEW_TYPE_TITLE_FRIEND))
            // 友だちアイテム
            RoomManager.mySingleRooms.forEach {
                list.add(FriendsView.FriendsViewData(it, FriendsView.VIEW_TYPE_ITEM_FRIEND))
            }
            val invites = RoomManager.myInviteRooms
            // 招待中1件以上の場合、タイトル表示
            if(invites.size > 0)
                // 招待グループタイトル
                list.add(FriendsView.FriendsViewData(Room(), FriendsView.VIEW_TYPE_TITLE_INVITE))

            // 招待グループアイテム
            invites.forEach {
                list.add(FriendsView.FriendsViewData(it, FriendsView.VIEW_TYPE_ITEM_INVITE))
            }
            val denys = RoomManager.myDenyRooms
            // 拒否1件以上の場合、タイトル表示
            if(denys.size > 0)
                // 拒否グループタイトル
                list.add(FriendsView.FriendsViewData(Room(), FriendsView.VIEW_TYPE_TITLE_DENY))

            // 拒否グループアイテム
            denys.forEach {
                list.add(FriendsView.FriendsViewData(it, FriendsView.VIEW_TYPE_ITEM_DENY))
            }

            items.postValue(list)
        }
    }

}