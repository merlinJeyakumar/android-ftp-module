package com.raju.native_developers.authentication

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.VisibleForTesting
import com.google.firebase.auth.FirebaseAuth
import com.raju.data.repositories.NativeDevelopersAppSettingsRepository

@SuppressLint("MissingPermission")
class NativeDevelopersAuthenticationManager(
    private val context: Context,
    private val appSettingsRepository: NativeDevelopersAppSettingsRepository
) {
    private val firebaseAuth by lazy { FirebaseAuth.getInstance() }


    companion object {
        private var INSTANCE: NativeDevelopersAuthenticationManager? = null

        @JvmStatic
        fun getInstance(
            context: Context,
            appSettingsRepository: NativeDevelopersAppSettingsRepository
        ): NativeDevelopersAuthenticationManager {
            if (INSTANCE == null) {
                synchronized(NativeDevelopersAuthenticationManager::javaClass) {
                    INSTANCE = NativeDevelopersAuthenticationManager(context, appSettingsRepository)
                }
            }
            return INSTANCE!!
        }

        @VisibleForTesting
        fun clearInstance() {
            INSTANCE = null
        }
    }

    fun reload(block: (successful: Boolean) -> Unit) {
        if (isSignedIn()) {
            firebaseAuth.currentUser?.reload()?.addOnCompleteListener {
                block(it.isSuccessful)
            }
        }else{
            block(false)
        }
    }

    fun signInAnonymously(block: (successful: Boolean) -> Unit) {
        firebaseAuth.signInAnonymously().addOnCompleteListener {
            block(it.isSuccessful)
        }
    }

    fun isSignedIn(): Boolean {
        return firebaseAuth.currentUser != null
    }

    fun isAnonymousUser(): Boolean {
        return if (isSignedIn()) {
            firebaseAuth.currentUser?.isAnonymous!!
        } else {
            false
        }
    }

    fun isDisabled() {
        firebaseAuth.currentUser?.isAnonymous
    }
}