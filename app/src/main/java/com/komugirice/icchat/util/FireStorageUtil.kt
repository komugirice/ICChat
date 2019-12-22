package com.komugirice.icchat.util

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.komugirice.icchat.extension.setRoundedImageView
import com.komugirice.icchat.firestore.manager.UserManager
import kotlinx.android.synthetic.main.activity_profile_setting.*
import timber.log.Timber

class FireStorageUtil {
    companion object {
        val USER_ICON_PATH = "userIcon"

        fun getUserIconImage(userId: String, onSuccess: (Task<Uri>?) -> Unit) {
            val url = "${userId}/${USER_ICON_PATH}"
            FirebaseStorage.getInstance().getReference(url).list(1)
                .addOnCompleteListener {
                    Timber.d("addOnCompleteListener通過")
                }
                .addOnSuccessListener {
                    it.items.firstOrNull()?.downloadUrl.apply {
                        onSuccess.invoke(this)
                    }
                }
                .addOnFailureListener {
                    Timber.d(it)
                    Timber.d("addOnFailureListener通過")
                }
        }
    }
}