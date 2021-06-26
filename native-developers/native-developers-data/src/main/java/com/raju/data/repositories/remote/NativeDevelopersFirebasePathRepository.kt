package com.raju.data.repositories.remote

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.raju.data.repositories.BaseRepository
import com.raju.domain.datasources.remote.IFirebasePathDataSource


class NativeDevelopersFirebasePathRepository(
    private val context: Context
) : BaseRepository(), IFirebasePathDataSource {

    companion object {
        private var INSTANCE: NativeDevelopersFirebasePathRepository? = null

        @JvmStatic
        fun getInstance(
            context: Context
        ): NativeDevelopersFirebasePathRepository {
            if (INSTANCE == null) {
                synchronized(NativeDevelopersFirebasePathRepository::javaClass) {
                    INSTANCE =
                        NativeDevelopersFirebasePathRepository(context)
                }
            }
            return INSTANCE!!
        }

        @VisibleForTesting
        fun clearInstance() {
            INSTANCE = null
        }
    }

    fun getAccountsPath(): String {
        return "/ACCOUNTS"
    }

    fun getUserPath(currentUserId:String): String {
        return "${getAccountsPath()}/USERS/$currentUserId"
    }

    fun getReferralsPath(currentUserId: String): String {
        return "${getUserPath(currentUserId)}/referrals"
    }

    fun getUserReferralsReferredPath(referredBy: String, referralsId:String): String {
        return "${getReferralsPath(referredBy)}/$referralsId"
    }
}
