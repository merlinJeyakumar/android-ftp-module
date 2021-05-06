package com.support.device.network.ftp.server

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.support.device.network.ftp.server.*
import org.apache.ftpserver.ConnectionConfigFactory
import org.apache.ftpserver.FtpServer
import org.apache.ftpserver.FtpServerFactory
import org.apache.ftpserver.ftplet.*
import org.apache.ftpserver.listener.ListenerFactory
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory
import org.apache.ftpserver.usermanager.SaltedPasswordEncryptor
import org.apache.ftpserver.usermanager.impl.BaseUser
import org.apache.ftpserver.usermanager.impl.WritePermission
import java.io.File
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*

class ServerManager private constructor(val context: Context) : IServerManager {
    private var fileTransferConnection: FileTransferConnection? = null
    private val TAG: String = this::class.java.simpleName

    val ANONYMOUS_USER_NAME = "anonymous@example.com"
    var ftpServerFactory = FtpServerFactory()
    lateinit var ftpServer: FtpServer
    var listenerFactory = ListenerFactory()
    var propertiesUserManagerFactory = PropertiesUserManagerFactory()
    val listeners = mutableListOf<FileTransferServerConnectionListener>()

    companion object {
        @JvmStatic
        private var INSTANCE: ServerManager? = null

        fun getInstance(context: Context): IServerManager {

            if (INSTANCE != null) {
                INSTANCE
            } else {
                INSTANCE = ServerManager(context).init()
            }
            return INSTANCE as ServerManager
        }
    }

    private fun init(): ServerManager {
        ftpServer = ftpServerFactory.createServer()
        return this
    }

