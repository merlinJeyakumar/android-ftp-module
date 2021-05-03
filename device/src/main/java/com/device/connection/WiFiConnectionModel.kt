package com.device.connection

data class WiFiConnectionModel(
    var isConnected: Boolean = false,
    var ssid: String = "",
    var ipAddress: String = "",
    var networkId: Int = 0,
    var state: Int = if (isConnected) CONNECTED else DISCONNECTED,
    var error: Throwable? = null
) {
    companion object {
        val DISCONNECTED = 0
        val CONNECTING = 1
        val CONNECTED = 2
        val DISCONNECTING = 3
        val CONNECTION_FAILED = 4
    }
}