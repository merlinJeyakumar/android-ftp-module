package com.support.utills

import android.app.Activity
import android.app.ActivityManager
import android.content.Context


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