package com.raju.native_developers.notifcation

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.raju.native_developers.notifcation.utility.NativeDevelopersNotificationUtility
import com.raju.native_developers.ui.activity.NotificationActivity
import com.support.utills.toJson
import io.karn.notify.Notify
import io.karn.notify.NotifyCreator
import io.karn.notify.entities.Payload
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

/**
 * developed by merlin-jeyakumar at 14June2021
 * handle notification by it properties
 * https://merlinjeyakumar.atlassian.net/wiki/spaces/JS/pages/30900236/Notification-Global-Channel+Native+Developers
 **/
abstract class NativeDevelopersFCMService : FirebaseMessagingService() {

    private lateinit var notify: NotifyCreator
    private val TAG = javaClass.simpleName

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.e(TAG, "onMessageReceived ${remoteMessage.data}")

        val notificationModel = Gson().fromJson<NotificationModel>(remoteMessage.data.toJson(), NotificationModel::class.java)
        notify = Notify.with(this).header {
            this.icon = getAppIcon()
        }
        if (notificationModel.function_mode == CONST_FUNC_SHARE_APP
                || notificationModel.function_mode == CONST_FUNC_OPEN_URL
        ) {
            val keyValueCode =
                    NativeDevelopersNotificationUtility().getNotificationIntent(notificationModel)

            setNotification(
                    getPendingIntent(reqCode = keyValueCode.third,
                            intent = Intent(
                                    this@NativeDevelopersFCMService,
                                    NotificationActivity::class.java
                            ).apply {
                                putExtra(keyValueCode.first, keyValueCode.second)
                                putExtra(CONST_NOTIFICATION_TYPE, keyValueCode.first)
                            })
                    ,
                    if (!notificationModel.image_url.isNullOrEmpty()) {
                        getPictureNotification(body = notificationModel.body,
                                imageUrl = notificationModel.image_url)
                    } else {
                        notificationModel.let {
                            getTextNotification(
                                    it.title,
                                    it.body,
                                    it.body
                            )
                        }
                    }
            )
            showNotification(NativeDevelopersNotificationUtility().getNotificationId(keyValueCode.first))
        } else {
            functionalEvent(notificationModel)
        }
    }

    fun getPendingIntent(
            reqCode: Int,
            intent: Intent,
            flags: Int = 0
    ): PendingIntent {
        return PendingIntent.getActivity(
                this@NativeDevelopersFCMService,
                reqCode,
                intent,
                flags
        )
    }


    fun setNotification(
            pendingIntent: PendingIntent? = null,
            notifyCreator: NotifyCreator
    ) {
        notify.meta {
            clickIntent = pendingIntent
            notifyCreator
        }
    }

    abstract fun functionalEvent(notificationModel: NotificationModel)

    fun getTextNotification(
            title: String,
            body: String,
            expandedBodyText: String
    ): NotifyCreator {
        return notify.asBigText {
            this.title = title
            this.text = body
            this.expandedText = expandedBodyText
            this.bigText = ""
        }
    }

    fun getPictureNotification(body: String, imageUrl: String): NotifyCreator {
        return notify.asBigPicture {
            this.expandedText = body
            this.image = bitmapFromUrl(imageUrl);
        }
    }

    fun showNotification(notificationId: Int) {
        notify.show(notificationId)
    }

    private fun bitmapFromUrl(imageUrl: String?): Bitmap? {
        Log.i(TAG, "bitmapFromUrl: imageUrl $imageUrl")
        return try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    abstract fun getAppIcon(): Int //R.mipmap.ic_launcher

    fun getNotificationClickIntent(
            reqCode: Int,
            intent: Intent,
            flags: Int = 0
    ): NotifyCreator {
        return notify.meta {
            clickIntent = PendingIntent.getActivity(
                    this@NativeDevelopersFCMService,
                    reqCode,
                    intent,
                    flags
            )
        }
    }

    companion object {
        const val CONST_NOTIFICATION_TYPE = "NOTIFICATION_TYPE"

        //Notification Code
        const val NOTIFICATION_DEFAULT = 1000
        const val NOTIFICATION_OPEN_URL = 1001
        const val NOTIFICATION_SHARE_APP = 1003

        //RequestCode
        const val REQ_ID_FUNC_OPEN_URL = 1001
        const val REQ_ID_FUNC_SHARE_APP = 1002

        //Navigation Intent Name
        const val CONST_FUNC_OPEN_URL = "open_url"
        const val CONST_FUNC_SHARE_APP = "share_app"
    }
}


/*
* https://fcm.googleapis.com/fcm/send
*
* {
  "to": "/topics/global_development-debug",
  "data": {
    "notification": "global",
    "body": "Lorem Ipsum is simply dummy...,",
    "link_url":"null",
    "image_url":"null",
    "function_mode":"start_praising",
    "priority": "high",
    "title": "Portugal vs. Denmark"
  }
}
*
*
* FirebaseMessaging.getInstance().subscribeToTopic("global_${BuildConfig.FLAVOR}-${BuildConfig.BUILD_TYPE}").addOnSuccessListener {} //global_development_debug
* */