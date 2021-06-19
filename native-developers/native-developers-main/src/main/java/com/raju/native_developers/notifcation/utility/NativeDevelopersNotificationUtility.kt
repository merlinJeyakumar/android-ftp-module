package com.raju.native_developers.notifcation.utility

import com.raju.native_developers.notifcation.NativeDevelopersFCMService
import com.raju.native_developers.notifcation.NotificationModel

internal class NativeDevelopersNotificationUtility {
    fun getNotificationId(notificationName: String): Int {
        return when (notificationName) {
            NativeDevelopersFCMService.CONST_FUNC_OPEN_URL -> {
                NativeDevelopersFCMService.NOTIFICATION_OPEN_URL
            }
            NativeDevelopersFCMService.CONST_FUNC_SHARE_APP -> {
                NativeDevelopersFCMService.NOTIFICATION_SHARE_APP
            }
            else -> {
                NativeDevelopersFCMService.NOTIFICATION_DEFAULT
            }
        }
    }

    fun getNotificationIntent(notificationModel: NotificationModel): Triple<String, String, Int> {
        val reqCode = when (notificationModel.function_mode) {
            NativeDevelopersFCMService.CONST_FUNC_SHARE_APP -> NativeDevelopersFCMService.REQ_ID_FUNC_SHARE_APP
            NativeDevelopersFCMService.CONST_FUNC_OPEN_URL -> NativeDevelopersFCMService.REQ_ID_FUNC_OPEN_URL
            else -> throw NullPointerException("default_function_mode_exception")
        }
        val navKey = when (notificationModel.function_mode) {
            NativeDevelopersFCMService.CONST_FUNC_SHARE_APP -> NativeDevelopersFCMService.CONST_FUNC_SHARE_APP
            NativeDevelopersFCMService.CONST_FUNC_OPEN_URL -> NativeDevelopersFCMService.CONST_FUNC_OPEN_URL
            else -> throw NullPointerException("default_function_mode_exception")
        }
        val navValue = when (notificationModel.function_mode) {
            NativeDevelopersFCMService.CONST_FUNC_SHARE_APP -> ""
            NativeDevelopersFCMService.CONST_FUNC_OPEN_URL -> notificationModel.link_url!!
            else -> throw NullPointerException("default_function_mode_exception")
        }
        return Triple(navKey,navValue,reqCode)
    }
}