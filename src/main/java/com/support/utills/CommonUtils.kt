package com.support.utills

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.support.R
import java.io.File
import java.util.*


fun getProgress(progressed: Long, totalCount: Long) {
    ((progressed * 100f) / totalCount)
}

fun Activity.startCall(phoneNumber: String) {
    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
    startActivity(intent)
}

fun Any.toJson(): String? {
    try {
        return Gson().toJson(this)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun Context.shareFileText(
    fileList: List<File>? = null,
    fileMimeType: Array<String> = arrayOf("text/*"),
    emailAddress: String? = null,
    emailSubject: String = "YOUR_SUBJECT_HERE - ${this.getString(R.string.app_name)}",
    description: String = ""
) {
    val intentShareFile = Intent(Intent.ACTION_SEND)
    if (fileList != null) {
        val uriList = arrayListOf<Uri>()
        fileList.forEach { file ->
            uriList.add(
                FileProvider.getUriForFile(
                    this@shareFileText,
                    "${this@shareFileText.packageName}.provider",
                    file
                )
            )
        }
        intentShareFile.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)
        intentShareFile.type = "*/*"
    } else {
        intentShareFile.type = "text/*"
    }
    intentShareFile.action = Intent.ACTION_SEND_MULTIPLE;
    intentShareFile.putExtra(Intent.EXTRA_MIME_TYPES, fileMimeType)
    emailAddress?.let {
        intentShareFile.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress));
        intentShareFile.putExtra(
            Intent.EXTRA_SUBJECT,
            emailSubject
        )
    }

    intentShareFile.putExtra(
        Intent.EXTRA_TEXT, description
    );
    startActivity(
        Intent.createChooser(intentShareFile, "Share").setFlags(FLAG_ACTIVITY_NEW_TASK)
    )
}

fun Context.getMimeType(uri: Uri): String? {
    var mimeType: String? = null
    mimeType = if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
        contentResolver.getType(uri)
    } else {
        val fileExtension: String = MimeTypeMap.getFileExtensionFromUrl(
            uri
                .toString()
        )
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(
            fileExtension.toLowerCase(Locale.getDefault())
        )
    }
    return mimeType
}

fun Context.getShareText(): String {
    return "Excited to share it from *${this.resources.getString(
        R.string.app_name
    )}* App,\nDownload an app from PlayStore https://play.google.com/store/apps/details?id=${this.applicationContext.packageName}&hl=en_IN"
}