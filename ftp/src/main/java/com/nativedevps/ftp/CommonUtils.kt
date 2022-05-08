package com.nativedevps.ftp

import android.content.Context
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import com.support.device.connection.WiFiReceiverManager
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

fun getWiFiIpAddress(context: Context): String {
    return WiFiReceiverManager.getInstance(context).getLocalIpAddress()
}

@Throws(InvocationTargetException::class, IllegalAccessException::class)
fun wifiHotspotEnabled(context: Context): Boolean {
    val manager =
        context.applicationContext.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager
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
        (context.applicationContext.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager)
    return if (wifiMgr.isWifiEnabled) { // Wi-Fi adapter is ON
        val wifiInfo = wifiMgr.connectionInfo
        wifiInfo.networkId != -1 || getWiFiIpAddress(context).isNotEmpty()
    } else {
        false // Wi-Fi adapter is OFF
    }
}