package com.komugirice.icchat.util

import com.example.qiitaapplication.extension.getIdFromEmail
import com.google.firebase.auth.FirebaseAuth

class FireStoreUtil {
    companion object {

        /**
         * ログインユーザIDを返却します。
         *
         * @param ログインユーザID
         */
        fun getLoginUserId(): String {
            val user = FirebaseAuth.getInstance().currentUser
            user?.also {
            } ?: run {
                return ""
            }
            val userId = user.email?.also{return it.getIdFromEmail()} ?: ""
            return userId
        }
    }
}