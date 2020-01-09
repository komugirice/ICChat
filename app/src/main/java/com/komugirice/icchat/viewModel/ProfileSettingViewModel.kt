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
import com.komugirice.icchat.firestore.model.Request
import com.komugirice.icchat.firestore.model.Room
import com.komugirice.icchat.firestore.model.User
import com.komugirice.icchat.firestore.store.RoomStore
import com.komugirice.icchat.util.FireStoreUtil
import com.komugirice.icchat.view.FriendsView


class ProfileSettingViewModel: ViewModel() {

    val items_request = MutableLiveData<List<Request>>()

    fun initData(@NonNull owner: LifecycleOwner) {
        update()
    }

    fun update() {
        RequestManager.initMyUserRequests {
            val list = mutableListOf<Request>()
            RequestManager.myUserRequests.filter{it.status == RequestStatus.REQUEST.id}.forEach { list.add(it) }
            items_request.postValue(list)
        }
    }
}