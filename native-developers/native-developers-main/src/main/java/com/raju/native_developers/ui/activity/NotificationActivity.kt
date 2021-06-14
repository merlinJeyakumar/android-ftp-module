package com.raju.native_developers.ui.activity

import android.os.Bundle
import com.raju.native_developers.Injection
import com.raju.native_developers.notifcation.NativeDevelopersFCMService
import com.raju.native_developers.notifcation.NativeDevelopersFCMService.Companion.CONST_NOTIFICATION_TYPE
import com.support.supportBaseClass.BaseActivity
import com.support.utills.Log
import com.support.utills.openURL
import com.support.utills.shareLink

class NotificationActivity : BaseActivity() {
    private val TAG = NotificationActivity::class.java.simpleName

    private val analyticsManager by lazy { Injection.provideAnalyticsManager(this@NotificationActivity) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initIntent()
    }

    private fun initIntent() {
        intent.getStringExtra(CONST_NOTIFICATION_TYPE)?.let {
            analyticsManager.analyticsWhenNotificationClicked(it)
        }
        Log.e(TAG, "initIntent ${intent.dataString}")

        intent?.getStringExtra(NativeDevelopersFCMService.CONST_OPEN_URL)?.let {
            openURL(this@NotificationActivity, it)
            finish()
        }

        intent?.getStringExtra(NativeDevelopersFCMService.CONST_SHARE_APP)?.let {
            shareLink(this,"title","body_text","hope you like this app")
            finish()
        }

    }
}