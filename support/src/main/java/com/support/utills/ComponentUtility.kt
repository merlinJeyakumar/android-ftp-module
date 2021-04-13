package com.support.utills

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager


fun Activity.isServiceRunning(serviceClass: Class<*>): Boolean {
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
    val appInstalled: Boolean
    appInstalled = try {
        pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
    return appInstalled
}