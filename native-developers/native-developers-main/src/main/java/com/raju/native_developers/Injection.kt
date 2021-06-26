package com.raju.native_developers

import android.content.Context
import com.google.gson.Gson
import com.raju.data.repositories.NativeDevelopersAppSettingsRepository
import com.raju.data.repositories.remote.NativeDevelopersFirebaseDataRepository
import com.raju.data.repositories.remote.NativeDevelopersFirebaseModelRepository
import com.raju.data.repositories.remote.NativeDevelopersFirebasePathRepository
import com.raju.native_developers.analytics.NativeDevelopersAnalyticsManager
import com.raju.native_developers.authentication.NativeDevelopersAuthenticationManager

object Injection {
    fun provideAppDataSource(context:Context): NativeDevelopersAppSettingsRepository {
        return NativeDevelopersAppSettingsRepository.getInstance(context, Gson())
    }

    fun provideAnalyticsManager(context:Context): NativeDevelopersAnalyticsManager {
        return NativeDevelopersAnalyticsManager.getInstance(context, provideAppDataSource(context))
    }

    fun provideAuthenticationManager(context:Context): NativeDevelopersAuthenticationManager {
        return NativeDevelopersAuthenticationManager.getInstance(context, provideAppDataSource(context))
    }

    fun provideFirebaseModelRepository(context: Context): NativeDevelopersFirebaseModelRepository {
        return NativeDevelopersFirebaseModelRepository.getInstance(
            context,
            provideFirebasePathRepository(context)
        )
    }

    fun provideFirebasePathRepository(context: Context): NativeDevelopersFirebasePathRepository {
        return NativeDevelopersFirebasePathRepository.getInstance(context)
    }

    fun provideFirebaseDataRepository(context: Context): NativeDevelopersFirebaseDataRepository {
        return NativeDevelopersFirebaseDataRepository.getInstance(
            context,
            provideFirebasePathRepository(context),
            provideFirebaseModelRepository(context),
            provideAppDataSource(context)
        )
    }
}