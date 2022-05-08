package com.nativedevps.ftp.server

import android.os.Environment
import com.nativedevps.ftp.server.FileTransferConnection
import com.nativedevps.ftp.server.FileTransferServerConnectionListener
import java.io.File

interface IServerManager {
    fun checkConnection(): Boolean
    fun createConnection(
            username: String?=null,
            password: String?=null,
            serverBrowserPath: String = Environment.getExternalStorageDirectory().path,
            port: Int = 2121
    )

    fun isConnected(): Boolean
    fun isPaused(): Boolean
    fun resumeOrStart()
    fun disconnect()
    fun pause()
    fun isStopped(): Boolean
    fun addServerConnectionListener(fileTransferServerConnectionListener: FileTransferServerConnectionListener)
    fun clearListener(fileTransferServerConnectionListener: FileTransferServerConnectionListener)
    fun getPropsFile(): File
    fun getConnectionStatus(): FileTransferConnection?
    fun setBoosted(isBoosting: Boolean)
    fun isBoosted(): Boolean
}
