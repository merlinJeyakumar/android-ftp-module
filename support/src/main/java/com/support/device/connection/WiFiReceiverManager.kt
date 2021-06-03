/*
 * Copyright (c) 2018 ThanksMister LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.support.device.connection

import android.annotation.SuppressLint
import android.content.Context
import android.net.*
import android.net.wifi.WifiManager
import android.os.Build
import android.text.TextUtils
import android.util.Log
import com.support.device.connection.WiFiUtility.*
import com.support.utills.NetworkUtils.isConnectedToWifi
import com.support.utills.text.TextUtills.trimBeginEndDoubleQuotes
import java.util.*
import kotlin.collections.HashMap
import kotlin.reflect.KClass


/*
https://gist.github.com/JosiasSena/100de74192ca3024da8494c1ca428294
 */
open class WiFiReceiverManager(
    private val context: Context
) {

    private lateinit var connectivityManager: ConnectivityManager
    private val TAG = this::class.java.simpleName
    private val wifiManager: WifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val callBackList = HashMap<KClass<out Any>, NetworkCallBack>()

    companion object {
        private var INSTANCE: WiFiReceiverManager? = null
        var disconnecting: Boolean = false
        var networkSSID: String = ""
        var networkPassword: String = ""
        var previousNetworkSSID: String = ""
        var networkId: Int = -1

        @JvmStatic
        fun getInstance(
            applicationContext: Context
        ): WiFiReceiverManager {
            if (INSTANCE == null) {
                synchronized(WiFiReceiverManager::javaClass) {
                    INSTANCE = WiFiReceiverManager(applicationContext)
                }
            }
            return INSTANCE!!
        }
    }

    fun init() {
        registerYourReceiver()
    }

    @SuppressLint("WrongConstant", "NewApi")
    private fun registerYourReceiver() {
        connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            val request: NetworkRequest = NetworkRequest.Builder()
                .build()
            connectivityManager.registerNetworkCallback(request, networkCallback)
        }
    }

    @SuppressLint("NewApi")
    private var networkCallback: ConnectivityManager.NetworkCallback =
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (context.isConnectedToWifi()) {
                    val currentWiFiModel = getCurrentWiFiModel()
                    postConnectionState(
                        WiFiConnectionStatusModel(
                            WiFiConnectionStatus.Connected,
                            currentWiFiModel.ipAddress
                        )
                    )
                }
            }

            override fun onLost(network: Network) {
                postConnectionState(WiFiConnectionStatusModel(wiFiConnectionStatus = WiFiConnectionStatus.Disconnected))
            }
        }

    fun getCurrentWiFiModel(context: Context = this.context): WiFiConnectionModel {
        var ssid: String? = null
        val connManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? =
            connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if (networkInfo!!.isConnected) {
            val connectionInfo = wifiManager.connectionInfo
            if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.ssid)) {
                ssid = connectionInfo.ssid
            }
            return WiFiConnectionModel(
                isConnected = networkInfo.isConnected,
                ssid = ssid?.trimBeginEndDoubleQuotes()!!,
                ipAddress = wifiAddress4(context)!!,
                networkId = connectionInfo.networkId,
                state = if (networkInfo.isConnected) WiFiConnectionModel.CONNECTED else WiFiConnectionModel.DISCONNECTED
            )
        }
        return WiFiConnectionModel(
            state = WiFiConnectionModel.DISCONNECTED,
            error = Throwable("wifi_connection_not_available")
        )
    }

    fun wifiAddress1(context: Context = this.context): String? {
        val connManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return getDefaultIpAddresses(connManager)
    }

    fun wifiAddress2(context: Context = this.context): String? {
        return getHotspotAddress(wifiManager)
    }

    fun wifiAddress3(context: Context = this.context): String {
        return getDefaultIpAddresses(getConnectionManager(context))
    }

    fun wifiAddress4(context: Context = this.context): String {
        return getIpAddress(context, wifiManager)
    }

    private fun getConnectionManager(context: Context): ConnectivityManager {
        return context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    fun postConnectionState(wiFiConnectionStatusModel: WiFiConnectionStatusModel) {
        callBackList.forEach {
            it.value.onNetworkConnectivityChanged(wiFiConnectionStatusModel)
        }
    }

    open fun addCallback(clazz: KClass<out Any>, networkCallBack: NetworkCallBack) {
        if (!callBackList.containsKey(clazz)) {
            callBackList[clazz] = networkCallBack
        } else {
            Log.e(TAG, "Illegal repeated adding")
        }
    }

    open fun removeCallBack(clazz: KClass<out Any>) {
        if (callBackList.containsKey(clazz)) {
            callBackList.remove(clazz)
        }
    }
}

open class NetworkCallBack {
    open fun onNetworkConnectivityChanged(wiFiConnectionStatusModel: WiFiConnectionStatusModel) {
        /*NOOP*/
    }
}

data class WiFiConnectionStatusModel(
    var wiFiConnectionStatus: WiFiConnectionStatus = WiFiConnectionStatus.Disconnected,
    var ipAddress: String? = null
)

enum class WiFiConnectionStatus {
    Connected,
    Disconnected
}