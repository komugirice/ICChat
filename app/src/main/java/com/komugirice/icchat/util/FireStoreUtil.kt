package com.komugirice.icchat.util

import androidx.lifecycle.MutableLiveData
import com.example.qiitaapplication.extension.getIdFromEmail
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.BuildConfig
import com.komugirice.icchat.firestore.model.User
import com.komugirice.icchat.firestore.manager.UserManager
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
                val id = user.email?.getIdFromEmail() + "-" ?: ""
                userId = userId.replace("^.{${id.length}}".toRegex(), id)
            }

            return userId
        }

    }
}