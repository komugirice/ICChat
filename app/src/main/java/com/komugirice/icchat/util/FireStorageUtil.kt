package com.komugirice.icchat.util

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import timber.log.Timber

class FireStorageUtil {
    companion object {
        val USER_ICON_PATH = "userIcon"
        val ROOM_PATH = "room"
        val ROOM_ICON_PATH = "roomIcon"

        /**
         * ユーザアイコン取得
         * @param userId: String
         * @param onSuccess
         *
         */
        fun getUserIconImage(userId: String, onSuccess: (Uri) -> Unit) {
            val url = "${USER_ICON_PATH}/${userId}"
            FirebaseStorage.getInstance().getReference(url).list(1)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result?.items?.firstOrNull()?.downloadUrl.apply {
                            this?.addOnSuccessListener {
                                onSuccess.invoke(it)
                            }
                        }
                    } else {
                        Timber.d(it.exception)
                        Timber.d(url)
                        Timber.d("getUserIconImage Failed")
                    }
                }
        }

        /**
         * グループアイコン取得
         * @param roomId: String
         * @param onSuccess
         *
         */
        fun getGroupIconImage(roomId: String, onSuccess: (Uri) -> Unit) {
            val url = "${ROOM_PATH}/${roomId}/${ROOM_ICON_PATH}"
            FirebaseStorage.getInstance().getReference(url).list(1)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result?.items?.firstOrNull()?.downloadUrl.apply {
                            this?.addOnSuccessListener {
                                onSuccess.invoke(it)
                            }
                        }
                    } else {
                        Timber.d(it.exception)
                        Timber.d(url)
                        Timber.d("getGroupIconImage Failed")
                    }
                }
        }

        /**
         * グループアイコン削除
         * @param roomId: String
         * @param onSuccess
         *
         */
        fun deleteGroupIconImage(roomId: String, onSuccess: (Task<Void>) -> Unit) {
            val url = "${ROOM_PATH}/${roomId}/${ROOM_ICON_PATH}"
            FirebaseStorage.getInstance().getReference(url).list(1)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result?.items?.firstOrNull()?.downloadUrl.apply {
                            this?.addOnSuccessListener {
                                FirebaseStorage.getInstance()
                                    .getReferenceFromUrl(it.toString())
                                    .delete()
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            Timber.d("グループアイコン削除: ${this?.result.toString()}")
                                            onSuccess.invoke(it)
                                        } else {
                                            Timber.d(it.exception)
                                            Timber.d(this?.result.toString())
                                            Timber.d("deleteGroupIconImage delete Failed")
                                        }
                                    }
                            }
                        }
                    } else {
                        Timber.d(it.exception)
                        Timber.d(url)
                        Timber.d("deleteGroupIconImage get Failed")
                    }
                }
        }
    }
}