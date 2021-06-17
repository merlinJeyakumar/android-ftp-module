package com.raju.native_developers.notifcation.utility

import com.raju.native_developers.notifcation.NativeDevelopersFCMService

internal class NativeDevelopersNotificationUtility {
    fun getNotificationId(notificationName: String): Int {
        return when (notificationName) {
            NativeDevelopersFCMService.CONST_OPEN_URL -> {
                NativeDevelopersFCMService.NOTIFICATION_OPEN_URL
            }
            NativeDevelopersFCMService.CONST_SHARE_APP -> {
                NativeDevelopersFCMService.NOTIFICATION_SHARE_APP
            }
            else -> {
                NativeDevelopersFCMService.NOTIFICATION_DEFAULT
            }
        }
    }
}