package com.komugirice.icchat.viewModel

import android.content.Intent
import androidx.annotation.NonNull
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.komugirice.icchat.ChatActivity
import com.komugirice.icchat.enums.MessageType
import com.komugirice.icchat.firebase.firestore.manager.RoomManager
import com.komugirice.icchat.firebase.firestore.model.File
import com.komugirice.icchat.firebase.firestore.model.Message
import com.komugirice.icchat.firebase.firestore.model.Room
import com.komugirice.icchat.firebase.firestore.store.FileStore
import com.komugirice.icchat.firebase.firestore.store.MessageStore
import timber.log.Timber
import java.util.*


class ChatViewModel: ViewModel() {

    val items = MutableLiveData<List<Pair<Message, File?>>>()
    val isException = MutableLiveData<Throwable>()
    private var messageListener: ListenerRegistration? = null
    // groupのusers不要論
    //val users = MutableLiveData<List<User>>()
    val room = MutableLiveData<Room>()


    fun initData(@NonNull owner: LifecycleOwner, roomId: String) {
        // ユーザ情報保持
        //RoomStore.getTargetRoomUsers(roomId){
            //users.postValue(it)

        var list = mutableListOf<Pair<Message, File?>>()

            // message情報
            MessageStore.getMessages(roomId){getMessages->
                getMessages.forEach {

                    FileStore.getFile(it.roomId, it.message, it.type){file->
                        list.add(Pair(it, file))

                        if(getMessages.size == list.size) {
                            // 表示順序がおかしいバグ
                            list.sortBy { it.first.createdAt }
                            items.postValue(list)
                            // 監視
                            val lastCreatedAt = getMessages.map{ it.createdAt }.max() ?: Date()
                            initSubscribe(roomId, lastCreatedAt)
                        }

                    }
                }


            }
        //}
    }

    /**
     * intent用（他画面遷移）
     */
    fun initRoom(intent: Intent): Boolean {
        intent.getSerializableExtra(ChatActivity.KEY_ROOM).also {
            if (it is Room && it.documentId.isNotEmpty()) {
                this.room.postValue(it)
                return true
            }
            return false
        }

    }

    /**
     * intentを使用しない場合（次画面のリロード）
     */
    fun initRoom(room: Room): Boolean {
        RoomManager.getTargetRoom(room.documentId)?.also {
            this.room.postValue(it)
            return true
        }
        return false
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
                    isException.postValue(firebaseFirestoreException)
                    return@addSnapshotListener
                }
                snapshot?.toObjects(Message::class.java)?.firstOrNull()?.also {

                    FileStore.getFile(it.roomId, it.message, it.type){file->
                        val tmp: MutableList<Pair<Message, File?>>? = items.value?.toMutableList()
                        tmp?.add(Pair(it, file))
                        items.postValue(tmp)
                    }

                }
            }
    }

}