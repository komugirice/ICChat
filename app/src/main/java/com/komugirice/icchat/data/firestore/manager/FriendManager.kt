package com.komugirice.icchat.data.firestore.manager

import android.content.Context
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.data.firestore.Friend
import com.komugirice.icchat.util.FireStoreUtil

class FriendManager {
    companion object {

        fun addFriend(context: Context?, friendId: String) {
            val userId = FireStoreUtil.getLoginUserId()
            FirebaseFirestore.getInstance()
            .collection("friends/$userId/friends")
            .add(Friend().apply {
                this.userId = friendId
                }
            ).addOnSuccessListener {
                Toast.makeText(
                    context,
                    "友だち登録が完了しました。ID:$friendId",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}