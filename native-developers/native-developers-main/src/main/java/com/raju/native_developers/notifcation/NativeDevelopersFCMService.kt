package com.raju.native_developers.notifcation

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.raju.native_developers.Injection
import com.raju.native_developers.notifcation.utility.NativeDevelopersNotificationUtility
import com.raju.native_developers.ui.activity.NotificationActivity
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

        val notificationModel = NotificationModel().apply {
            this.notification = remoteMessage.data["notification"]!!
            this.body = remoteMessage.data["body"]!!
            this.title = remoteMessage.data["title"]!!
            this.imageUrl =
                remoteMessage.data["image_url"]!!.let { imageUrl -> if (imageUrl != "null") imageUrl else null }
            this.linkUrl =
                remoteMessage.data["link_url"]!!.let { linkUrl -> if (linkUrl != "null") linkUrl else null }
            this.functionMode =
                remoteMessage.data["function_mode"]!!.let { linkUrl -> if (linkUrl != "null") linkUrl else "" }
        }

        notify = Notify.with(this).header {
            this.icon = getAppIcon()
        }
        if (notificationModel.linkUrl != null
            || notificationModel.functionMode == CONST_SHARE_APP
        ) {
            val reqCode = when (notificationModel.functionMode) {
                CONST_SHARE_APP -> REQ_SHARE_APP
                else -> REQ_OPEN_URL
            }

            val navKey = when (notificationModel.functionMode) {
                CONST_SHARE_APP -> CONST_SHARE_APP
                else -> CONST_OPEN_URL
            }

            val navValue = when (notificationModel.functionMode) {
                CONST_SHARE_APP -> ""
                else -> notificationModel.linkUrl!!
            }
            showNotification {
                clickIntent = PendingIntent.getActivity(
                    this@NativeDevelopersFCMService,
                    reqCode,
                    Intent(
                        this@NativeDevelopersFCMService,
                        NotificationActivity::class.java
                    ).apply {
                        putExtra(navKey, navValue)
                        putExtra(CONST_NOTIFICATION_TYPE, navKey)
                    },
                    0
                )
            }

            if ((notificationModel.imageUrl != null)) {
                notify.asBigPicture {
                    this.expandedText = remoteMessage.data["body"]
                    this.image = bitmapFromUrl(notificationModel.imageUrl);
                }
            } else {
                notify.asBigText {
                    this.title = remoteMessage.data["title"]
                    this.text = remoteMessage.data["body"]
                    this.expandedText = remoteMessage.data["body"]
                    this.bigText = ""
                }
            }
            notify.show(NativeDevelopersNotificationUtility().getNotificationId(navKey))
        } else {
            functionalEvent(notificationModel)
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

    fun showNotification(meta: Payload.Meta.() -> Unit) {
        notify.meta { meta }
        notify.show()
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
            clickIntent = PendingIntent.getActivity(this@NativeDevelopersFCMService,
                    reqCode,
                    intent,
                    flags)
        }
    }

    companion object {
        const val CONST_NOTIFICATION_TYPE = "NOTIFICATION_TYPE"


        //Notification Code
        const val NOTIFICATION_DEFAULT = 1000
        const val NOTIFICATION_OPEN_URL = 1001
        const val NOTIFICATION_SHARE_APP = 1003

        //RequestCode
        const val REQ_OPEN_URL = 1001
        const val REQ_SHARE_APP = 1002

        const val CONST_OPEN_URL = "open_url"
        const val CONST_SHARE_APP = "share_app"
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
    "content_available": true,
    "priority": "high",
    "title": "Portugal vs. Denmark"
  }
}
*
*
* FirebaseMessaging.getInstance().subscribeToTopic("global_${BuildConfig.FLAVOR}-${BuildConfig.BUILD_TYPE}").addOnSuccessListener {} //global_development_debug
* */