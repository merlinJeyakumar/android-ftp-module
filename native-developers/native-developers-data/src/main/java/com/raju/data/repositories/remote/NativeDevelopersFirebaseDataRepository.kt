package com.raju.data.repositories.remote

import android.content.Context
import android.net.Uri
import androidx.annotation.VisibleForTesting
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ShortDynamicLink
import com.raju.data.R
import com.raju.data.repositories.NativeDevelopersAppSettingsRepository
import com.raju.data.repositories.BaseRepository
import com.raju.domain.datasources.remote.IFirebaseDataSource
import com.support.inline.orElse


class NativeDevelopersFirebaseDataRepository(
    private val context: Context,
    private val firebasePathRepository: NativeDevelopersFirebasePathRepository,
    private val firebaseModelRepository: NativeDevelopersFirebaseModelRepository,
    private val appSettingsRepository: NativeDevelopersAppSettingsRepository
) : BaseRepository(), IFirebaseDataSource {

    companion object {
        private var INSTANCE: NativeDevelopersFirebaseDataRepository? = null

        @JvmStatic
        fun getInstance(
            context: Context,
            firebasePathRepository: NativeDevelopersFirebasePathRepository,
            firebaseModelRepository: NativeDevelopersFirebaseModelRepository,
            appSettingsRepository: NativeDevelopersAppSettingsRepository
        ): NativeDevelopersFirebaseDataRepository {
            if (INSTANCE == null) {
                synchronized(NativeDevelopersFirebaseDataRepository::javaClass) {
                    INSTANCE =
                        NativeDevelopersFirebaseDataRepository(
                            context,
                            firebasePathRepository,
                            firebaseModelRepository,
                            appSettingsRepository
                        )
                }
            }
            return INSTANCE!!
        }

        @VisibleForTesting
        fun clearInstance() {
            INSTANCE = null
        }
    }

    fun updateReferralStatus(
        referredBy: String,
        referralsId: String
    ): Task<Void>? {
        return appSettingsRepository.getReferralModel()?.let {
            return FirebaseDatabase.getInstance()
                .getReference(
                    firebasePathRepository.getUserReferralsReferredPath(
                        referredBy,
                        referralsId
                    )
                )
                .setValue(it)
        }.orElse {
            null
        }
    }

    fun getDynamicLink(url: String): Task<ShortDynamicLink>? {
        appSettingsRepository.getDynamicLink()?.let { dynamicLink: String ->
            return FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(url))
                .setDomainUriPrefix(dynamicLink)
                .setAndroidParameters(
                    DynamicLink.AndroidParameters.Builder(context.packageName)
                        .setMinimumVersion(1)
                        .build()
                ).buildShortDynamicLink(R.string.app_name)
        }.orElse {
            return null
        }
    }
}
