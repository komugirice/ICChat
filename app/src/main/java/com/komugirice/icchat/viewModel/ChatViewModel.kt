package com.komugirice.icchat.viewModel

import androidx.annotation.NonNull
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.data.firestore.Message
import com.komugirice.icchat.data.firestore.User
import com.komugirice.icchat.util.FireStoreUtil


class ChatViewModel: ViewModel() {

    val items = MutableLiveData<List<Message>>()
    val isException = MutableLiveData<Throwable>()


    fun initData() {

    }

    // TODO 監視
//    fun update(msgList: MutableLiveData<MutableList<List<Message>>>) {
//
//
//    }

}