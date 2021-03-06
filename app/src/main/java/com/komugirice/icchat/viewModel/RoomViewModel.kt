package com.komugirice.icchat.viewModel

import androidx.annotation.NonNull
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.komugirice.icchat.firebase.firestore.manager.RoomManager
import com.komugirice.icchat.firebase.firestore.model.Message
import com.komugirice.icchat.firebase.firestore.model.Room
import com.komugirice.icchat.firebase.firestore.store.MessageStore

class RoomViewModel: ViewModel() {
    val items = MutableLiveData<List<Pair<Room, Message?>>>()

    fun initData(@NonNull owner: LifecycleOwner) {
        update()
    }

    /**
     * Roomと最新のMessageを取得
     *
     */
    fun update() {
        var list = mutableListOf<Pair<Room, Message?>>()
        if(RoomManager.myRooms.isEmpty()) {
            items.postValue(list)
            return
        }
        RoomManager.myRooms.forEach { room ->
            // とりあえずroom設定
            var pair : Pair<Room, Message?> = (room to null)
            MessageStore.getLastMessage(room.documentId) {
                if (it.isSuccessful) {
                    val message = it.result?.toObjects(Message::class.java)?.firstOrNull()
                    // 取得したらmessageも
                    pair = pair.copy(second = message)
                }
                list.add(pair)

                if (RoomManager.myRooms.size == list.size) {
                    list.sortByDescending { it.second?.createdAt }
                    items.postValue(list)
                }
            }
        }

    }

}