package com.komugirice.icchat.util

import android.text.Html
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.google.gson.Gson
import com.komugirice.icchat.ICChatApplication.Companion.applicationContext
import com.komugirice.icchat.R
import com.komugirice.icchat.enums.MessageType
import com.komugirice.icchat.extension.*
import com.komugirice.icchat.firebase.firestore.manager.UserManager
import com.komugirice.icchat.firebase.firestore.model.Message
import com.komugirice.icchat.firebase.firestore.model.Request
import com.komugirice.icchat.firebase.firestore.model.Room
import com.squareup.picasso.Picasso
import timber.log.Timber
import java.io.File
import java.util.*


object ICChatUtil {
    fun deleteShareImages() {
        var shareImagesDir = File(StringBuilder().append(applicationContext.cacheDir).append("/share_image").toString())
        Timber.d("shareImagesDir.exists:${shareImagesDir.exists()}")
        if (!shareImagesDir.exists())
            return
        shareImagesDir.listFiles().forEach {
            Timber.d("fileName:${it.name}")
            if (it.exists()) {
                val isDeleted = it.delete()
                Timber.d("isDeleted:$isDeleted")
            }
        }
    }

    fun deleteCacheDir() {
        var cacheDir = File(StringBuilder().append(applicationContext.cacheDir).toString())
        Timber.d("cacheDir.exists:${cacheDir.exists()}")
        if (!cacheDir.exists())
            return
        cacheDir.listFiles().forEach {
            Timber.d("fileName:${it.name}")
            if (it.exists()) {
                val isDeleted = it.delete()
                Timber.d("isDeleted:$isDeleted")
            }
        }
    }
}