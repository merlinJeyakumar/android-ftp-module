package com.nativedevps.ftp.client

import android.content.Context
import android.graphics.Bitmap
import com.nativedevps.ftp.Utilitsss.downloadSample
import com.nativedevps.ftp.client.cache.FilesCache
import com.nativedevps.ftp.model.FtpFileModel
import com.nativedevps.ftp.model.FtpUrlModel
import com.support.utills.GeneralUtils.getProgress
import com.support.utills.Log
import com.support.utills.file.copy
import org.apache.commons.io.output.CountingOutputStream
import org.apache.commons.net.ftp.FTP
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
    lateinit var ftpUrlModel: FtpUrlModel
    private var clientState: ClientState = ClientState.DISCONNECTED
    private lateinit var baseAddress: String
    private var filesCache = FilesCache()
    private var currentDirectory: FtpUrlModel? = null

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
            ftpClient.connect(ftpUrlModel.address ?: "", ftpUrlModel.port?.toInt() ?: 0)
            setState(ClientState.CONNECTED)
            val replyCode = ftpClient.replyCode
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                ftpClient.disconnect()
                setState(ClientState.ERROR)
                callback(false, null, "Error in connection, code: $replyCode")
                return
            }
            setState(ClientState.LOGGING)
            ftpClient.login(ftpUrlModel.userName, ftpUrlModel.password)
            setState(ClientState.LOGGED_IN)
            ftpClient.controlEncoding = "UTF-8"
            setState(ClientState.FILES_RETRIEVING)
            ftpClient.changeWorkingDirectory(ftpUrlModel.initialPath)
            baseAddress = ftpUrlModel.ftpBaseAddress
            filesCache.dump()
            ftpClient.listFiles().toList().apply {
                setState(ClientState.FILES_RETRIEVED)
                val ftpModelList = getFtpAsModel(this).apply {
                    currentDirectory = FtpUrlModel.fromUrl(first().ftpAddress)
                }

                cacheFiles(ftpModelList)
                callback(true, ftpModelList, null)
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
        path: String,
        cacheCallback: ((List<FtpFileModel>?) -> Unit)?,
        callback: (Boolean, List<FtpFileModel>?, String?) -> Unit,
    ) {
        if (isActiveConnection()) {
            try {
                Log.e("TAG", "CurrentDirectory: $currentDirectory")
                Log.e("TAG", "Path: $path")

                val urlModel = getFtpUrlModel(path)!!
                retrieveCachedFiles(urlModel)?.let {
                    setState(ClientState.CACHE_RETRIEVED)
                    cacheCallback?.invoke(it)
                }
                if (clientState != ClientState.CACHE_RETRIEVED) {
                    setState(ClientState.FILES_RETRIEVING)
                }
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE)
                if (!ftpClient.changeWorkingDirectory(urlModel.getRawPath())) {
                    callback(false, null, "unable to change directory")
                    return
                }
                val files = getFtpAsModel(ftpClient.listFiles().toList()).apply {
                    currentDirectory = getFtpUrlModel(firstOrNull()?.ftpAddress) ?: currentDirectory
                }
                cacheFiles(files)
                setState(ClientState.FILES_RETRIEVED)
                /*if (path == PREVIOUS_DIRECTORY) { //noop
                    filesCache.moveCursorTop()
                }*/
                callback(true, files, null)
            } catch (e: Exception) {
                e.printStackTrace()
                callback(false, null, "Kindly retry unable to process")
            }
        }
    }

    private fun getFtpUrlModel(path: String?): FtpUrlModel? {
        return if (path == PREVIOUS_DIRECTORY) {
            FtpUrlModel.fromUrl(currentDirectory?.previousPathAddress ?: ftpUrlModel.initialPath!!) //fixme: previous not working as expected
        } else {
            path?.let { FtpUrlModel.fromUrl(it) }
        }
    }

    /**
     * Executes cwd command
     * list a files in the directory
     **/
    override suspend fun pull(
        fileName: String,
        outputStream: OutputStream,
        progress: ((Int, Int, Int) -> Unit)?,
        callback: (Boolean, String?) -> Unit,
    ) {
        if (isActiveConnection()) {
            try {
                val cos: CountingOutputStream = object : CountingOutputStream(outputStream) {
                    override fun beforeWrite(size: Int) {
                        super.beforeWrite(size)
                        progress?.invoke(
                            getProgress(count, size),
                            count,
                            size
                        )
                        println("Downloaded $count/$size")
                    }
                }

                ftpClient.setFileType(FTP.BINARY_FILE_TYPE)
                copy(ftpClient.retrieveFileStream(fileName), cos)
                callback(true, null)
                ftpClient.completePendingCommand()
            } catch (e: Exception) {
                ftpClient.completePendingCommand()
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
        Log.e("ClientState: ${clientState.name}")
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

    override fun setCredentials(ftpUrlModel: FtpUrlModel): ClientManager {
        this.ftpUrlModel = ftpUrlModel
        return this
    }

    override suspend fun getImageBitmap(
        fileName: String,
        callback: (Boolean, Bitmap?, String?) -> Unit,
    ) {
        try {
            ftpClient.retrieveFileStream(fileName).use {
                val bitmap = downloadSample(it)
                ftpClient.completePendingCommand()
                callback(true, bitmap, null)
            }
        } catch (e: Exception) {
            Log.e("getImageBitmap: " + e.localizedMessage)
            ftpClient.completePendingCommand()
            callback(false, null, e.localizedMessage)
        }
    }

    private suspend fun getFtpAsModel(list: List<FTPFile>): List<FtpFileModel> {
        val ftpFileModel = mutableListOf<FtpFileModel>()
        for (ftpFile in list) {
            ftpFileModel.add(FtpFileModel().build(context, baseAddress, ftpFile, ftpClient))
        }
        return ftpFileModel.toList()
    }

    private fun cacheFiles(list: List<FtpFileModel>) {
        currentDirectory?.let { filesCache.pushElement(it, list) } //fixme:
    }

    private fun retrieveCachedFiles(path: FtpUrlModel): List<FtpFileModel>? {
        return filesCache.getElement(path)
    }

    override fun dump(){
        ftpClient.disconnect()
        setState(ClientState.DISCONNECTED)
        currentDirectory = null
        filesCache.dump()
    }

    private fun isInitialPath(): Boolean {
        return ftpUrlModel.initialPath == ftpClient.printWorkingDirectory()
    }

    companion object {
        private var clientManager: ClientManager? = null
        open fun getInstance(
            context: Context,
            clientStateCallback: ((ClientState) -> Unit)?,
        ): ClientManager {
            return clientManager ?: (ClientManager(context, clientStateCallback).apply {
                clientManager = this
            })
        }
    }
}