package com.raju.native_developers

import android.app.Application
import com.android.installreferrer.api.InstallReferrerClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.raju.data.repositories.NativeDevelopersAppSettingsRepository
import com.raju.data.repositories.remote.NativeDevelopersFirebaseDataRepository
import com.raju.native_developers.referral.InstallReferrerPlay
import com.raju.native_developers.referral.Utility.checkAndUpdateReferrals
import com.support.utills.Log

open class NativeDevelopersApplication : Application() {
    private lateinit var installReferrerClient: InstallReferrerClient
    private val nativeDevelopersAppSettingsRepository: NativeDevelopersAppSettingsRepository
        get() = NativeDevelopersInjection.provideAppDataSource(applicationContext)
    private val developersFirebaseDataRepository: NativeDevelopersFirebaseDataRepository
        get() = NativeDevelopersInjection.provideFirebaseDataRepository(applicationContext)
    open val uid: String?
        get() = FirebaseAuth.getInstance().uid
    private val TAG: String = "NativeDevelopersApplication"

    override fun onCreate() {
        super.onCreate()

        initReferral()
        initAuthListener()
    }

    /*
    * Authentication State listener
    * listen and check logged status and update referral data to firebase
    */
    private fun initAuthListener() {
        FirebaseAuth.getInstance().addAuthStateListener {
            Log.e(TAG, "Auth State Changed ${it.currentUser?.uid}")
            it.currentUser?.let { firebaseUser: FirebaseUser ->
                checkAndUpdateReferrals(
                    applicationContext,
                    firebaseUser.uid
                )
            }
        }
    }

    private fun initReferral() {
        if (nativeDevelopersAppSettingsRepository.isReferralUpdated()) { //Prevent continuous trigger while app-open
            return
        }
        installReferrerClient = InstallReferrerClient.newBuilder(this).build()
        installReferrerClient.startConnection(
            InstallReferrerPlay(applicationContext) {
                if (it.utm_medium != "organic") { //check its direct install

                }

                nativeDevelopersAppSettingsRepository.setReferralUpdated(false)
                nativeDevelopersAppSettingsRepository.setReferralModel(it)
                uid?.let {
                    checkAndUpdateReferrals(
                        applicationContext,
                        it
                    )
                }
            }
        )
    }
}