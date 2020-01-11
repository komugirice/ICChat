package com.komugirice.icchat.util

import android.net.Uri
import com.example.qiitaapplication.extension.getSuffix
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import timber.log.Timber

class FireStorageUtil {
    companion object {
        val USER_ICON_PATH = "userIcon"
        val ROOM_PATH = "room"
        val ROOM_ICON_PATH = "roomIcon"
        val FILE_PATH = "file"
        val IMAGE_PATH = "image"

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
        fun deleteGroupIconImage(roomId: String, onSuccess: () -> Unit) {
            val url = "${ROOM_PATH}/${roomId}/${ROOM_ICON_PATH}"
            FirebaseStorage.getInstance().getReference(url).list(1)
                // 画像が未登録の場合、入らない
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
            // 画像が未登録の場合の不具合対応
            onSuccess.invoke()
        }

        /**
         * チャット画面から画像投稿
         * @param roomId: String
         * @param uri: Uri
         * @param onSuccess
         *
         */
        fun registRoomMessageImage(roomId: String, uri: Uri, onComplete: () -> Unit) {
            val fileName = uri.lastPathSegment ?: ""
            val extension = fileName.getSuffix()
            val uploadUrl = "${System.currentTimeMillis()}.${extension}"
            FirebaseStorage.getInstance().reference.child("${ROOM_PATH}/${roomId}/${IMAGE_PATH}/${uploadUrl}")
                .putFile(uri)
                .addOnCompleteListener{
                    onComplete.invoke()
                }

        }
    }
}