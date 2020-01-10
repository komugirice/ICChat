package com.komugirice.icchat.viewModel

import androidx.annotation.NonNull
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.komugirice.icchat.enum.RequestStatus
import com.komugirice.icchat.firebase.firebaseFacade
import com.komugirice.icchat.firebase.firestore.manager.RequestManager
import com.komugirice.icchat.firebase.firestore.manager.RoomManager
import com.komugirice.icchat.view.FriendsView


class FriendViewModel: ViewModel() {

    val items = MutableLiveData<List<FriendsView.FriendsViewData>>()
    val isException = MutableLiveData<Throwable>()

    fun initData(@NonNull owner: LifecycleOwner) {
        update()
    }

    fun update() {
        firebaseFacade.initManager {
            val list = mutableListOf<FriendsView.FriendsViewData>()

            // ①グループ
            // グループタイトル
            list.add(FriendsView.FriendsViewData(FriendsView.VIEW_TYPE_TITLE_GROUP))
            // グループアイテム
            RoomManager.myGroupRooms.forEach {
                list.add(FriendsView.FriendsViewData(it, FriendsView.VIEW_TYPE_ITEM_GROUP))
            }

            // ②友だち
            // 友だちタイトル
            list.add(FriendsView.FriendsViewData(FriendsView.VIEW_TYPE_TITLE_FRIEND))
            // 友だちアイテム
            RoomManager.mySingleRooms.forEach {
                list.add(FriendsView.FriendsViewData(it, FriendsView.VIEW_TYPE_ITEM_FRIEND))
            }

            // ③招待されている友だち
            val friendRequests = RequestManager.usersRequestToMe
                .filter{it.status == RequestStatus.REQUEST.id}
            // 招待中1件以上の場合、タイトル表示
            if(friendRequests.size > 0)
            // 招待されている友だちタイトル
                list.add(FriendsView.FriendsViewData(FriendsView.VIEW_TYPE_TITLE_REQUEST_FRIEND))
            // 招待されている友だちアイテム
            friendRequests.forEach {req ->
                list.add(FriendsView.FriendsViewData(req, FriendsView.VIEW_TYPE_ITEM_REQUEST_FRIEND))
            }

            // ④友だち拒否したユーザ
            val friendDenys = RequestManager.usersRequestToMe
                .filter{it.status == RequestStatus.DENY.id}
            // 拒否ユーザ1件以上の場合、タイトル表示
            if(friendDenys.size > 0)
            // 拒否ユーザタイトル
                list.add(FriendsView.FriendsViewData(FriendsView.VIEW_TYPE_TITLE_DENY_FRIEND))

            // ⑥拒否グループアイテム
            friendDenys.forEach {
                list.add(FriendsView.FriendsViewData(it, FriendsView.VIEW_TYPE_ITEM_DENY_FRIEND))
            }

            // ⑤招待されているグループ
            val groupRequests = RequestManager.groupsRequestToMe
                .filter{it.requests.first().status == RequestStatus.REQUEST.id}
            // 招待中1件以上の場合、タイトル表示
            if(groupRequests.size > 0)
                // 招待グループタイトル
                list.add(FriendsView.FriendsViewData(FriendsView.VIEW_TYPE_TITLE_REQUEST_GROUP))
            // 招待グループアイテム
            groupRequests.forEach {
                list.add(FriendsView.FriendsViewData(it.room, FriendsView.VIEW_TYPE_ITEM_REQUEST_GROUP))
            }

            // ⑥拒否したグループ
            val groupDenys = RequestManager.groupsRequestToMe
                .filter{it.requests.first().status == RequestStatus.DENY.id}
            // 拒否1件以上の場合、タイトル表示
            if(groupDenys.size > 0)
                // 拒否グループタイトル
                list.add(FriendsView.FriendsViewData(FriendsView.VIEW_TYPE_TITLE_DENY_GROUP))

            // ⑥拒否グループアイテム
            groupDenys.forEach {
                list.add(FriendsView.FriendsViewData(it.room, FriendsView.VIEW_TYPE_ITEM_DENY_GROUP))
            }

            items.postValue(list)
        }
    }

}