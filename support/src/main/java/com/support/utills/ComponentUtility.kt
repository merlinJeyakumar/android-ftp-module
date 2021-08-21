package com.support.utills

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.support.R


fun Context.isServiceRunning(serviceClass: Class<*>): Boolean {
    val manager: ActivityManager =
        getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Int.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}

fun Context.isAppInstalled(packageName: String): Boolean {
    val pm: PackageManager = packageManager
    return try {
        pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

fun openURL(context: Context, url: String) {
    var url = url
    if (!url.startsWith("http://") && !url.startsWith("https://")) {
        url = "http://$url"
    }
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

fun shareLink(
    activity: Activity,
    title: String,
    body: String,
    urlTitle: String
) {
    try {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_SUBJECT, activity.resources.getString(R.string.app_name))
        i.putExtra(Intent.EXTRA_TEXT, "$title\n$body\n$urlTitle:\n${activity.getPlayStoreUrl()}")
        activity.startActivity(Intent.createChooser(i, "Choose sharing"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}