package com.raju.native_developers

import android.content.Context
import com.google.gson.Gson
import com.raju.data.repositories.AppSettingsRepository

object Injection {

    fun provideContext(): AppController {
        return AppController.instance
    }

    fun provideAppDataSource(): AppSettingsRepository {
        return AppSettingsRepository.getInstance(provideContext(), Gson())
    }

    fun provideAnalyticsManager(): AnalyticsManager {
        return AnalyticsManager.getInstance(provideContext(), provideAppDataSource())
    }
}