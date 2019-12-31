package com.komugirice.icchat.viewModel

import androidx.annotation.NonNull
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.enum.RequestStatus
import com.komugirice.icchat.firestore.manager.RequestManager
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
            val groupRequests = RequestManager.groupsRequestToMe
                .filter{it.requests.first().status == RequestStatus.REQUEST.id}
            // 招待中1件以上の場合、タイトル表示
            if(groupRequests.size > 0)
                // 招待グループタイトル
                list.add(FriendsView.FriendsViewData(Room(), FriendsView.VIEW_TYPE_TITLE_REQUEST_GROUP))

            // 招待グループアイテム
            groupRequests.forEach {
                list.add(FriendsView.FriendsViewData(it.room, FriendsView.VIEW_TYPE_ITEM_REQUEST_GROUP))
            }
            val groupDenys = RequestManager.groupsRequestToMe
                .filter{it.requests.first().status == RequestStatus.DENY.id}
            // 拒否1件以上の場合、タイトル表示
            if(groupDenys.size > 0)
                // 拒否グループタイトル
                list.add(FriendsView.FriendsViewData(Room(), FriendsView.VIEW_TYPE_TITLE_DENY_GROUP))

            // 拒否グループアイテム
            groupDenys.forEach {
                list.add(FriendsView.FriendsViewData(it.room, FriendsView.VIEW_TYPE_ITEM_DENY_GROUP))
            }

            items.postValue(list)
        }
    }

}