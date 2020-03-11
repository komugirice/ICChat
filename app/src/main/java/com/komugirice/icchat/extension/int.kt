package com.komugirice.icchat.extension

import android.content.ContentResolver
import android.net.Uri
import com.komugirice.icchat.ICChatApplication.Companion.applicationContext


fun Int.convertUrlFromDrawableResId(): Uri? {
    val sb = StringBuilder()
    sb.append(ContentResolver.SCHEME_ANDROID_RESOURCE)
    sb.append("://")
    sb.append(applicationContext.getResources().getResourcePackageName(this))
    sb.append("/")
    sb.append(applicationContext.getResources().getResourceTypeName(this))
    sb.append("/")
    sb.append(applicationContext.getResources().getResourceEntryName(this))
    return  Uri.parse(sb.toString())
}