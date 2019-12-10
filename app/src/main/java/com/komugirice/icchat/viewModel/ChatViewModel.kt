package com.komugirice.icchat.viewModel

import androidx.annotation.NonNull
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.komugirice.icchat.data.firestore.Message
import com.komugirice.icchat.data.firestore.Room
import com.komugirice.icchat.data.firestore.store.MessageStore
import timber.log.Timber
import java.util.*


class ChatViewModel: ViewModel() {

    val items = MutableLiveData<List<Message>>()
    val isException = MutableLiveData<Throwable>()
    private var messageListener: ListenerRegistration? = null


    fun initData(@NonNull owner: LifecycleOwner, roomId: String) {
        MessageStore.getMessages(roomId, items)
        items.observe(owner, Observer {
            val lastCreatedAt = it.map{ it.createdAt }.max() ?: Date()
            initSubscribe(roomId, lastCreatedAt)
        })
    }

    /**
     * 監視メソッド
     *
     */
    private fun initSubscribe(roomId: String, lastCreatedAt: Date) {
        messageListener = FirebaseFirestore
            .getInstance()
            .collection("rooms/$roomId/messages")
            .orderBy(Room::createdAt.name, Query.Direction.DESCENDING)
            .whereGreaterThan(Message::createdAt.name, lastCreatedAt)
            .limit(1L)
            .addSnapshotListener { snapshot, firebaseFirestoreException ->
                Timber.d("initSubscribe snapshot:$snapshot firebaseFirestoreException:$firebaseFirestoreException")
                if (firebaseFirestoreException != null) {
                    firebaseFirestoreException.printStackTrace()
                    return@addSnapshotListener
                }
                snapshot?.toObjects(Message::class.java)?.firstOrNull()?.also {
                    val tmp: MutableList<Message>? = items.value?.toMutableList()
                    tmp?.add(it)
                    items.postValue(tmp)
                }
            }
    }

}