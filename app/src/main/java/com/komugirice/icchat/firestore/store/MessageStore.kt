package com.komugirice.icchat.firestore.store

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.komugirice.icchat.firestore.model.Message
import com.komugirice.icchat.firestore.manager.UserManager
import java.util.*

class MessageStore {
    companion object {
        fun registerMessage(roomId: String, message: String) {
            val msgObj = Message().apply {
                this.documentId = UUID.randomUUID().toString()
                this.roomId = roomId
                this.userId = UserManager.myUserId
                this.message = message
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
                .addOnSuccessListener {
                    val messages = it.toObjects(Message::class.java)
                    //val messages: List<Message> = it.data.map{ it.value }.toList()
                    liveMsgList.postValue(messages)
                }
        }
    }
}