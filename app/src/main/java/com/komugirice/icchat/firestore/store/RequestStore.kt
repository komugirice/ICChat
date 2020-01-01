package com.komugirice.icchat.firestore.store

import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.enum.RequestStatus
import com.komugirice.icchat.firestore.manager.RequestManager
import com.komugirice.icchat.firestore.manager.RoomManager
import com.komugirice.icchat.firestore.manager.UserManager
import com.komugirice.icchat.firestore.model.GroupRequests
import com.komugirice.icchat.firestore.model.Request
import com.komugirice.icchat.firestore.model.Room
import java.util.*

class RequestStore {
    companion object {
        fun getLoginUserRequests(onFailure: () -> Unit, onSuccess: (List<Request>) -> Unit) {
            FirebaseFirestore.getInstance()
                .collection("users/${UserManager.myUserId}/requests")
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result?.toObjects(Request::class.java)?.also {
                            onSuccess.invoke(it)
                        } ?: run {
                            onFailure.invoke()
                        }
                    } else {
                        onFailure.invoke()
                    }
                }
        }

        fun getLoginUserGroupsRequests(onSuccess: (List<GroupRequests>) -> Unit) {
            val groupsRequests = mutableListOf<GroupRequests>()
            var index = 0
            if(RoomManager.myRooms.isEmpty()) {
                onSuccess.invoke(groupsRequests)
                return
            }
            RoomManager.myRooms.forEach { room ->
                FirebaseFirestore.getInstance()
                    .collection("rooms/${room.documentId}/requests")
                    .get()
                    .addOnCompleteListener {
                        index++
                        if(it.isSuccessful) {
                            it.result?.toObjects(Request::class.java)?.also {
                                groupsRequests.add(GroupRequests().apply {
                                    this.room = room
                                    this.requests = it
                                })
                            }
                        }
                        if(RoomManager.myRooms.size == index)
                            onSuccess.invoke(groupsRequests)
                    }
            }
        }

        fun getUsersRequestToMe(onSuccess: (List<Request>) -> Unit) {
            var usersRequestToMe = mutableListOf<Request>()
            var index = 0
            UserManager.allUsers.forEach {
                FirebaseFirestore.getInstance()
                    .collection("users/${it.userId}/requests")
                    .document(UserManager.myUserId)
                    .get()
                    .addOnCompleteListener {
                        index++
                        if(it.isSuccessful) {
                            it.result?.toObject(Request::class.java)?.also {
                                usersRequestToMe.add(it)
                            }
                        }
                        if(UserManager.allUsers.size == index)
                            onSuccess.invoke(usersRequestToMe)
                    }
            }

        }

        fun getGroupsRequestToMe(onSuccess: (List<GroupRequests>) -> Unit) {
            val groupsRequestToMe = mutableListOf<GroupRequests>()
            var index = 0
            RoomStore.getAllGroupRooms(){
                val allGroup = it
                allGroup.forEach {room ->
                    FirebaseFirestore.getInstance()
                        .collection("rooms/${room.documentId}/requests")
                        .document(UserManager.myUserId)
                        .get()
                        .addOnCompleteListener {
                            index++
                            if(it.isSuccessful) {
                                it.result?.toObject(Request::class.java)?.also {
                                    groupsRequestToMe.add(GroupRequests().apply {
                                        this.room = room
                                        this.requests.add(it)
                                    })
                                }
                            }
                            if (allGroup.size == index)
                                onSuccess.invoke(groupsRequestToMe)
                        }
                }
            }
        }

        fun requestFriend(userId: String, onSuccess: () -> Unit) {
            val request = Request().apply {
                documentId = userId
                requestId = UserManager.myUserId
                beRequestedId = userId
                status = RequestStatus.REQUEST.id
                createdAt = Date()
            }

            FirebaseFirestore.getInstance()
                .collection("users/${UserManager.myUserId}/requests")
                .document(request.documentId)
                .set(request)
                // 失敗しない
                .addOnSuccessListener {
                    onSuccess.invoke()
                }
        }

        fun registerGroupRequest(gRequest: GroupRequests?, onSuccess: () -> Unit){
            if(gRequest == null) {
                onSuccess.invoke()
                return
            }
            var index = 0
            gRequest.requests.forEach {
                FirebaseFirestore.getInstance()
                    .collection("rooms/${gRequest.room.documentId}/requests")
                    .document(it.documentId)
                    .set(it)
                    // 失敗しない
                    .addOnCompleteListener {
                        index++
                        if (gRequest.requests.size == index)
                            onSuccess.invoke()
                    }
            }
        }

        fun deleteGroupRequest(roomId: String, requests: List<String>, onSuccess: () -> Unit){
            if(requests.isEmpty()) {
                onSuccess.invoke()
                return
            }
            var index = 0
            requests.forEach {
                FirebaseFirestore.getInstance()
                    .collection("rooms/${roomId}/requests")
                    .document(it)
                    .delete()
                    .addOnCompleteListener {
                        index++
                        if (requests.size == index)
                            onSuccess.invoke()
                    }
            }
        }


        fun denyGroupRequest(roomId: String, userId: String, onComplete: () -> Unit) {
            FirebaseFirestore.getInstance()
                .collection("rooms/${roomId}/requests")
                .document(userId)
                .update("status", RequestStatus.DENY.id)
                // データがなければ失敗する可能性もある
                .addOnCompleteListener {
                    onComplete.invoke()
                }
        }

        fun cancelDenyGroupRequest(roomId: String, userId: String, onComplete: () -> Unit) {
            FirebaseFirestore.getInstance()
                .collection("rooms/${roomId}/requests")
                .document(userId)
                .delete()
                // データがなければ失敗する可能性もある
                .addOnCompleteListener {
                    onComplete.invoke()
                }
        }

        fun acceptGroupRequest(roomId: String, userId: String, onComplete: () -> Unit) {
            FirebaseFirestore.getInstance()
                .collection("rooms/${roomId}/requests")
                .document(userId)
                .update("status", RequestStatus.ACCEPT.id)
                // データがなければ失敗する可能性もある
                .addOnCompleteListener {
                    onComplete.invoke()
                }
        }
    }
}