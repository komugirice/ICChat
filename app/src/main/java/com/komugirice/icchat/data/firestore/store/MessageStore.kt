package com.komugirice.icchat.data.firestore.store

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.data.firestore.Message
import com.komugirice.icchat.data.firestore.manager.UserManager
import com.komugirice.icchat.util.FireStoreUtil
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
                .get()
                .addOnSuccessListener {
                    val messages = it.toObjects(Message::class.java)
                    //val messages: List<Message> = it.data.map{ it.value }.toList()
                    liveMsgList.postValue(messages)
                }
        }
    }
}