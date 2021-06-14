package com.raju.domain.datasources

import com.raju.domain.models.DeveloperModel


interface IAppSettingsDataSource {
    fun getDeveloperModel(): DeveloperModel
    fun putDeveloperModel(developerModel: DeveloperModel)
    fun setFcmToken(token: String)
}