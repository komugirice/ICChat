package com.komugirice.icchat.firebase.firestore.store

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.komugirice.icchat.enums.MessageType
import com.komugirice.icchat.firebase.firestore.model.Message
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import java.util.*

class MessageStore {
    companion object {
        const val ROOMS = "rooms"
        const val MESSAGES = "messages"
        fun registerMyMessage(roomId: String, message: String) {
            val msgObj = Message().apply {
                this.documentId = UUID.randomUUID().toString()
                this.roomId = roomId
                this.userId = UserManager.myUserId
                this.message = message
                this.type = MessageType.TEXT.id
            }

            FirebaseFirestore.getInstance()
                .collection("$ROOMS/$roomId/$MESSAGES")
                .document(msgObj.documentId)
                .set(msgObj)
        }

        fun registerMessage(roomId: String, userId: String, message: String, type: Int
                            , onSuccess: () -> Unit) {
            val msgObj = Message().apply {
                this.documentId = UUID.randomUUID().toString()
                this.roomId = roomId
                this.userId = userId
                this.message = message
                this.type = type
            }

            FirebaseFirestore.getInstance()
                .collection("$ROOMS/$roomId/$MESSAGES")
                .document(msgObj.documentId)
                .set(msgObj)
                .addOnSuccessListener {
                    onSuccess.invoke()
                }
        }

        fun getMessages(roomId: String, onSuccess: (List<Message>) -> Unit) {

            FirebaseFirestore.getInstance()
                .collection("$ROOMS/$roomId/$MESSAGES")
                .orderBy(Message::createdAt.name, Query.Direction.ASCENDING)
                .get()
                // 必ず成功する。messagesが作られて無くても成功する。
                .addOnSuccessListener {
                    val messages = it.toObjects(Message::class.java)
                    onSuccess.invoke(messages)
                }
        }

        fun getLastMessage(roomId: String, onComplete: (Task<QuerySnapshot>) -> Unit) {
            FirebaseFirestore.getInstance()
                .collection("$ROOMS/$roomId/$MESSAGES")
                .orderBy(Message::createdAt.name, Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener {
                    onComplete.invoke(it)
                }
        }

        fun deleteMessage(message: Message, onComplete: () -> Unit) {
            FirebaseFirestore.getInstance()
                .collection("$ROOMS/${message.roomId}/$MESSAGES")
                .document(message.documentId)
                .delete()
                .addOnCompleteListener {
                    onComplete.invoke()
                }
        }
    }
}