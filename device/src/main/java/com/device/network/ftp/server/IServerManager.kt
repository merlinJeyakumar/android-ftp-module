package com.device.network.ftp.server

import android.os.Environment
import java.io.File

interface IServerManager {
    fun checkConnection(): Boolean
    fun createConnection(userPropsFile: File,
                         username: String,
                         password: String,
                         serverBrowserPath: String = Environment.getExternalStorageDirectory().path + "/",
                         port: Int = 2121,
                         connectionStatusFTP: (m: ServerManager.ServerConnectionStatusFTP) -> Unit)

    fun isConnected(): Boolean
    fun isPaused(): Boolean
    fun resumeOrStart()
    fun disconnect()
    fun pause()
    fun isStopped(): Boolean
}
