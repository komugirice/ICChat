package com.komugirice.icchat.util

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import timber.log.Timber

class FireStorageUtil {
    companion object {
        val USER_ICON_PATH = "userIcon"
        val ROOM_PATH = "room"
        val ROOM_ICON_PATH = "roomIcon"

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
                        Timber.d("addOnFailureListener通過")
                    }
                }
        }

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
                        Timber.d("addOnFailureListener通過")
                    }
                }
        }
    }
}