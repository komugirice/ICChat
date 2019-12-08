package com.komugirice.icchat.viewModel

import androidx.annotation.NonNull
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.komugirice.icchat.data.firestore.Message
import com.komugirice.icchat.data.firestore.store.MessageStore


class ChatViewModel: ViewModel() {

    val items = MutableLiveData<List<Message>>()
    val isException = MutableLiveData<Throwable>()


    fun initData(@NonNull owner: LifecycleOwner, roomId: String) {
        MessageStore.getMessages(roomId, items)
    }

//    fun update(roomId: String, msgList: MutableLiveData<MutableList<Message>>) {
//
//    }

    // TODO 監視


}