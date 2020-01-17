package com.komugirice.icchat.extension

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import java.io.File

/**
 * Get the file name from uri.
 *
 * @param context context
 * @param uri uri
 * @return file name
 */
fun Uri?.getFileNameFromUri(context: Context): String? { // is null
    if (null == this) {
        return null
    }
    // get scheme
    val scheme = this.scheme
    // get file name
    var fileName: String? = null
    when (scheme) {
        "content" -> {
            val projection =
                arrayOf(MediaStore.MediaColumns.DISPLAY_NAME)
            val cursor = context.contentResolver
                .query(this, projection, null, null, null)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    fileName = cursor.getString(
                        cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                    )
                }
                cursor.close()
            }
        }
        "file" -> fileName = File(this.path).getName()
        else -> {
        }
    }
    return fileName
}