package com.raju.native_developers

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class AppController : Application() {
    private val TAG = javaClass.simpleName

    companion object {
        lateinit var instance: AppController
    }

    override fun attachBaseContext(context: Context) {
        super.attachBaseContext(context)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    override fun onCreate() {
        super.onCreate()
        initialize()
    }

    private fun initialize() {
        instance = this
    }
}