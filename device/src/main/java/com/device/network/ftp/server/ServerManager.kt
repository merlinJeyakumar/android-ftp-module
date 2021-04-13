package com.device.network.ftp.server

import android.content.Context
import android.content.DialogInterface
import android.net.wifi.WifiManager
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.device.R
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
import java.util.ArrayList
import java.util.HashMap

class ServerManager private constructor(val context: Context) : IServerManager {
    private val TAG: String = this::class.java.simpleName

    var ftpServerFactory = FtpServerFactory()
    lateinit var ftpServer: FtpServer
    var listenerFactory = ListenerFactory()
    var propertiesUserManagerFactory = PropertiesUserManagerFactory()

    companion object {
        @JvmStatic
        private var INSTANCE: ServerManager? = null

        fun getInstance(context: Context): IServerManager {

            if (INSTANCE !=null) {
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
            userPropsFile: File,
            username: String,
            password: String,
            serverBrowserPath: String,
            port: Int,
            connectionStatusFTP: (m: ServerConnectionStatusFTP) -> Unit
    ) {
        listenerFactory.port = port
        ftpServerFactory.addListener("default", listenerFactory.createListener())
        propertiesUserManagerFactory.file = userPropsFile
        propertiesUserManagerFactory.passwordEncryptor = SaltedPasswordEncryptor()
        val um = propertiesUserManagerFactory.createUserManager()
        val user = BaseUser()
        user.name = username
        user.password = password
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
                connectionStatusFTP(ServerConnectionStatusFTP.Initialized)
            }

            override fun destroy() {
                //no-op
                connectionStatusFTP(ServerConnectionStatusFTP.Disconnected)
            }

            @Throws(FtpException::class, IOException::class)
            override fun beforeCommand(session: FtpSession, request: FtpRequest): FtpletResult {
                Log.e(TAG, "beforeCommand $session.sessionId ${request.command}")
                return FtpletResult.DEFAULT
            }

            @Throws(FtpException::class, IOException::class)
            override fun afterCommand(session: FtpSession, request: FtpRequest, reply: FtpReply): FtpletResult {
                Log.e(TAG, "afterCommand ${session.sessionId} ${request.command} ${reply.message}")
                return FtpletResult.DEFAULT
            }

            @Throws(FtpException::class, IOException::class)
            override fun onConnect(session: FtpSession): FtpletResult {
                connectionStatusFTP(ServerConnectionStatusFTP.Connected)
                return FtpletResult.DEFAULT
            }

            @Throws(FtpException::class, IOException::class)
            override fun onDisconnect(session: FtpSession): FtpletResult {
                connectionStatusFTP(ServerConnectionStatusFTP.Disconnected)
                return FtpletResult.DEFAULT
            }
        }
        ftpServerFactory.ftplets = m
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
        val manager = context.applicationContext.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager
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
        val wifiMgr = (context.applicationContext.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager)
        return if (wifiMgr.isWifiEnabled) { // Wi-Fi adapter is ON
            val wifiInfo = wifiMgr.connectionInfo
            wifiInfo.networkId != -1
        } else {
            false // Wi-Fi adapter is OFF
        }
    }

    enum class ServerConnectionStatusFTP {
        Initialized,
        Connected,
        Disconnected,
        Paused,
        Error
    }
}