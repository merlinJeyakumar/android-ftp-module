package com.raju.data.repositories

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.raju.domain.datasources.IAppSettingsDataSource
import com.raju.domain.models.DeveloperModel
import com.raju.domain.models.ReferralModel
import com.support.inline.orElse
import com.support.shared_pref.BaseLiveSharedPreferences
import com.support.shared_pref.Prefs


class NativeDevelopersAppSettingsRepository(
    private val applicationContext: Context,
    private val plainGson: Gson
) : IAppSettingsDataSource {

    private var liveSharedPreferences: BaseLiveSharedPreferences
    private val SP_NAME = "native_developers"

    companion object {

        private var INSTANCE: NativeDevelopersAppSettingsRepository? = null

        @JvmStatic
        fun getInstance(applicationContext: Context, plainGson: Gson): NativeDevelopersAppSettingsRepository {
            if (INSTANCE == null) {
                synchronized(NativeDevelopersAppSettingsRepository::javaClass) {
                    INSTANCE = NativeDevelopersAppSettingsRepository(applicationContext, plainGson)
                }
            }
            return INSTANCE!!
        }

        @VisibleForTesting
        fun clearInstance() {
            INSTANCE = null
        }

        private const val PREFS_FCM_TOKEN = "PREFS_FCM_TOKEN"
        private const val PREFS_REFERRAL_MODEL = "PREFS_REFERRAL_MODEL"
        private const val PREFS_DEVELOPER_MODEL = "PREFS_DEVELOPER_MODEL"
        private const val PREFS_IS_REFERRAL_CHECKED = "PREFS_IS_REFERRAL_CHECKED"
    }

    init {
        Prefs.Builder()
            .setContext(applicationContext)
            .setMode(Context.MODE_PRIVATE)
            .setPrefsName(SP_NAME)
            .build()

        liveSharedPreferences = BaseLiveSharedPreferences(Prefs.getPreferences())
    }



    override fun getDeveloperModel(): DeveloperModel {
        Prefs.getString(PREFS_DEVELOPER_MODEL, null)?.let {
            return plainGson.fromJson(it, DeveloperModel::class.java)
        }.orElse {
            return DeveloperModel(
                "Jeyakumar - Developer",
                "https://in.linkedin.com/in/merlin-jeyakumar",
                "+919042886538",
                "nativedevps@gmail.com",
                "https://play.google.com/store/apps/developer?id=Native+Developers"
            )
        }
    }

    override fun putDeveloperModel(developerModel: DeveloperModel) {
        Prefs.putString(PREFS_DEVELOPER_MODEL, plainGson.toJson(developerModel))
    }

    override fun setFcmToken(token: String) {
        Prefs.putString(PREFS_FCM_TOKEN,token)
    }

    fun getFcmToken(): String? {
        return Prefs.getString(PREFS_FCM_TOKEN,null)
    }

    fun setReferralModel(referralModel: ReferralModel){
        return Prefs.putString(PREFS_REFERRAL_MODEL, plainGson.toJson(referralModel))
    }

    fun getReferralModel(): ReferralModel? {
        return Prefs.getString(PREFS_REFERRAL_MODEL,null)?.let {
            plainGson.fromJson(it,ReferralModel::class.java)
        }.orElse {
            null
        }
    }

    fun isReferralUpdated(): Boolean {
        return Prefs.getBoolean(PREFS_IS_REFERRAL_CHECKED,false)
    }

    fun setReferralUpdated(boolean: Boolean) {
        Prefs.putBoolean(PREFS_IS_REFERRAL_CHECKED,boolean)
    }
}