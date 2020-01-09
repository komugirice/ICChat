package com.komugirice.icchat.viewModel

import androidx.annotation.NonNull
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.komugirice.icchat.enum.RequestStatus
import com.komugirice.icchat.firebase.firestore.manager.RequestManager
import com.komugirice.icchat.firebase.firestore.model.Request


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