package com.nativedevps.ftp

import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import com.nativedevps.ftp.model.FtpUrlModel
import com.support.device.connection.WiFiReceiverManager
import com.support.inline.orElse
import com.support.utills.file.getMimeTypeExtension
import org.apache.commons.net.ftp.FTPClient
import java.io.File
import java.io.InputStream
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method


fun getWiFiIpAddress(context: Context): String {
    return WiFiReceiverManager.getInstance(context).getLocalIpAddress()
}

@Throws(InvocationTargetException::class, IllegalAccessException::class)
fun wifiHotspotEnabled(context: Context): Boolean {
    val manager =
        context.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
    var method: Method? = null
    try {
        method = manager.javaClass.getDeclaredMethod("isWifiApEnabled")
    } catch (e: NoSuchMethodException) {
        e.printStackTrace()
    }
    method!!.isAccessible = true //in the case of visibility change in future APIs
    return method.invoke(manager) as Boolean
}

fun checkWifiOnAndConnected(context: Context): Boolean {
    val wifiMgr =
        (context.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager)
    return if (wifiMgr.isWifiEnabled) { // Wi-Fi adapter is ON
        val wifiInfo = wifiMgr.connectionInfo
        wifiInfo.networkId != -1 || getWiFiIpAddress(context).isNotEmpty()
    } else {
        false // Wi-Fi adapter is OFF
    }
}

suspend fun decodeUriBitmapToSize(
    context: Context,
    uri: Uri,
    maxImageSize: Int,
): Bitmap? {
    return context.contentResolver.openInputStream(uri)?.use { inputStream ->
        decodeBitmapToSize(inputStream, maxImageSize)
    }.orElse {
        null
    }
}

suspend fun decodeFileBitmapToSize(
    file: File,
    maxImageSize: Int,
) {
    file.inputStream().use { inputStream ->
        decodeBitmapToSize(inputStream, maxImageSize)
    }.orElse {
        null
    }
}

fun decodeBitmapToSize(
    fis: InputStream,
    maxImageSize: Int,
): Bitmap? {
    var b: Bitmap? = null
    //Decode image size
    val bitmapOptions = BitmapFactory.Options()
    bitmapOptions.inJustDecodeBounds = true
    bitmapOptions.inInputShareable = true;
    bitmapOptions.inPurgeable = true;
    BitmapFactory.decodeStream(fis, null, bitmapOptions)
    var scale = 1
    if (bitmapOptions.outHeight > maxImageSize || bitmapOptions.outWidth > maxImageSize) {
        scale = Math.pow(
            2.0,
            Math.ceil(Math.log(maxImageSize / Math.max(
                bitmapOptions.outHeight,
                bitmapOptions.outWidth
            ).toDouble()) / Math.log(0.5))
        ).toInt()
    }

    //Decode with inSampleSize
    val o2 = BitmapFactory.Options()
    o2.inSampleSize = scale
    b = BitmapFactory.decodeStream(fis, null, o2)
    return b
}

fun getMime(filename: String): String? {
    return getMimeTypeExtension(filename)
}

fun FTPClient.flushedInputStream(fileName:String): FlushedInputStream {
    return FlushedInputStream(this.retrieveFileStream(fileName))
}