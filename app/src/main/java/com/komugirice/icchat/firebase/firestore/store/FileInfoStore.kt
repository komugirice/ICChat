package com.komugirice.icchat.firebase.firestore.store

import android.webkit.MimeTypeMap
import com.komugirice.icchat.extension.getSuffix
import com.google.firebase.firestore.FirebaseFirestore
import com.komugirice.icchat.enums.MessageType
import com.komugirice.icchat.firebase.firestore.model.FileInfo
import java.util.*

class FileInfoStore {
    companion object {
        const val ROOMS = "rooms"
        const val FILE_PATHS = "fileInfo"

        /**
         * ファイル登録（fireStorageの登録名と元のファイル名の変換）
         * @param roomId
         * @param fileName 元のファイル名
         * @param convertName fireStorageの登録名
         *
         */
        fun registerFile(roomId: String, fileName: String, convertName: String, onComplete: () -> Unit) {
            // MimeType
            val mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileName.getSuffix())

            val fileObj = FileInfo().apply {
                this.documentId = UUID.randomUUID().toString()
                this.roomId = roomId
                this.name = fileName
                this.mimeType = mimetype
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
        fun getFile(roomId: String, convertName: String, type: Int, onSuccess: (FileInfo?) -> Unit) {
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
                        it.result?.toObjects(FileInfo::class.java)?.also {
                            val file = it.firstOrNull()
                            onSuccess.invoke(file)
                        }
                    }
                }
        }

        /**
         * ファイル削除
         * @param fileInfo
         * @param onComplete
         *
         */
        fun deleteFile(fileInfo: FileInfo, onComplete: () -> Unit) {

            FirebaseFirestore.getInstance()
                .collection("$ROOMS/${fileInfo.roomId}/$FILE_PATHS")
                .document(fileInfo.documentId)
                .delete()
                .addOnCompleteListener {
                    onComplete.invoke()
                }
        }
    }
}