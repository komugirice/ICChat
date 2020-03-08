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
                .whereEqualTo(Interest::delFlg.name, false)
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result?.toObjects(Interest::class.java)?.also {
                            onSuccess.invoke(it)
                        }
                    } else {
                        // 0件の場合はこっち
                        onSuccess.invoke(mutableListOf())
                    }
                }
        }

        /**
         * Interest取得
         * @param userId
         * @param onSuccess
         *
         */
        fun getDeleteInterests(userId: String, onSuccess: (List<Interest>) -> Unit) {
            FirebaseFirestore.getInstance()
                .collection("$USERS/$userId/$INTERESTS")
                .whereEqualTo(Interest::delFlg.name, true)
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result?.toObjects(Interest::class.java)?.also {
                            onSuccess.invoke(it)
                        }
                    } else {
                        // 0件の場合はこっち
                        onSuccess.invoke(mutableListOf())
                    }
                }
        }

        /**
         * Interest登録
         * (ログインユーザからのみ登録可能）
         * @param interest
         * @param onSuccess
         *
         */
        fun registerInterest(interest: Interest, onSuccess: () -> Unit) {

            FirebaseFirestore.getInstance()
                .collection("$USERS/${UserManager.myUserId}/$INTERESTS")
                .document(interest.documentId)
                .set(interest)
                .addOnSuccessListener {
                    onSuccess.invoke()
                }
        }

        /**
         * Interest登録（別サイトからIntent.ACTION_SEND）
         * (ログインユーザからのみ登録可能）
         * @param ogpData
         * @param onSuccess
         *
         */
        fun registerInterestWithOgp(userId: String, ogpData: OgpData, onSuccess: () -> Unit) {

            val interest = Interest(ogpData).apply{
                this.documentId = UUID.randomUUID().toString()
                this.isOgp = true
            }

            FirebaseFirestore.getInstance()
                .collection("$USERS/$userId/$INTERESTS")
                .document(interest.documentId)
                .set(interest)
                .addOnSuccessListener {
                    onSuccess.invoke()
                }
        }

        /**
         * Interest削除（論理削除）
         * (ログインユーザからのみ削除可能）
         * @param interest
         * @param onComplete
         *
         */
        fun deleteInterest(interest: Interest, onComplete: () -> Unit) {
            // 削除フラグON
            interest.delFlg = true
            FirebaseFirestore.getInstance()
                .collection("$USERS/${UserManager.myUserId}/$INTERESTS")
                .document(interest.documentId)
                .set(interest)
                .addOnCompleteListener {
                    onComplete.invoke()
                }
        }

        /**
         * Interest削除（物理削除）
         * (ログインユーザからのみ削除可能）
         * @param interest
         * @param onComplete
         *
         */
        fun deleteCompleteInterest(interest: Interest, onComplete: () -> Unit) {
            // 削除フラグON
            interest.delFlg = true
            FirebaseFirestore.getInstance()
                .collection("$USERS/${UserManager.myUserId}/$INTERESTS")
                .document(interest.documentId)
                .delete()
                .addOnCompleteListener {
                    onComplete.invoke()
                }
        }

        /**
         * Interest復元
         * (ログインユーザからのみ可能）
         * @param interest
         * @param onComplete
         *
         */
        fun restoreInterest(interest: Interest, onComplete: () -> Unit) {
            // 削除フラグOFF
            interest.delFlg = false
            FirebaseFirestore.getInstance()
                .collection("$USERS/${UserManager.myUserId}/$INTERESTS")
                .document(interest.documentId)
                .set(interest)
                .addOnCompleteListener {
                    onComplete.invoke()
                }
        }

    }
}