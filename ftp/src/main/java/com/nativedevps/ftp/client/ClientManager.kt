package com.nativedevps.ftp.client

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.nativedevps.ftp.FlushedInputStream
import com.nativedevps.ftp.model.CredentialModel
import com.nativedevps.ftp.model.FtpFileModel
import com.support.utills.Log
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import org.apache.commons.net.ftp.FTPReply
import java.io.InputStream
import java.io.OutputStream

const val PREVIOUS_DIRECTORY = ".."

class ClientManager(
    private val context: Context,
    private val clientStateCallback: ((ClientState) -> Unit)?,
) : IClientManager() {
    private var ftpClient = FTPClient()
    lateinit var credentialModel: CredentialModel
    private var clientState: ClientState = ClientState.DISCONNECTED

    fun build(): IClientManager {
        return this
    }

    /**
     * Executes connect command ip, port
     * Executes login command with username, password
     * lists out last directory contents
     **/
    override suspend fun login(callback: (Boolean, List<FtpFileModel>?, String?) -> Unit) {
        try {
            setState(ClientState.CONNECTING)
            ftpClient.connect(credentialModel.address ?: "", credentialModel.port?.toInt() ?: 0)
            setState(ClientState.CONNECTED)
            val replyCode = ftpClient.replyCode
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                ftpClient.disconnect()
                setState(ClientState.ERROR)
                callback(false, null, "Error in connection, code: $replyCode")
                return
            }
            setState(ClientState.LOGGING)
            ftpClient.login(credentialModel.userName, credentialModel.password)
            setState(ClientState.LOGGED_IN)
            ftpClient.controlEncoding = "UTF-8"
            setState(ClientState.FILES_RETRIEVING)
            ftpClient.setFileType(BINARY_FILE_TYPE)
            ftpClient.listFiles().toList().apply {
                setState(ClientState.FILES_RETRIEVED)
                callback(true, getFtpAsModel(this), null)
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
        callback: (Boolean, List<FtpFileModel>?, String?) -> Unit
    ) {
        if (isActiveConnection()) {
            try {
                setState(ClientState.FILES_RETRIEVING)
                val replyCode = ftpClient.cwd(fileName)
                if (!FTPReply.isPositiveCompletion(replyCode)) {
                    callback(false, null, "Error in connection, code: $replyCode")
                    return
                }
                val files = ftpClient.listFiles()
                setState(ClientState.FILES_RETRIEVED)
                callback(true, getFtpAsModel(files.toList()), null)
            } catch (e: Exception) {
                e.printStackTrace()
                callback(false, null, "Kindly retry unable to process")
            }
        }
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
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE)
                ftpClient.retrieveFile(fileName, outputStream)
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
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE)
                ftpClient.storeFile(fileName, inputStream)
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
        return ftpClient.isConnected
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
            ftpClient.disconnect()
            setState(ClientState.DISCONNECTED)
            callback(false, null)
        } catch (e: Exception) {
            Log.e("disconnect " + e.localizedMessage)
            callback(false, "Kindly retry unable to process")
        }
    }

    override fun setCredentials(credentialModel: CredentialModel): ClientManager {
        this.credentialModel = credentialModel
        return this
    }

    override suspend fun getImageBitmap(
        fileName: String,
        callback: (Boolean, Bitmap?, String?) -> Unit,
    ) {
        try {
            val inputStream = FlushedInputStream(ftpClient.retrieveFileStream(fileName))
            val bitmap = BitmapFactory.decodeStream(inputStream)
            callback(true, bitmap, null)
        } catch (e: Exception) {
            Log.e("getImageBitmap: " + e.localizedMessage)
            callback(false, null, e.localizedMessage)
        }
    }

    private suspend fun getFtpAsModel(list: List<FTPFile>): List<FtpFileModel> {
        val ftpFileModel = mutableListOf<FtpFileModel>()
        for (ftpFile in list) {
            ftpFileModel.add(FtpFileModel().build(ftpFile, ftpClient))
        }
        return ftpFileModel.toList()
    }
}