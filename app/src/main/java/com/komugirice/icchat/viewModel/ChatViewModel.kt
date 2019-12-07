package com.komugirice.icchat.viewModel

import androidx.annotation.NonNull
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.data.firestore.Message
import com.komugirice.icchat.data.firestore.User
import com.komugirice.icchat.data.firestore.manager.MessageManager
import com.komugirice.icchat.util.FireStoreUtil


class ChatViewModel: ViewModel() {

    val items = MutableLiveData<List<Message>>()
    val isException = MutableLiveData<Throwable>()


    fun initData(@NonNull owner: LifecycleOwner, roomId: String) {
        MessageManager.getMessages(roomId, items)
    }

//    fun update(roomId: String, msgList: MutableLiveData<MutableList<Message>>) {
//
//    }

    // TODO 監視


}