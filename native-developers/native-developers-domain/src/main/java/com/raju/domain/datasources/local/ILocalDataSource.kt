package com.raju.domain.datasources.local

import androidx.lifecycle.LiveData
import com.raju.domain.entity.QuickTextEntity

interface ILocalDataSource {
    fun getLiveQuickText(): LiveData<List<QuickTextEntity>>
}