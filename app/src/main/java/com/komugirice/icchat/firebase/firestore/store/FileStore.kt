package com.komugirice.icchat.firebase.firestore.store

import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.enums.MessageType
import com.komugirice.icchat.firebase.firestore.model.File
import java.util.*

class FileStore {
    companion object {
        const val ROOMS = "rooms"
        const val FILE_PATHS = "filepaths"

        /**
         * ファイル登録（fireStorageの登録名と元のファイル名の変換）
         * @param roomId
         * @param fileName 元のファイル名
         * @param convertName fireStorageの登録名
         *
         */
        fun registerFile(roomId: String, fileName: String, convertName: String, onComplete: () -> Unit) {
            val fileObj = File().apply {
                this.documentId = UUID.randomUUID().toString()
                this.roomId = roomId
                this.name = fileName
                this.convertName = convertName
            }

            FirebaseFirestore.getInstance()
                .collection("$ROOMS/$roomId/$FILE_PATHS")
                .document(fileObj.documentId)
                .set(fileObj)
                .addOnCompleteListener {
                    onComplete.invoke()
                }
        }

        /**
         * ファイル取得（fireStorageの登録名と元のファイル名の変換）
         * @param roomId
         * @param convertName fireStorageの登録名
         * @param onSuccess: (File?)
         *
         */
        fun getFile(roomId: String, convertName: String, type: Int, onSuccess: (File?) -> Unit) {
            if(!(type == MessageType.IMAGE.id || type == MessageType.FILE.id)) {
                onSuccess.invoke(null)
                return
            }
            FirebaseFirestore.getInstance()
                .collection("$ROOMS/$roomId/$FILE_PATHS")
                .whereEqualTo("convertName", convertName)
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result?.toObjects(File::class.java)?.also {
                            val file = it.firstOrNull()
                            onSuccess.invoke(file)
                        }
                    }
                }
        }

        /**
         * ファイル削除
         * @param file
         * @param onComplete
         *
         */
        fun deleteFile(file: File, onComplete: () -> Unit) {

            FirebaseFirestore.getInstance()
                .collection("$ROOMS/${file.roomId}/$FILE_PATHS")
                .document(file.documentId)
                .delete()
                .addOnCompleteListener {
                    onComplete.invoke()
                }
        }
    }
}