package com.komugirice.icchat.extension

import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import com.google.gson.Gson
import com.komugirice.icchat.ICChatApplication
import com.komugirice.icchat.R
import com.komugirice.icchat.enums.MessageType
import com.komugirice.icchat.firebase.firestore.model.Message
import com.komugirice.icchat.util.FireStorageUtil
import com.squareup.picasso.Picasso
import com.yalantis.ucrop.view.TransformImageView
import timber.log.Timber
import java.io.File

/**
 * メッセージの画像を設定する
 *
 * @param url
 *
 */
@BindingAdapter("messageImageUrl")
fun TransformImageView.loadMessageImage(message: Message) {
    // 画像タイプ判定
    if(!MessageType.getValue(message.type).isImage) return
    var file = File.createTempFile("${System.currentTimeMillis()}", ".temp", ICChatApplication.applicationContext.cacheDir)
    FireStorageUtil.getRoomMessageImage(message, {
        // 取得成功
        setImageUri(it, file.toUri())
        Picasso.get().load(file.toUri()).into(this)
    }, {
        // 取得失敗
        this.visibility = View.GONE
        this.isClickable = false
    })

}

fun TransformImageView.loadImageFromUri(uri: Uri?) {
    // 画像タイプ判定
    if(uri == null) return
    var file = File.createTempFile("${System.currentTimeMillis()}", ".temp", ICChatApplication.applicationContext.cacheDir)
    setImageUri(uri, file.toUri())
    Picasso.get().load(file.toUri()).into(this)

}