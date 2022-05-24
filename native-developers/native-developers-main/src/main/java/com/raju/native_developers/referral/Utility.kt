package com.raju.native_developers.referral

import android.content.Context
import com.raju.native_developers.BuildConfig
import com.raju.native_developers.NativeDevelopersInjection
import com.support.inline.orElse
import com.support.utills.Log
import java.net.URLEncoder
import java.util.*

object Utility {
    private val TAG: String = "Utility"

    /*
    * check already updated status, when it false updating it to Firebase Database
    */
    fun checkAndUpdateReferrals(context: Context, firebaseUid: String) {
        val nativeDevelopersAppSettingsRepository = NativeDevelopersInjection.provideAppDataSource(context)
        val nativeDevelopersFirebaseDataRepository =
            NativeDevelopersInjection.provideFirebaseDataRepository(context)

        if (!nativeDevelopersAppSettingsRepository.isReferralUpdated()) {
            nativeDevelopersAppSettingsRepository.getReferralModel()?.let {
                nativeDevelopersFirebaseDataRepository.updateReferralStatus(
                    it.utm_source,
                    firebaseUid
                )?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (!BuildConfig.DEBUG) {
                            nativeDevelopersAppSettingsRepository.setReferralUpdated(true)
                        }
                    }
                }
            }.orElse {
                Log.e(TAG, "Auth State Listener invalid callback")
            }
        }
    }

    /*
    * Encode before
    */
    fun buildReferralUrl(
        context: Context,
        userId: String,
        appName: String,
        referringType: String,
        campaignName: String
    ): String {
        return "https://play.google.com/store/apps/details?id=" +
                context.packageName +
                "&referrer=utm_source=" +
                userId +
                "&utm_medium=" +
                appName +
                "&utm_term=" +
                referringType +
                "&utm_campaign=" +
                campaignName

    }

    fun encodeText(string: String): String {
        return URLEncoder.encode(string.apply {
            replace(" ", "_")
            toLowerCase(Locale.getDefault())
        }, "utf-8")
    }
}