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


class FriendViewModel: ViewModel() {

    val items = MutableLiveData<List<Room>>()
    val isException = MutableLiveData<Throwable>()

    fun initData(@NonNull owner: LifecycleOwner) {
        update()
    }

    fun update() {
        RoomManager.initRoomManager {
            items.postValue(it)
        }
    }
}