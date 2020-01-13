package com.komugirice.icchat.util

import android.content.Context
import android.widget.Toast
import com.komugirice.icchat.ICChatApplication.Companion.applicationContext
import com.komugirice.icchat.R
import com.komugirice.icchat.firebase.fcm.FcmApi
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import timber.log.Timber

class FcmUtil {
    companion object {

        /**
         * 友だち申請のfcm送信
         * @param context
         * @param userId
         *
         */
        fun sendRequestFriendFcm(userId: String) {
            val  targetUser = UserManager.getTargetUser(userId)
            targetUser?.fcmToken?.apply{
                val message = applicationContext.getString(R.string.fcm_friend_request, UserManager.myUser.name)
                val token = targetUser.fcmToken
                val type = "0"
                FcmApi.sendMessageOkHttp(token, message, type)
            } ?: run{

                Timber.e("$userId:FCMトークンがnullです")

            }
        }

        /**
         * 友だち申請承認のfcm送信
         * @param context
         * @param userId
         *
         */
        fun sendAcceptFriendFcm(userId: String) {
            val  targetUser = UserManager.getTargetUser(userId)
            targetUser?.fcmToken?.apply{
                val message = applicationContext.getString(R.string.fcm_friend_request_accept, UserManager.myUser.name)
                val token = targetUser.fcmToken
                val type = "0"
                FcmApi.sendMessageOkHttp(token, message, type)
            } ?: run{
                Timber.e("$userId:FCMトークンがnullです")
            }
        }

        /**
         * 友だち申請拒否のfcm送信
         * @param context
         * @param userId
         *
         */
        fun sendDenyFriendFcm(userId: String) {
            val  targetUser = UserManager.getTargetUser(userId)
            targetUser?.fcmToken?.apply{
                val message = applicationContext.getString(R.string.fcm_friend_request_deny, UserManager.myUser.name)
                val token = targetUser.fcmToken
                val type = "0"
                FcmApi.sendMessageOkHttp(token, message, type)
            } ?: run{
                Timber.e("$userId:FCMトークンがnullです")
            }
        }

        /**
         * グループ招待のfcm送信
         * @param context
         * @param userId
         *
         */
        fun sendRequestGroupFcm(userId: String, groupName: String) {
            val  targetUser = UserManager.getTargetUser(userId)
            targetUser?.fcmToken?.apply{
                val message = applicationContext.getString(R.string.fcm_group_request, groupName)
                val token = targetUser.fcmToken
                val type = "0"
                FcmApi.sendMessageOkHttp(token, message, type)
            } ?: run{
                Timber.e("$userId:FCMトークンがnullです")
            }
        }
    }
}