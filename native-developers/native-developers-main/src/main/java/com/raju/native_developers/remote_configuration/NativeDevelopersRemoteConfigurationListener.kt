package com.raju.native_developers.remote_configuration

import android.content.Context
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.raju.data.repositories.NativeDevelopersAppSettingsRepository
import com.raju.domain.models.DeveloperModel
import com.raju.native_developers.R
import com.support.utills.Log

abstract class NativeDevelopersRemoteConfigurationListener(
    private val context: Context,
    private val firebaseRemoteConfig: FirebaseRemoteConfig,
    private val nativeDevelopersAppSettingsRepository: NativeDevelopersAppSettingsRepository
) : OnCompleteListener<Boolean> {
    private val TAG: String = this::class.java.simpleName

    override fun onComplete(task: Task<Boolean>) {
        if (task.isSuccessful) {
            handleStationConfiguration()
        } else {
            Log.e(TAG, task.exception?.localizedMessage)
            onConfigurationReceived(
                false,
                firebaseRemoteConfig,
                null,
                null
            )
        }
    }

    private fun handleStationConfiguration() {
        val developerName =
            firebaseRemoteConfig.getString(context.getString(R.string.remote_dev_developer_name))
        val developerEmail =
            firebaseRemoteConfig.getString(context.getString(R.string.remote_dev_email_address))
        val developerLink =
            firebaseRemoteConfig.getString(context.getString(R.string.remote_dev_profile_link))
        val developerPhone =
            firebaseRemoteConfig.getString(context.getString(R.string.remote_dev_mobile_number))
        val organisationName =
            firebaseRemoteConfig.getString(context.getString(R.string.remote_dev_organisation_name))
        val publisherLink =
            firebaseRemoteConfig.getString(context.getString(R.string.remote_dev_publisher_link))
        val developerModel = DeveloperModel(
            developerName = developerName,
            developerEmail = developerEmail,
            developerLink = developerLink,
            developerPhone = developerPhone,
            organisationName = organisationName,
            publisherLink = publisherLink
        )

        saveConfiguration()
        onConfigurationReceived(
            true,
            firebaseRemoteConfig,
            developerModel,
            null
        )
    }

    private fun saveConfiguration() {
        nativeDevelopersAppSettingsRepository.setDynamicLink(
            dynamicUrl = firebaseRemoteConfig.getString(
                context.getString(R.string.remote_dev_dynamic_link)
            )
        )
    }

    abstract fun onConfigurationReceived(
        isSuccess: Boolean,
        firebaseRemoteConfig: FirebaseRemoteConfig,
        developerModel: DeveloperModel?,
        exception: Exception?
    )
}