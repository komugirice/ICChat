package com.komugirice.icchat.extension

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.komugirice.icchat.ICChatApplication.Companion.applicationContext
import java.io.File
import java.io.FileOutputStream

/**
 * Get the file name from uri.
 *
 * @param context context
 * @param uri uri
 * @return file name
 */
fun Uri?.getFileNameFromUri(): String? { // is null
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
            val cursor = applicationContext.contentResolver
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

/**
 * ファイル作成　
 * ※ローカルストレージから:◯、fireStorageから:☓　あやしい時がある
 */
fun Uri.makeTempFile(context: Context, filename: String, suffix: String): File? {
    val file = File.createTempFile(filename, suffix, context.cacheDir)
    val inputStream = context.contentResolver.openInputStream(this)
    inputStream?.apply{
        val fileOutputStream = FileOutputStream(file)
        val buffer = ByteArray(1024)
        while (true) {
            val length = inputStream.read(buffer)
            if (length <= 0)
                break
            fileOutputStream.write(buffer, 0, length)
        }
        return file
    }
    return null
}

/**
 * CreateBy Jane
 * Uriで指定されたFileをAppのCacheに保存する
 */
fun Uri.makeTempFile(): File? {
    val cursor = applicationContext.contentResolver.query(this, null, null, null, null)
    var fileName = ""
    if (cursor != null && cursor.moveToFirst())
        cursor.columnNames.firstOrNull { it == MediaStore.MediaColumns.DISPLAY_NAME }?.also {
            fileName = cursor.getString(cursor.getColumnIndex(it))
        }
    if (fileName.isEmpty())
        fileName = "${System.currentTimeMillis()}"
    val file = File.createTempFile(fileName, "", applicationContext.cacheDir)
    val inputStream = applicationContext.contentResolver.openInputStream(this)
    if (inputStream != null) {
        val fileOutputStream = FileOutputStream(file)
        val buffer = ByteArray(1024)
        while (true) {
            val length = inputStream.read(buffer)
            if (length <= 0)
                break
            fileOutputStream.write(buffer, 0, length)
        }
        return file
    }
    return null
}