package com.komugirice.icchat.firebase.firestore.store

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.komugirice.icchat.data.model.OgpData
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import com.komugirice.icchat.firebase.firestore.model.Interest
import com.komugirice.icchat.firebase.firestore.model.Message
import java.util.*

class InterestStore {
    companion object {
        const val USERS = "users"
        const val INTERESTS = "interests"

        /**
         * Interest取得
         * @param userId
         * @param onSuccess
         *
         */
        fun getInterests(userId: String, onSuccess: (List<Interest>) -> Unit) {
            FirebaseFirestore.getInstance()
                .collection("$USERS/$userId/$INTERESTS")
                .orderBy(Interest::createdAt.name, Query.Direction.ASCENDING)
                .get()
                // 必ず成功する。interestsが作られて無くても成功する。
                .addOnSuccessListener {
                    val interests = it.toObjects(Interest::class.java)
                    onSuccess.invoke(interests)
                }
        }

        /**
         * Interest登録（別サイトからIntent.ACTION_SEND）
         * (ログインユーザからのみ登録可能）
         * @param ogpData
         * @param onSuccess
         *
         */
        fun registerInterestWithOgp(ogpData: OgpData, onSuccess: () -> Unit) {

            val interest = Interest(ogpData).apply{
                this.documentId = UUID.randomUUID().toString()
                this.isOgp = true
            }

            FirebaseFirestore.getInstance()
                .collection("$USERS/${UserManager.myUserId}/$INTERESTS")
                .document(interest.documentId)
                .set(interest)
                .addOnSuccessListener {
                    onSuccess.invoke()
                }
        }

    }
}