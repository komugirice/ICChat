package com.komugirice.icchat.firebase.firestore.store

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.komugirice.icchat.enum.MessageType
import com.komugirice.icchat.firebase.firestore.model.Message
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import java.util.*

class MessageStore {
    companion object {
        fun registerMessage(roomId: String, message: String) {
            val msgObj = Message().apply {
                this.documentId = UUID.randomUUID().toString()
                this.roomId = roomId
                this.userId = UserManager.myUserId
                this.message = message
                this.type = MessageType.TEXT.id
            }

            FirebaseFirestore.getInstance()
                .collection("rooms/$roomId/messages")
                .document(msgObj.documentId)
                .set(msgObj)
        }

        fun getMessages(roomId: String, liveMsgList: MutableLiveData<List<Message>>) {

            FirebaseFirestore.getInstance()
                .collection("rooms/$roomId/messages")
                .orderBy(Message::createdAt.name, Query.Direction.ASCENDING)
                .get()
                // 必ず成功する。messagesが作られて無くても成功する。
                .addOnSuccessListener {
                    val messages = it.toObjects(Message::class.java)
                    //val messages: List<Message> = it.data.map{ it.value }.toList()
                    liveMsgList.postValue(messages)
                }
        }

        fun getLastMessage(roomId: String, onComplete: (Task<QuerySnapshot>) -> Unit) {
            FirebaseFirestore.getInstance()
                .collection("rooms/$roomId/messages")
                .orderBy(Message::createdAt.name, Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener {
                    onComplete.invoke(it)
                }
        }
    }
}