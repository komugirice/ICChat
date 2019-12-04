package com.komugirice.icchat.data.firestore.manager

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.qiitaapplication.extension.toDate
import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.data.firestore.Friend
import com.komugirice.icchat.data.firestore.Room
import com.komugirice.icchat.data.firestore.User
import com.komugirice.icchat.util.FireStoreUtil

class RoomManager {
    companion object {
        fun registerDebugUserRoom(registUserList: MutableList<User>) {
            val registRoomList: MutableList<Pair<String, Room>> = mutableListOf()

            registUserList.forEach{
                registRoomList.add(
                    Pair(
                        it.userId,
                        Room().apply{
                            this.name = it.name
                        }
                    )
                )
            }

            registRoomList.forEach {

            }
        }

        fun getLoginUserRooms(pRooms: MutableLiveData<MutableList<Pair<Room, List<Friend>>>>) {
            val userId = FireStoreUtil.getLoginUserId()
            var rooms: MutableList<Pair<Room, List<Friend>>> = mutableListOf()
            // rooms取得
            FirebaseFirestore.getInstance()
                .collection("rooms/$userId/rooms")
                .get()
                .addOnSuccessListener {

                    var tempRooms = it.toObjects(Room::class.java)
//                    it.documents.forEach{
//                        temp.add(
//                            Room().apply {
//                                this.documentId = it.id
//                                this.name = it.get("name").toString()
//                                this.createdAt = it.get("createdAt").toString().toDate("yyyy/MM/dd hh:mm:ss")
//                            }
//                        )
//                    }
                    // roomsに紐づくfriens取得
                    tempRooms.forEach {
                        val room = it
                        FirebaseFirestore.getInstance()
                            .collection("rooms/$userId/rooms/${it.documentId}/friends")
                            .get()
                            .addOnSuccessListener {
                                var friends = it.toObjects(Friend::class.java)

                                rooms.add(
                                    Pair(room, friends)
                                )

                            }
                    }
                    pRooms.postValue(rooms)

                }
        }

        fun getRoomsUsers(){

        }
    }
}