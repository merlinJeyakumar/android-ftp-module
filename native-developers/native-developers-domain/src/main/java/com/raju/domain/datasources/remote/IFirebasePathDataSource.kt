package com.raju.domain.datasources.remote


interface IFirebasePathDataSource {
    fun getAccountsPath(): String
    fun getUserPath(currentUserId: String): String
    fun getReferralsPath(currentUserId: String): String
    fun getUserReferralsReferredPath(referredBy: String, referralsId: String): String
}