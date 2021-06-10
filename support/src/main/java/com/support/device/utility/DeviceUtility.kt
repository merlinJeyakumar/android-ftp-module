package com.support.device.utility

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build

object DeviceUtility {
    fun sendSms(context: Context, to: String, body: String) {
        val sendIntent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:"))
        sendIntent.putExtra("sms_body", body)

        sendIntent.putExtra("address", to)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            sendIntent.type = "vnd.android-dir/mms-sms"
        } else {
            Uri.parse("smsto: " + to)
        }
        context.startActivity(sendIntent)
    }
}