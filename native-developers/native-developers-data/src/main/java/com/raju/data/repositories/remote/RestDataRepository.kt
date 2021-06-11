package com.raju.data.repositories.remote

import androidx.annotation.VisibleForTesting
import com.raju.data.repositories.BaseRepository
import com.raju.data.webservices.IService
import com.raju.domain.datasources.remote.IRestDataSource


class RestDataRepository(
    private val service: IService
) : BaseRepository(), IRestDataSource {

    companion object {
        private var INSTANCE: RestDataRepository? = null

        @JvmStatic
        fun getInstance(
            service: IService
        ): RestDataRepository {
            if (INSTANCE == null) {
                synchronized(RestDataRepository::javaClass) {
                    INSTANCE =
                        RestDataRepository(service)
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
