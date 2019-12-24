package com.komugirice.icchat.viewModel

import androidx.annotation.NonNull
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.komugirice.icchat.firestore.manager.RoomManager
import com.komugirice.icchat.firestore.manager.UserManager
import com.komugirice.icchat.firestore.model.Message
import com.komugirice.icchat.firestore.model.Room
import com.komugirice.icchat.firestore.store.MessageStore
import com.komugirice.icchat.firestore.store.RoomStore

class RoomViewModel: ViewModel() {
    val items = MutableLiveData<List<Pair<Room, Message>>>()

    fun initData(@NonNull owner: LifecycleOwner) {
        update()
    }

    /**
     * Roomと最新のMessageを取得
     *
     */
    fun update() {
        var list = mutableListOf<Pair<Room, Message>>()
        RoomManager.myRooms.forEach { room ->
            // とりあえずroom to ダミー
            var pair = (room to Message())
            MessageStore.getLastMessage(room.documentId) {
                if (it.isSuccessful) {
                    val message = it.result?.toObjects(Message::class.java)?.firstOrNull()
                    message?.also {
                        // 取得したらmessageも
                        pair = pair.copy(second = it)
                    }
                }
                list.add(pair)

                if (RoomManager.myRooms.size == list.size) {
                    list.sortByDescending { it.second.createdAt }
                    items.postValue(list)
                }

            }
        }

    }

}