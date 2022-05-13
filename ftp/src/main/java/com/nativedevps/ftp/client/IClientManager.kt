package com.nativedevps.ftp.client

import android.graphics.Bitmap
import com.nativedevps.ftp.model.CredentialModel
import com.nativedevps.ftp.model.FtpFileModel
import org.apache.commons.net.ftp.FTPFile
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
        FILES_RETRIEVING,
        FILES_RETRIEVED
    }

    abstract suspend fun login(callback: (Boolean, List<FtpFileModel>?, String?) -> Unit)
    abstract suspend fun cwd(fileName: String, callback: (Boolean, List<FtpFileModel>?, String?) -> Unit)

    abstract suspend fun push(
        fileName: String,
        inputStream: InputStream,
        progress: ((Int) -> Unit)?,
        callback: (Boolean, String?) -> Unit,
    )

    abstract fun disconnect(callback: (Boolean, String?) -> Unit)
    abstract fun setCredentials(credentialModel: CredentialModel): ClientManager
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
}