    override fun checkConnection(): Boolean {
        try {
            return checkWifiOnAndConnected(context) || wifiHotspotEnabled(context)
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        return false
    }

    override fun createConnection(
        username: String?,
        password: String?,
        serverBrowserPath: String,
        port: Int
    ) {
        var username = username
        if (username == null) {
            username = ANONYMOUS_USER_NAME
        }
        listenerFactory.port = port
        ftpServerFactory.addListener("default", listenerFactory.createListener())
        //propertiesUserManagerFactory.file = getPropsFile() //todo
        propertiesUserManagerFactory.passwordEncryptor = SaltedPasswordEncryptor()
        val um = propertiesUserManagerFactory.createUserManager()
        val user = if (username == ANONYMOUS_USER_NAME) {
            val connectionConfigFactory = ConnectionConfigFactory()
            connectionConfigFactory.isAnonymousLoginEnabled = true
            ftpServerFactory.connectionConfig = connectionConfigFactory.createConnectionConfig()
            getAnonymousUser()
        } else {
            if (password != null) {
                getAuthenticatedUser(username, password)
            } else {
                Log.e(TAG, "password_required")
                return
            }
        }
        user.homeDirectory = serverBrowserPath
        val auths: MutableList<Authority> = ArrayList()
        val auth: Authority = WritePermission()
        auths.add(auth)
        user.authorities = auths
        try {
            um.save(user)
        } catch (e1: FtpException) {
            e1.printStackTrace()
        }
        ftpServerFactory.userManager = um
        val m: MutableMap<String, Ftplet> = HashMap()
        m["miaFtplet"] = object : Ftplet {
            @Throws(FtpException::class)
            override fun init(ftpletContext: FtpletContext) {
                //no-op
                updateConnectionStatus(
                    FileTransferConnection(
                        FileTransferServerConnectionStatus.Initialized,
                        FileTransferServerConnectionProperties(
                            userName = username,
                            password = password,
                            address = getWiFiIpAddress(),
                            port = port,
                            exception = null
                        )
                    )
                )
            }

            override fun destroy() {
                //no-op
                updateConnectionStatus(
                    FileTransferConnection(
                        FileTransferServerConnectionStatus.Disconnected,
                        FileTransferServerConnectionProperties(
                            userName = username,
                            password = password,
                            address = getWiFiIpAddress(),
                            port = port,
                            exception = null
                        )

                    )
                )
            }

            @Throws(FtpException::class, IOException::class)
            override fun beforeCommand(session: FtpSession, request: FtpRequest): FtpletResult {
                Log.e(TAG, "beforeCommand $session.sessionId ${request.command}")
                return FtpletResult.DEFAULT
            }

            @Throws(FtpException::class, IOException::class)
            override fun afterCommand(
                session: FtpSession,
                request: FtpRequest,
                reply: FtpReply
            ): FtpletResult {
                Log.e(TAG, "afterCommand ${session.sessionId} ${request.command} ${reply.message}")
                return FtpletResult.DEFAULT
            }

            @Throws(FtpException::class, IOException::class)
            override fun onConnect(session: FtpSession): FtpletResult {
                updateConnectionStatus(
                    FileTransferConnection(
                        FileTransferServerConnectionStatus.Connected,
                        FileTransferServerConnectionProperties(
                            userName = username,
                            password = password,
                            address = getWiFiIpAddress(),
                            port = port,
                            exception = null
                        )
                    )
                )
                return FtpletResult.DEFAULT
            }

            @Throws(FtpException::class, IOException::class)
            override fun onDisconnect(session: FtpSession): FtpletResult {
                return FtpletResult.DEFAULT
            }
        }
        ftpServerFactory.ftplets = m
        resumeOrStart()
    }

    private fun getAuthenticatedUser(
        username: String = "admin",
        pass: String = "pass"
    ): BaseUser {
        val user = BaseUser()
        user.name = username
        user.password = pass
        val auths: MutableList<Authority> = ArrayList()
        val auth: Authority = WritePermission()
        auths.add(auth)
        user.authorities = auths
        return user
    }

    private fun getAnonymousUser(): BaseUser {
        val user = BaseUser()
        user.name = "anonymous"
        return user
    }

    private fun updateConnectionStatus(fileTransferConnection: FileTransferConnection){
        this.fileTransferConnection = fileTransferConnection
        for (listener in listeners) {
            listener.whenConnectionStatusChanged(fileTransferConnection)
        }
    }

    override fun isConnected(): Boolean {
        return !(ftpServer.isStopped || ftpServer.isSuspended)
    }

    override fun isPaused(): Boolean {
        return ftpServer.isSuspended
    }

    override fun resumeOrStart() {
        if (isPaused()) {
            ftpServer.resume()
        } else {
            ftpServer.start()
        }
    }

    override fun disconnect() {
        ftpServer.stop()
        ftpServer = ftpServerFactory.createServer()
    }

    override fun pause() {
        ftpServer.suspend()
    }

    override fun isStopped(): Boolean {
        return ftpServer.isStopped
    }

    @Throws(InvocationTargetException::class, IllegalAccessException::class)
    private fun wifiHotspotEnabled(context: Context): Boolean {
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

    private fun checkWifiOnAndConnected(context: Context): Boolean {
        val wifiMgr =
            (context.applicationContext.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager)
        return if (wifiMgr.isWifiEnabled) { // Wi-Fi adapter is ON
            val wifiInfo = wifiMgr.connectionInfo
            wifiInfo.networkId != -1 || getWiFiIpAddress().isNotEmpty()
        } else {
            false // Wi-Fi adapter is OFF
        }
    }

    override fun getConnectionStatus(): FileTransferConnection? {
        return fileTransferConnection
    }

    override fun getWiFiIpAddress(): String {
        try {
            if (wifiHotspotEnabled(context)) {
                return "192.168.43.1"
            }
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        return Utils.getIPAddress(true)
    }

    override fun addServerConnectionListener(fileTransferServerConnectionListener: FileTransferServerConnectionListener) {
        if (!listeners.contains(fileTransferServerConnectionListener)) {
            listeners.add(fileTransferServerConnectionListener)
        }
    }

    override fun clearListener(fileTransferServerConnectionListener: FileTransferServerConnectionListener) {
        if (!listeners.contains(fileTransferServerConnectionListener)) {
            listeners.remove(fileTransferServerConnectionListener)
        }
    }

    override fun getPropsFile(): File {
        return File(context.filesDir, "connection.properties")
    }
}