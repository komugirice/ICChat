package com.komugirice.icchat.firebase.firestore.store

import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.enums.RequestStatus
import com.komugirice.icchat.firebase.firestore.manager.RoomManager
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import com.komugirice.icchat.firebase.firestore.model.GroupRequests
import com.komugirice.icchat.firebase.firestore.model.Request
import java.util.*

class RequestStore {
    companion object {
        const val ROOMS = "rooms"
        const val REQUESTS = "requests"
        const val USERS = "users"

        fun getLoginUserRequests(onFailure: () -> Unit, onSuccess: (List<Request>) -> Unit) {
            FirebaseFirestore.getInstance()
                .collection("$USERS/${UserManager.myUserId}/$REQUESTS")
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
                    .collection("$ROOMS/${room.documentId}/$REQUESTS")
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
            // エラーでる。Document references must have an even number of segments, but users/00000037-cedc-45ff-8b6f-1589c393c3ef/requests has 3
            var usersRequestToMe = mutableListOf<Request>()
            var index = 0
            UserManager.allUsers.forEach {
                FirebaseFirestore.getInstance()
                    .collection(USERS)
                    .document(it.userId)
                    .collection(REQUESTS)
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
                        // エラーでる。java.lang.IllegalArgumentException: Invalid document reference. Document references must have an even number of segments, but rooms/3ded0e18-2c76-4438-8dec-ca5720bdbf29/requests has 3
                        //.collection("$ROOMS/${room.documentId}/$REQUESTS")
                        .collection(ROOMS)
                        .document(room.documentId)
                        .collection(REQUESTS)
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
                requesterId = UserManager.myUserId
                beRequestedId = userId
                status = RequestStatus.REQUEST.id
                createdAt = Date()
            }

            FirebaseFirestore.getInstance()
                .collection("$USERS/${UserManager.myUserId}/$REQUESTS")
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
                    .collection("$ROOMS/${gRequest.room.documentId}/$REQUESTS")
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
                    .collection("$ROOMS/${roomId}/$REQUESTS")
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
                .collection("$ROOMS/${roomId}/$REQUESTS")
                .document(userId)
                .update("status", RequestStatus.DENY.id)
                // データがなければ失敗する可能性もある
                .addOnCompleteListener {
                    onComplete.invoke()
                }
        }

        fun cancelDenyGroupRequest(roomId: String, userId: String, onComplete: () -> Unit) {
            FirebaseFirestore.getInstance()
                .collection("$ROOMS/${roomId}/$REQUESTS")
                .document(userId)
                .delete()
                // データがなければ失敗する可能性もある
                .addOnCompleteListener {
                    onComplete.invoke()
                }
        }

        fun acceptGroupRequest(roomId: String, userId: String, onComplete: () -> Unit) {
            FirebaseFirestore.getInstance()
                .collection("$ROOMS/${roomId}/$REQUESTS")
                .document(userId)
                .update("status", RequestStatus.ACCEPT.id)
                // データがなければ失敗する可能性もある
                .addOnCompleteListener {
                    onComplete.invoke()
                }
        }

        fun acceptUserRequest(requesterId: String, onComplete: () -> Unit) {
            FirebaseFirestore.getInstance()
                .collection("$USERS/${requesterId}/$REQUESTS")
                .document(UserManager.myUserId)
                .update("status", RequestStatus.ACCEPT.id)
                // データがなければ失敗する可能性もある
                .addOnCompleteListener {
                    onComplete.invoke()
                }
        }

        fun denyUserRequest(requesterId: String, onComplete: () -> Unit) {
            FirebaseFirestore.getInstance()
                .collection("users/${requesterId}/$REQUESTS")
                .document(UserManager.myUserId)
                .update("status", RequestStatus.DENY.id)
                // データがなければ失敗する可能性もある
                .addOnCompleteListener {
                    onComplete.invoke()
                }
        }


        fun cancelDenyUserRequest(requesterId: String, onComplete: () -> Unit) {
            FirebaseFirestore.getInstance()
                .collection("$USERS/${requesterId}/$REQUESTS")
                .document(UserManager.myUserId)
                .delete()
                // データがなければ失敗する可能性もある
                .addOnCompleteListener {
                    onComplete.invoke()
                }
        }

        fun deleteUsersRequest(requesterId: String, beRequestedId: String) {
            FirebaseFirestore.getInstance()
                .collection("$USERS/${requesterId}/$REQUESTS")
                .document(beRequestedId)
                .delete()
        }
    }
}