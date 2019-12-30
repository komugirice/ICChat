package com.komugirice.icchat.firestore.store

import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.firestore.manager.RequestManager
import com.komugirice.icchat.firestore.manager.RoomManager
import com.komugirice.icchat.firestore.manager.UserManager
import com.komugirice.icchat.firestore.model.Request
import com.komugirice.icchat.firestore.model.Room
import java.util.*

class RequestStore {
    companion object {
        fun getLoginUserRequest(onFailure: () -> Unit, onSuccess: (Request) -> Unit) {
            FirebaseFirestore.getInstance()
                .collection("request")
                .document(UserManager.myUserId)
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result?.toObject(Request::class.java)?.also {
                            onSuccess.invoke(it)
                        } ?: run {
                            onFailure.invoke()
                        }
                    } else {
                        onFailure.invoke()
                    }
                }
        }

        fun getLoginUserGroupsRequest(onSuccess: (List<Request>) -> Unit) {
            val roomsRequest = mutableListOf<Request>()
            var index = 0
            RoomManager.myRooms.forEach {
                index++
                FirebaseFirestore.getInstance()
                    .collection("request")
                    .document(it.documentId)
                    .get()
                    .addOnSuccessListener {
                        it.toObject(Request::class.java)?.also {
                            roomsRequest.add(it)
                        }
                        if(RoomManager.myRooms.size == index)
                            onSuccess.invoke(roomsRequest)
                    }
            }
        }

        fun requestFriend(userId: String, onSuccess: () -> Unit) {
            val request = RequestManager.myUserRequest
            request.requestIdList.add(userId)
            request.createdAt = Date()

            FirebaseFirestore.getInstance()
                .collection("request")
                .document(request.documentId)
                .set(request)
                // 失敗しない
                .addOnSuccessListener {
                    onSuccess.invoke()
                }
        }
    }
}