package com.nativedevps.ftp.client

import android.content.Context
import com.nativedevps.ftp.model.CredentialModel
import com.support.utills.Log
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import org.apache.commons.net.ftp.FTPReply
import java.io.InputStream
import java.io.OutputStream

class ClientManager(
    private val context: Context,
    private val clientStateCallback: ((ClientState) -> Unit)?,
) : IClientManager() {
    private var ftp = FTPClient()
    lateinit var credentialModel: CredentialModel
    private var clientState: ClientState = ClientState.DISCONNECTED

    fun setCredentials(credentialModel: CredentialModel) {
        this.credentialModel = credentialModel
    }

    fun build(): IClientManager {
        return this
    }

    /**
     * Executes connect command ip, port
     * Executes login command with username, password
     * lists out last directory contents
     **/
    override suspend fun login(callback: (Boolean, List<FTPFile>?, String?) -> Unit) {
        try {
            setState(ClientState.CONNECTING)
            ftp.connect(credentialModel.address ?: "", credentialModel.port?.toInt() ?: 0)
            setState(ClientState.CONNECTED)
            val replyCode = ftp.replyCode
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                ftp.disconnect()
                setState(ClientState.ERROR)
                callback(false, null, "Error in connection, code: $replyCode")
                return
            }
            setState(ClientState.LOGGING)
            ftp.login(credentialModel.userName, credentialModel.password)
            setState(ClientState.LOGGED_IN)
            ftp.controlEncoding = "UTF-8"
            setState(ClientState.FILES_RETRIEVING)
            ftp.listFiles().toList().apply {
                setState(ClientState.FILES_RETRIEVED)
                callback(true, this, null)
            }
        } catch (e: Exception) {
            Log.e(e.localizedMessage)
            setState(ClientState.ERROR)
            callback(false, null, "Error in connecting, kindly ensure credentials are working")
        }
    }

    /**
     * Executes cwd command
     * set current directory and list out it files
     **/
    override suspend fun cwd(
        fileName: String,
        callback: (Boolean, List<FTPFile>?, String?) -> Unit,
    ) {
        if (isActiveConnection()) {
            try {
                setState(ClientState.FILES_RETRIEVING)
                val replyCode = ftp.cwd(fileName)
                if (!FTPReply.isPositiveCompletion(replyCode)) {
                    callback(false, null, "Error in connection, code: $replyCode")
                    return
                }
                val files = ftp.listFiles()
                setState(ClientState.FILES_RETRIEVED)
                callback(true, files.toList(), null)
            } catch (e: Exception) {
                Log.e("disconnect " + e.localizedMessage)
                callback(false, null, "Kindly retry unable to process")
            }
        }
    }

    /**
     * Executes dot command with cwd (cwd..)
     * lists out last directory contents
     **/
    override suspend fun prevPull(
        fileName: String,
        outputStream: OutputStream,
        progress: ((Int) -> Unit)?,
        callback: (Boolean, String?) -> Unit,
    ) {
        pull(fileName, outputStream, progress, callback)
    }

    /**
     * Executes cwd command
     * list a files in the directory
     **/
    suspend fun pull(
        fileName: String,
        outputStream: OutputStream,
        progress: ((Int) -> Unit)?,
        callback: (Boolean, String?) -> Unit,
    ) {
        if (isActiveConnection()) {
            try {
                progress?.invoke(0)
                ftp.setFileType(FTP.BINARY_FILE_TYPE)
                ftp.retrieveFile(fileName, outputStream)
                progress?.invoke(100)
            } catch (e: Exception) {
                Log.e("disconnect " + e.localizedMessage)
                callback(false, "Kindly retry unable to process")
            }
        }
    }

    /**
     * Executes with stor command
     * write the content through file stream
     **/
    override suspend fun push(
        fileName: String,
        inputStream: InputStream,
        progress: ((Int) -> Unit)?,
        callback: (Boolean, String?) -> Unit,
    ) {
        if (isActiveConnection()) {
            try {
                progress?.invoke(0)
                ftp.setFileType(FTP.BINARY_FILE_TYPE)
                ftp.storeFile(fileName, inputStream)
                progress?.invoke(100)
                callback(true, null)
            } catch (e: Exception) {
                Log.e("disconnect " + e.localizedMessage)
                callback(false, "Kindly retry unable to process")
            }
        }
    }

    private fun setState(clientState: ClientState) {
        this.clientState = clientState
        clientStateCallback?.invoke(clientState)
    }

    override fun isActiveConnection(): Boolean {
        return ftp.isConnected
    }

    fun clientState(): ClientState {
        return clientState
    }

    /**
     * Disconnects ftp server
     **/
    override fun disconnect(callback: (Boolean, String?) -> Unit) {
        try {
            setState(ClientState.DISCONNECTING)
            ftp.disconnect()
            setState(ClientState.DISCONNECTED)
            callback(false, null)
        } catch (e: Exception) {
            Log.e("disconnect " + e.localizedMessage)
            callback(false, "Kindly retry unable to process")
        }
    }
}