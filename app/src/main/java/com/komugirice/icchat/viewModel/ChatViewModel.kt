package com.komugirice.icchat.viewModel

import android.content.Intent
import androidx.annotation.NonNull
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.komugirice.icchat.ChatActivity
import com.komugirice.icchat.enums.MessageType
import com.komugirice.icchat.extension.getDateToString
import com.komugirice.icchat.extension.toDate
import com.komugirice.icchat.firebase.firestore.manager.RoomManager
import com.komugirice.icchat.firebase.firestore.model.FileInfo
import com.komugirice.icchat.firebase.firestore.model.Message
import com.komugirice.icchat.firebase.firestore.model.Room
import com.komugirice.icchat.firebase.firestore.store.FileInfoStore
import com.komugirice.icchat.firebase.firestore.store.MessageStore
import com.komugirice.icchat.view.InterestView
import timber.log.Timber
import java.util.*


class ChatViewModel: ViewModel() {

    val items = MutableLiveData<List<Pair<Message, FileInfo?>>>()
    val isException = MutableLiveData<Throwable>()
    private var messageListener: ListenerRegistration? = null
    // groupのusers不要論
    //val users = MutableLiveData<List<User>>()
    val room = MutableLiveData<Room>()
    var isNonMove = false


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

    fun initData(@NonNull owner: LifecycleOwner, roomId: String, isNonMove: Boolean = false) {
        this.isNonMove = isNonMove
        // ユーザ情報保持
        //RoomStore.getTargetRoomUsers(roomId){
            //users.postValue(it)

        var list = mutableListOf<Pair<Message, FileInfo?>>()

            // message情報
            MessageStore.getMessages(roomId){getMessages->

                getMessages.forEach {

                    FileInfoStore.getFile(it.roomId, it.message, it.type){ file->
                        list.add(Pair(it, file))

                        if(getMessages.size == list.size) {
                            // 表示順序がおかしいバグ対応
                            list.sortBy { it.first.createdAt }
                            // 日付タイプ追加
                            val dateList = getMessageListWithDateType(list)
                            items.postValue(dateList)
                            // 監視
                            val lastCreatedAt = getMessages.map{ it.createdAt }.max() ?: Date()
                            initSubscribe(roomId, lastCreatedAt)
                        }

                    }
                }
                // message0件の不具合対応
                if(getMessages.isEmpty()) {
                    // 監視
                    this.isNonMove = false
                    val lastCreatedAt = Date()
                    initSubscribe(roomId, lastCreatedAt)
                }

            }
        //}
    }

    /**
     * 日付タイプ追加
     *
     */
    private fun getMessageListWithDateType(list: MutableList<Pair<Message, FileInfo?>>)
            : MutableList<Pair<Message, FileInfo?>> {
        var retList = mutableListOf<Pair<Message, FileInfo?>>()

        var targetDate: Date? = "9999/12/31".toDate()    // ダミー値設定

        // interestViewDataにinterestsを格納していく
        // その間に日付をチェックしながら、違いがあった場合、日付データを追加する。
        list.forEach {

            // 先に日付データを格納
            val thisDate = it.first.createdAt
            // compareTo : (thisTime<anotherTime ? -1 : (thisTime==anotherTime ? 0 : 1));
            if(thisDate != null && thisDate.getDateToString() !=  targetDate?.getDateToString()) {
                targetDate = thisDate // targetDate更新
                // 違いがあった場合、日付データを追加
                retList.add(
                    Pair(Message().apply{
                        createdAt = thisDate
                        type = MessageType.DATE.id
                    }, null)
                )
            }
            // 次にinterestsを格納
            retList.add(it)
        }
        return retList
    }

    /**
     * 監視メソッド
     *
     */
    private fun initSubscribe(roomId: String, lastCreatedAt: Date) {
        messageListener = FirebaseFirestore
            .getInstance()
            .collection("rooms/$roomId/messages")
            .orderBy(Message::createdAt.name, Query.Direction.DESCENDING)
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

                    val tmp: MutableList<Pair<Message, FileInfo?>>? = items.value?.toMutableList()

                    // 日付線追加
                    if(it.createdAt.getDateToString() != lastCreatedAt.getDateToString()) {
                        tmp?.add(
                            Pair(Message().apply{
                                createdAt = it.createdAt
                                type = MessageType.DATE.id
                            }, null)
                        )
                    }
                    // ファイルデータ取得＆データ追加
                    FileInfoStore.getFile(it.roomId, it.message, it.type){ file->
                        tmp?.add(Pair(it, file))
                        items.postValue(tmp)
                    }

                }
            }
    }

}