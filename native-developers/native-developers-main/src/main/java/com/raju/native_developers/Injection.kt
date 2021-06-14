package com.raju.native_developers

import android.content.Context
import com.google.gson.Gson
import com.raju.data.repositories.AppSettingsRepository

object Injection {
    fun provideAppDataSource(context:Context): AppSettingsRepository {
        return AppSettingsRepository.getInstance(context, Gson())
    }

    fun provideAnalyticsManager(context:Context): AnalyticsManager {
        return AnalyticsManager.getInstance(context, provideAppDataSource(context))
    }
}