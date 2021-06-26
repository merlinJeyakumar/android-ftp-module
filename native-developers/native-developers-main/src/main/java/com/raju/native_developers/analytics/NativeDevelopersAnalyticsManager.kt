package com.raju.native_developers.analytics

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.google.firebase.analytics.FirebaseAnalytics
import com.raju.data.repositories.NativeDevelopersAppSettingsRepository
import com.raju.native_developers.authentication.NativeDevelopersAuthenticationManager

@SuppressLint("MissingPermission")
class NativeDevelopersAnalyticsManager(
        private val context: Context,
        private val appSettingsRepository: NativeDevelopersAppSettingsRepository
) {
    private val firebaseAnalyticsManager by lazy { FirebaseAnalytics.getInstance(context) }
    private val analyticsItemNames = AnalyticsItemNames()
    private val analyticsContentType = AnalyticsItemTypes()


    companion object {
        val CONST_USER_ID = "USER_ID"
        private var INSTANCE: NativeDevelopersAnalyticsManager? = null

        @JvmStatic
        fun getInstance(context: Context, appSettingsRepository: NativeDevelopersAppSettingsRepository): NativeDevelopersAnalyticsManager {
            if (INSTANCE == null) {
                synchronized(NativeDevelopersAuthenticationManager::javaClass) {
                    INSTANCE = NativeDevelopersAnalyticsManager(context, appSettingsRepository)
                }
            }
            return INSTANCE!!
        }

        @VisibleForTesting
        fun clearInstance() {
            INSTANCE = null
        }
    }

    @JvmName("getFirebaseAnalyticsManager1")
    fun getFirebaseAnalyticsManager(): FirebaseAnalytics {
        return firebaseAnalyticsManager
    }

    /*Rating-Dialog*/
    fun analyticsWhenRateLaterPressed() {
        val map = mutableMapOf<String, Any>()
        map[analyticsItemNames.CONST_WHEN_RATE_LATER_PRESSED] = true
        sendAnalytics(analyticsContentType.CONST_RATING_DIALOG, map)
    }

    fun analyticsWhenRateNowPressed() {
        val map = mutableMapOf<String, Any>()
        map[analyticsItemNames.CONST_WHEN_RATE_NOW_PRESSED] = true
        sendAnalytics(analyticsContentType.CONST_RATING_DIALOG, map)
    }

    fun analyticsWhenRatingDragged(rateLevel: String) {
        val map = mutableMapOf<String, Any>()
        map[analyticsItemNames.CONST_WHEN_RATING_DRAGGED] = rateLevel
        sendAnalytics(analyticsContentType.CONST_RATING_DIALOG, map)
    }

    fun analyticsShareMenuPressed() {
        val map = mutableMapOf<String, Any>()
        map[analyticsItemNames.CONST_WHEN_SHARE_MENU_PRESSED] = true
        sendAnalytics(analyticsContentType.CONST_TOOLBAR, map)
    }

    fun analyticsRateMenuPressed() {
        val map = mutableMapOf<String, Any>()
        map[analyticsItemNames.CONST_WHEN_RATE_MENU_PRESSED] = true
        sendAnalytics(analyticsContentType.CONST_TOOLBAR, map)
    }

    fun analyticsAboutMenuPressed() {
        val map = mutableMapOf<String, Any>()
        map[analyticsItemNames.CONST_WHEN_ABOUT_MENU_PRESSED] = true
        sendAnalytics(analyticsContentType.CONST_TOOLBAR, map)
    }

    /*About-Dialog*/
    fun analyticsWhenAboutProfilePressed() {
        val map = mutableMapOf<String, Any>()
        map[analyticsItemNames.CONST_ABOUT_WHEN_PROFILE_PRESSED] = true
        sendAnalytics(analyticsContentType.CONST_ABOUT_DIALOG, map)
    }

    fun analyticsWhenAboutPlayStoreProfilePressed() {
        val map = mutableMapOf<String, Any>()
        map[analyticsItemNames.CONST_PLAYSTORE_PROFILE_PRESSED] = true
        sendAnalytics(analyticsContentType.CONST_ABOUT_DIALOG, map)
    }

    fun analyticsWhenAboutRequestPressed() {
        val map = mutableMapOf<String, Any>()
        map[analyticsItemNames.CONST_ABOUT_DIALOG_REQUEST_PRESSED] = true
        sendAnalytics(analyticsContentType.CONST_ABOUT_DIALOG, map)
    }

    fun analyticsWhenNotificationClicked(string:String) {
        val map = mutableMapOf<String, Any>()
        map[analyticsItemNames.CONST_NOTIFCATION_PRESSED] = string
        sendAnalytics(analyticsContentType.CONST_NOTIFICATION, map)
    }

    /*Request-Screen*/
    fun analyticsWhenRequestSubmitted(mode:Int) {
        val map = mutableMapOf<String, Any>()
        map[analyticsItemNames.CONST_WHEN_REQUEST_SUBMITTED] = mode
        sendAnalytics(analyticsContentType.CONST_REQUEST, map)
    }

    private fun sendAnalytics(event: String, map: Map<String, Any>) {
        val bundle = Bundle()
        for (entry in map) {
            bundle.putString(entry.key, entry.value.toString())
        }
        firebaseAnalyticsManager.logEvent(event, bundle)
    }
}