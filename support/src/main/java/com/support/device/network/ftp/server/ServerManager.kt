package com.support.device.network.ftp.server

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.support.device.connection.WiFiReceiverManager
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
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*


class ServerManager private constructor(val context: Context) : IServerManager {
    private var fileTransferServerConnectionProperties: FileTransferServerConnectionProperties? = null
    private var fileTransferConnection: FileTransferConnection? = null
    private val TAG: String = this::class.java.simpleName

    private val ANONYMOUS_USER_NAME = "anonymous"
    private val ANONYMOUS_USER_PASSWORD = "anonymous@domain.com"
    private lateinit var ftpServerFactory: FtpServerFactory
    private var ftpServer: FtpServer? = null
    private var listenerFactory = ListenerFactory()
    private val listeners = mutableListOf<FileTransferServerConnectionListener>()

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
        ftpServerFactory = FtpServerFactory()
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
        val wiFiIpAddress = getWiFiIpAddress()
        // anonymous/authenticated
        fileTransferServerConnectionProperties = FileTransferServerConnectionProperties(
                userName = username,
                password = password,
                address = getWiFiIpAddress(),
                browsePath = serverBrowserPath,
                port = port,
                exception = null
        )
        Log.i(
                TAG,
                "-config port:$port userName:$username password:$password address:$wiFiIpAddress"
        )
        setupStart(
                port,
                getPropsFile(),
                getUser(fileTransferServerConnectionProperties!!)
        )
    }

    private fun getUser(transferServerConnectionProperties: FileTransferServerConnectionProperties): BaseUser {
        return if (transferServerConnectionProperties.userName != null && transferServerConnectionProperties.password != null) {
            getAuthenticatedUser()
        } else {
            getAnonymousUser()
        }
    }

    private val ftpLetCallback = object : Ftplet {
        @Throws(FtpException::class)
        override fun init(ftpletContext: FtpletContext) {
            //no-opsdsd
            updateConnectionStatus(
                    FileTransferConnection(
                            FileTransferServerConnectionStatus.Initialized,
                            fileTransferServerConnectionProperties!!
                    )
            )
        }

        override fun destroy() {
            //no-op
            updateConnectionStatus(
                    FileTransferConnection(
                            FileTransferServerConnectionStatus.Disconnected,
                            fileTransferServerConnectionProperties!!
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
                            fileTransferServerConnectionProperties!!
                    )
            )
            return FtpletResult.DEFAULT
        }

        @Throws(FtpException::class, IOException::class)
        override fun onDisconnect(session: FtpSession): FtpletResult {
            return FtpletResult.DEFAULT
        }
    }

    private fun getAuthenticatedUser(
            username: String = "admin",
            pass: String = "pass"
    ): BaseUser {
        val user = BaseUser()
        user.name = username
        user.password = pass
        user.homeDirectory = Environment.getExternalStorageDirectory().path + "/"
        val auths: MutableList<Authority> = ArrayList()
        val auth: Authority = WritePermission()
        auths.add(auth)
        user.authorities = auths
        return user
    }

    private fun getAnonymousUser(): BaseUser {
        val user = BaseUser()
        val connectionConfigFactory = ConnectionConfigFactory()
        connectionConfigFactory.isAnonymousLoginEnabled = true
        ftpServerFactory.connectionConfig = connectionConfigFactory.createConnectionConfig();
        user.name = ANONYMOUS_USER_NAME
        //user.password = ANONYMOUS_USER_PASSWORD
        val auths: MutableList<Authority> = ArrayList()
        val auth: Authority = WritePermission()
        auths.add(auth)
        user.authorities = auths
        return user
    }

    private fun updateConnectionStatus(fileTransferConnection: FileTransferConnection) {
        this.fileTransferConnection = fileTransferConnection
        for (listener in listeners) {
            listener.whenConnectionStatusChanged(fileTransferConnection)
        }
    }

    override fun isConnected(): Boolean {
        return !(ftpServer?.isStopped!! || ftpServer?.isSuspended!!)
    }

    override fun isPaused(): Boolean {
        return ftpServer?.isSuspended!!
    }

    override fun resumeOrStart() {
        if (isPaused()) {
            ftpServer?.resume()
        } else {
            ftpServer?.start()
        }
    }

    override fun disconnect() {
        Log.i(TAG, "-Config disconnect")
        ftpServer?.stop()
        init()
    }

    override fun pause() {
        ftpServer?.suspend()
    }

    override fun isStopped(): Boolean {
        return ftpServer?.isStopped!!
    }

    fun restart() {
        init()
    }

    @Throws(FileNotFoundException::class)
    private fun setupStart(
            port: Int,
            propsFile: File,
            user: BaseUser
    ) {
        listenerFactory.port = port
        ftpServerFactory.addListener("default", listenerFactory.createListener())
        val files = File(Environment.getExternalStorageDirectory().path + "/users.properties")
        if (!files.exists()) {
            try {
                files.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        val userManagerFactory = PropertiesUserManagerFactory()
        userManagerFactory.file = propsFile
        userManagerFactory.passwordEncryptor = SaltedPasswordEncryptor()

        val userManager = userManagerFactory.createUserManager()
        userManager.save(user)
        ftpServerFactory.userManager = userManager
        ftpServerFactory.ftplets = mutableMapOf<String, Ftplet>("miaFtplet" to ftpLetCallback)
        ftpServer?.start()
    }


    fun getUser(): BaseUser {
        val user = BaseUser()
        user.name = "merlin"
        user.password = "pass"
        val home = Environment.getExternalStorageDirectory().path + "/"
        user.homeDirectory = home
        val auths: MutableList<Authority> = ArrayList()
        val auth: Authority = WritePermission()
        auths.add(auth)
        user.authorities = auths
        return user
    }

    @Throws(FileNotFoundException::class)
    private fun setupStart(username: String, password: String, subLoc: String) {
        listenerFactory.port = 2121
        ftpServerFactory.addListener("default", listenerFactory.createListener())
        val files = File(Environment.getExternalStorageDirectory().path + "/users.properties")
        if (!files.exists()) {
            try {
                files.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        val userManagerFactory = PropertiesUserManagerFactory()
        userManagerFactory.file = files
        userManagerFactory.passwordEncryptor = SaltedPasswordEncryptor()
        val um = userManagerFactory.createUserManager()
        val user = BaseUser()
        user.name = username
        user.password = password
        val home = Environment.getExternalStorageDirectory().path + "/" + subLoc
        user.homeDirectory = home
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
            }

            override fun destroy() {}

            @Throws(FtpException::class, IOException::class)
            override fun beforeCommand(session: FtpSession, request: FtpRequest): FtpletResult {
                return FtpletResult.DEFAULT
            }

            @Throws(FtpException::class, IOException::class)
            override fun afterCommand(session: FtpSession, request: FtpRequest, reply: FtpReply): FtpletResult {
                return FtpletResult.DEFAULT
            }

            @Throws(FtpException::class, IOException::class)
            override fun onConnect(session: FtpSession): FtpletResult {
                return FtpletResult.DEFAULT
            }

            @Throws(FtpException::class, IOException::class)
            override fun onDisconnect(session: FtpSession): FtpletResult {
                return FtpletResult.DEFAULT
            }
        }
        ftpServerFactory.ftplets = m
        ftpServer?.start()
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
        return WiFiReceiverManager.getInstance(context).wifiAddress4()
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
        val files = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.path + "/users.properties")
        } else {
            File(Environment.getExternalStorageDirectory().path + "/users.properties")
        }
        if (!files.exists()) {
            try {
                files.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return files
    }
}