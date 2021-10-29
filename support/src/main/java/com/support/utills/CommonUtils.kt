package com.support.utills

import android.app.Activity
import android.content.*
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.support.R
import java.io.File
import java.util.*


fun getProgress(progressed: Long, totalCount: Long): Float {
    return ((progressed * 100f) / totalCount)
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

fun <T> String.jsonToList(): List<T>? {
    return try {
        Gson().fromJson(this, object : TypeToken<List<T>>() {}.type)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun Activity.shareText(description: String) {
    val sharebody = description

    // The value which we will sending through data via
    // other applications is defined
    // via the Intent.ACTION_SEND

    // The value which we will sending through data via
    // other applications is defined
    // via the Intent.ACTION_SEND
    val intentt = Intent(Intent.ACTION_SEND)

    // setting type of data shared as text

    // setting type of data shared as text
    intentt.type = "text/plain"
    intentt.putExtra(Intent.EXTRA_SUBJECT, "Subject Here")

    // Adding the text to share using putExtra

    // Adding the text to share using putExtra
    intentt.putExtra(Intent.EXTRA_TEXT, sharebody)
    startActivity(this, Intent.createChooser(intentt, "Share Via"), null)
}

fun Context.shareFileText(
    fileList: List<File>? = null,
    fileMimeType: Array<String> = arrayOf("text/*"),
    emailAddress: String? = null,
    emailSubject: String = "YOUR_SUBJECT_HERE - ${this.getString(R.string.app_name)}",
    description: String = "",
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
        Intent.createChooser(intentShareFile, null).setFlags(FLAG_ACTIVITY_NEW_TASK)
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
    return "Excited to share it from *${
        this.resources.getString(
            R.string.app_name
        )
    }* App,\nDownload an app from PlayStore https://play.google.com/store/apps/details?id=${this.applicationContext.packageName}&hl=en_IN"
}

fun getAvailableMemory(): Long {
    val runTime = Runtime.getRuntime()
    val usedMemInMB = (runTime.totalMemory() - runTime.freeMemory()) / 1048576L;
    val maxHeapSizeInMB = runTime.maxMemory() / 1048576L;
    return maxHeapSizeInMB - usedMemInMB;
}

fun Context.openLinkInternally(text: String) {
    val defaultBrowser =
        Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER)
    defaultBrowser.data = Uri.parse(text)
    startActivity(this, defaultBrowser, null)
}

fun Context.openLinkExternally(text: String) {
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(text))
    startActivity(this, browserIntent, null)
}

fun Context.pasteClipboardText(): String? {
    val clipboard: ClipboardManager =
        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip: ClipData = clipboard.primaryClip ?: return null
    val item = clip.getItemAt(0) ?: return null
    return item.text.toString() ?: return null
}

fun Context.getPlayStoreUrl(): String {
    return "https://play.google.com/store/apps/details?id=" + applicationContext.packageName
}

fun Activity.sharePlayStoreUrl() {
    shareFileText(description = "${resources.getString(R.string.app_name)}\n\nHope you like this application!\n\n${getPlayStoreUrl()}")
}