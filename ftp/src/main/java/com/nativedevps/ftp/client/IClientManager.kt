package com.nativedevps.ftp.client

import android.graphics.Bitmap
import com.nativedevps.ftp.model.FtpUrlModel
import com.nativedevps.ftp.model.FtpFileModel
import java.io.InputStream
import java.io.OutputStream

abstract class IClientManager {
    abstract fun isActiveConnection(): Boolean

    enum class ClientState {
        ERROR,
        DISCONNECTING,
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        LOGGING,
        LOGGED_IN,
        CACHE_RETRIEVED,
        FILES_RETRIEVING,
        FILES_RETRIEVED
    }

    abstract suspend fun login(callback: (Boolean, List<FtpFileModel>?, String?) -> Unit)
    abstract suspend fun cwd(
        path: String,
        cacheCallback: ((List<FtpFileModel>?)->Unit)?,
        callback: (Boolean, List<FtpFileModel>?, String?) -> Unit,
    )

    abstract suspend fun push(
        fileName: String,
        inputStream: InputStream,
        progress: ((Int) -> Unit)?,
        callback: (Boolean, String?) -> Unit,
    )

    abstract fun disconnect(callback: (Boolean, String?) -> Unit)
    abstract fun setCredentials(ftpUrlModel: FtpUrlModel): ClientManager
    abstract suspend fun getImageBitmap(
        fileName: String,
        callback: (Boolean, Bitmap?, String?) -> Unit
    )

    abstract suspend fun pull(
        fileName: String,
        outputStream: OutputStream,
        progress: ((Int,Int,Int) -> Unit)?,
        callback: (Boolean, String?) -> Unit
    )

    abstract fun dump()
}