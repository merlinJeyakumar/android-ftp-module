package com.raju.native_developers

import android.app.Application
import com.android.installreferrer.api.InstallReferrerClient
import com.raju.data.repositories.NativeDevelopersAppSettingsRepository
import com.raju.native_developers.referral.InstallReferrerPlay

open class NativeDevelopersApplication : Application() {
    private lateinit var installReferrerClient: InstallReferrerClient
    private val appSettingsRepository: NativeDevelopersAppSettingsRepository
        get() = Injection.provideAppDataSource(applicationContext)
    private val TAG: String = "NativeDevelopersApplication"

    override fun onCreate() {
        super.onCreate()

        initReferral()
    }

    private fun initReferral() {
        if (appSettingsRepository.isReferralUpdated()) { //Prevent continuous trigger while app-open
            return
        }
        installReferrerClient = InstallReferrerClient.newBuilder(this).build()
        installReferrerClient.startConnection(
            InstallReferrerPlay(applicationContext) {
                if (it.utm_medium != "organic" || BuildConfig.DEBUG) { //check its direct install
                    appSettingsRepository.setReferralUpdated(true)
                    appSettingsRepository.setReferralModel(it)
                }
            }
        )

        doSampleInput()
    }

    private fun doSampleInput() {
    }
}