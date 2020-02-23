package com.komugirice.icchat.util

import com.komugirice.icchat.extension.getIdFromEmail
import com.google.firebase.auth.FirebaseAuth
import com.komugirice.icchat.BuildConfig
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

    }
}