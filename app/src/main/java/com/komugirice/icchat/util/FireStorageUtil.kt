package com.komugirice.icchat.util

import android.content.Context
import android.net.Uri
import com.komugirice.icchat.extension.getRemoveSuffixName
import com.komugirice.icchat.extension.getSuffix
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.util.FileUtil
import com.google.firebase.storage.FirebaseStorage
import com.komugirice.icchat.enums.MessageType
import com.komugirice.icchat.extension.makeTempFile
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import com.komugirice.icchat.firebase.firestore.model.Message
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

class FireStorageUtil {
    companion object {
        val USER_ICON_PATH = "userIcon"
        val ROOM_PATH = "room"
        val ROOM_ICON_PATH = "roomIcon"
        val FILE_PATH = "file"
        val IMAGE_PATH = "image"
        val INTEREST_PATH = "interest"

        /**
         * ユーザアイコン取得
         * @param userId: String
         * @param onSuccess
         *
         */
        fun getUserIconImage(userId: String, onSuccess: (Uri?) -> Unit) {
            val url = "${USER_ICON_PATH}/${userId}"
            FirebaseStorage.getInstance().getReference(url).list(1)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result?.items?.firstOrNull()?.downloadUrl.apply {
                            this?.addOnSuccessListener {
                                onSuccess.invoke(it)
                            } ?: run {
                                onSuccess.invoke(null)
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
        fun getGroupIconImage(roomId: String, onSuccess: (Uri?) -> Unit) {
            val url = "${ROOM_PATH}/${roomId}/${ROOM_ICON_PATH}"
            FirebaseStorage.getInstance().getReference(url).list(1)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result?.items?.firstOrNull()?.downloadUrl.apply {
                            this?.addOnSuccessListener {
                                onSuccess.invoke(it)
                            } ?: run {
                                onSuccess.invoke(null)
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
                                            Timber.d("グループアイコン削除: ${this.result.toString()}")
                                        } else {
                                            Timber.d(it.exception)
                                            Timber.d(this.result.toString())
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
        fun registRoomMessageImage(roomId: String, uri: Uri, convertName: String, onComplete: () -> Unit) {

            FirebaseStorage.getInstance().reference.child("${ROOM_PATH}/${roomId}/${IMAGE_PATH}/${convertName}")
                .putFile(uri)
                .addOnCompleteListener{
                    onComplete.invoke()
                }

        }

        /**
         * チャット画面からファイル投稿
         * @param context: ※applicationContextだと落ちたので…
         * @param roomId: String
         * @param uri: Uri ローカルストレージから取得したファイルのUri
         * @param onSuccess
         *
         */
        fun registRoomMessageFile(context: Context, roomId: String, uri: Uri, convertName: String, onComplete: () -> Unit) {

            val path = ICChatFileUtil.getPathFromUri(context, uri)
            val tmpFile = File(path)
            //val tmpFile = uri.makeTempFile(context, convertName.getRemoveSuffixName(), convertName.getSuffix())
            val tmpUri = Uri.fromFile(tmpFile)
            FirebaseStorage.getInstance().reference.child("${ROOM_PATH}/${roomId}/${FILE_PATH}/${convertName}")
                .putFile(tmpUri)
                .addOnCompleteListener{
                    onComplete.invoke()
                }

        }

        /**
         * チャット画面の画像・ファイル投稿をダウンロード
         * @param message: Message
         * @param tempFile
         * @param onComplete
         * @param onError
         *
         */
        fun downloadRoomMessageFile(message: Message, tempFile: File, onComplete: () -> Unit, onError: () -> Unit) {
            var path = "${ROOM_PATH}/${message.roomId}/"
            path += if(message.type == MessageType.IMAGE.id) IMAGE_PATH else FILE_PATH
            path += "/${message.message}"
            FirebaseStorage.getInstance().reference.child(path).getFile(tempFile).addOnSuccessListener {
                onComplete.invoke()
            }.addOnFailureListener {
                onError.invoke()
            }
        }

        /**
         * チャット画面の画像投稿を取得
         * @param roomId: String
         * @param message: Message
         * @param onSuccess
         *
         */
        fun getRoomMessageImage(message: Message, onSuccess: (Uri) -> Unit, onFailure: () -> Unit) {
            FirebaseStorage.getInstance().reference.child("${ROOM_PATH}/${message.roomId}/${IMAGE_PATH}/${message.message}")
                .downloadUrl
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result?.apply {
                            onSuccess.invoke(this)
                        }
                    } else {
                        Timber.e(it.exception)
                        Timber.d("getRoomMessageImage Failed")
                        onFailure.invoke()
                    }
                }

        }

        /**
         * チャット画面の画像/ファイルを削除
         * @param roomId: String
         * @param message: Message
         * @param onSuccess
         *
         */
        fun deleteRoomMessageFile(message: Message, onSuccess: () -> Unit) {
            if(!(message.type == MessageType.IMAGE.id || message.type == MessageType.FILE.id)) {
                onSuccess.invoke()
                return
            }

            var path = "${ROOM_PATH}/${message.roomId}/"
            path += if(message.type == MessageType.IMAGE.id) IMAGE_PATH else FILE_PATH
            path += "/${message.message}"
            FirebaseStorage.getInstance().reference.child(path)
                .delete()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        onSuccess.invoke()
                    } else {
                        Timber.e(it.exception)
                        Timber.d("deleteRoomMessageFile Failed")
                        onSuccess.invoke()
                    }
                }

        }

        /**
         * 興味画面の投稿画像を取得
         * @param roomId: String
         * @param fileName: String
         * @param onSuccess
         *
         */
        fun getInterestImage(userId: String, fileName: String, onSuccess: (Uri) -> Unit) {
            FirebaseStorage.getInstance().reference.child("${INTEREST_PATH}/$userId/${IMAGE_PATH}/$fileName")
                .downloadUrl
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result?.apply {
                            onSuccess.invoke(this)
                        }
                    } else {
                        Timber.e(it.exception)
                        Timber.d("getRoomMessageImage Failed")
                    }
                }

        }

        /**
         * 興味入力画面より画像登録
         * @param fileName: String
         * @param uri: Uri
         * @param onSuccess
         *
         */
        fun registInterestImage(fileName: String?, uri: Uri, onComplete: () -> Unit) {

            if(fileName == null) onComplete.invoke()

            FirebaseStorage.getInstance().reference.child("${INTEREST_PATH}/${UserManager.myUserId}/${IMAGE_PATH}/${fileName}")
                .putFile(uri)
                .addOnCompleteListener{
                    onComplete.invoke()
                }

        }

        /**
         * 興味入力画面より画像削除
         * @param fileName: String
         * @param uri: Uri
         * @param onSuccess
         *
         */
        fun deleteInterestImage(fileName: String?, onSuccess: () -> Unit) {

            if(fileName == null) onSuccess.invoke()

            FirebaseStorage.getInstance().reference.child("${INTEREST_PATH}/${UserManager.myUserId}/${IMAGE_PATH}/${fileName}")
                .delete()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        onSuccess.invoke()
                    } else {
                        Timber.e(it.exception)
                        Timber.d("registRoomMessageImage Failed")
                        onSuccess.invoke()
                    }
                }


        }


    }
}