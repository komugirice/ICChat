package com.komugirice.icchat.data.firestore.manager

import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.data.firestore.Message
import com.komugirice.icchat.data.firestore.Room
import com.komugirice.icchat.data.firestore.User
import com.komugirice.icchat.util.FireStoreUtil
import java.util.*

class MessageManager {
    companion object {
        fun registerMessage(roomId: String, message: String) {
            val msgObj = Message().apply {
                this.documentId = UUID.randomUUID().toString()
                this.roomId = roomId
                this.userId = FireStoreUtil.getLoginUserId()
                this.message = message
            }

            FirebaseFirestore.getInstance()
                .collection("messages/$roomId")
                .document(msgObj.documentId)
                .set(msgObj)
        }
    }
}