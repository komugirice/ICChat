package com.komugirice.icchat.util

import com.komugirice.icchat.extension.getIdFromEmail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.komugirice.icchat.BuildConfig
import com.komugirice.icchat.firebase.firestore.model.Interest
import com.komugirice.icchat.firebase.firestore.model.Version
import com.komugirice.icchat.firebase.firestore.store.InterestStore
import timber.log.Timber
import java.util.*

class FireStoreUtil {
    companion object {

        /**
         * ログインユーザIDを返却します。
         *
         * @param ログインユーザID
         */
        fun createLoginUserId(): String {
            val user = FirebaseAuth.getInstance().currentUser
            user?.also {
            } ?: run {
                return ""
            }

            var userId = UUID.randomUUID().toString()
            if(BuildConfig.DEBUG) {
                val id = user.email?.getIdFromEmail() + "-"
                userId = userId.replace("^.{${id.length}}".toRegex(), id)
            }

            return userId
        }

        /**
         * バージョン取得
         * @param onSuccess
         *
         */
        fun getVersion(onSuccess: (String) -> Unit) {
            FirebaseFirestore.getInstance()
                .collection("version")
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result?.toObjects(Version::class.java)?.firstOrNull().apply {
                            this?.version?.apply{
                                onSuccess.invoke(this)
                            } ?: run {
                                Timber.d("getVersion Failed")
                            }
                        }
                    } else {
                        Timber.d(it.exception)
                        Timber.d("getVersion Failed")
                    }
                }
        }

    }
}