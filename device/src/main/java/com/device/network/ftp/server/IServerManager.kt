package com.device.network.ftp.server

import android.os.Environment
import java.io.File

interface IServerManager {
    fun checkConnection(): Boolean
    fun createConnection(
            username: String?=null,
            password: String?=null,
            serverBrowserPath: String = Environment.getExternalStorageDirectory().path + "/",
            port: Int = 2121
    )

    fun isConnected(): Boolean
    fun isPaused(): Boolean
    fun resumeOrStart()
    fun disconnect()
    fun pause()
    fun isStopped(): Boolean
    fun getWiFiIpAddress(): String
    fun addServerConnectionListener(fileTransferServerConnectionListener: FileTransferServerConnectionListener)
    fun clearListener(fileTransferServerConnectionListener: FileTransferServerConnectionListener)
    fun getPropsFile(): File
}
