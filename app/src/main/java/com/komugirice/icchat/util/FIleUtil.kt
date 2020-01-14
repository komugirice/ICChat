package com.komugirice.icchat.util

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import timber.log.Timber

object FIleUtil {
    /**
     * Contents://のUriからファイルパスを取得するパクリもの
     */
    fun getPathFromUri(context: Context, uri: Uri): String? {
        val isAfterKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
        // DocumentProvider
        Timber.d("uri:" + uri.getAuthority())
        if (isAfterKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if ("com.android.externalstorage.documents" ==
                uri.getAuthority()
            ) { // ExternalStorageProvider
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                return if ("primary".equals(type, ignoreCase = true)) {
                    Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                } else {
                    "/stroage/" + type + "/" + split[1]
                }
            } else if ("com.android.providers.downloads.documents" ==
                uri.getAuthority()
            ) { // DownloadsProvider
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri: Uri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
                )
                return getDataColumn(context, contentUri, null, null)
            } else if ("com.android.providers.media.documents" ==
                uri.getAuthority()
            ) { // MediaProvider
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                contentUri = MediaStore.Files.getContentUri("external")
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.getScheme(), ignoreCase = true)) { //MediaStore
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.getScheme(), ignoreCase = true)) { // File
            return uri.getPath()
        }
        return null
    }

    /**
     * パクリものの付属品
     */
    fun getDataColumn(
        context: Context, uri: Uri, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val projection = arrayOf(
            MediaStore.Files.FileColumns.DATA
        )
        try {
            cursor = context.getContentResolver().query(
                uri, projection, selection, selectionArgs, null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val cindex: Int = cursor.getColumnIndexOrThrow(projection[0])
                return cursor.getString(cindex)
            }
        } finally {
            if (cursor != null) cursor.close()
        }
        return null
    }


}