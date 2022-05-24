package com.raju.data.repositories.remote

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.raju.data.repositories.BaseRepository
import com.raju.domain.datasources.remote.IFirebaseModelSource
import com.raju.domain.datasources.remote.IFirebasePathDataSource
import com.raju.domain.models.ReferralModel


class NativeDevelopersFirebaseModelRepository(
    private val context: Context,
    private val firebasePathRepository: NativeDevelopersFirebasePathRepository
) : BaseRepository(), IFirebaseModelSource {

    companion object {
        private var INSTANCE: NativeDevelopersFirebaseModelRepository? = null

        @JvmStatic
        fun getInstance(
            context: Context,
            firebasePathRepository: NativeDevelopersFirebasePathRepository
        ): NativeDevelopersFirebaseModelRepository {
            if (INSTANCE == null) {
                synchronized(NativeDevelopersFirebaseModelRepository::javaClass) {
                    INSTANCE =
                        NativeDevelopersFirebaseModelRepository(context,firebasePathRepository)
                }
            }
            return INSTANCE!!
        }

        @VisibleForTesting
        fun clearInstance() {
            INSTANCE = null
        }
    }
}
