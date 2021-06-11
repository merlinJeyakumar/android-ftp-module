package com.raju.data.repositories

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.raju.domain.datasources.IAppSettingsDataSource
import com.raju.domain.models.DeveloperModel
import com.support.inline.orElse
import com.support.shared_pref.BaseLiveSharedPreferences
import com.support.shared_pref.Prefs


class AppSettingsRepository(
    private val applicationContext: Context,
    private val plainGson: Gson
) : IAppSettingsDataSource {

    private var liveSharedPreferences: BaseLiveSharedPreferences
    private val SP_NAME = "prefs_myapp"

    companion object {

        private var INSTANCE: AppSettingsRepository? = null

        @JvmStatic
        fun getInstance(applicationContext: Context, plainGson: Gson): AppSettingsRepository {
            if (INSTANCE == null) {
                synchronized(AppSettingsRepository::javaClass) {
                    INSTANCE = AppSettingsRepository(applicationContext, plainGson)
                }
            }
            return INSTANCE!!
        }

        @VisibleForTesting
        fun clearInstance() {
            INSTANCE = null
        }

        private const val PREFS_USERNAME = "PREFS_USERNAME"
        private const val PREFS_PASSWORD = "PREFS_PASSWORD"
        private const val PREFS_IS_AUTHENTICATED = "PREFS_IS_AUTHENTICATED"
        private const val PREFS_PORT = "PREFS_PORT"
        private const val PREFS_DEVELOPER_MODEL = "PREFS_DEVELOPER_MODEL"
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
            return plainGson.fromJson<DeveloperModel>(it, DeveloperModel::class.java)
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

    }
}