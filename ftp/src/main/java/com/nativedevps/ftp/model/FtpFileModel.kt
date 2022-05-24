package com.nativedevps.ftp.model

import android.content.Context
import android.net.Uri
import com.support.utills.file.FileType
import com.support.utills.file.getFileType
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile

class FtpFileModel {
    lateinit var ftpFile: FTPFile
    var ftpAddress: String = ""
    var filePath: String = ""
    var fileType: FileType = FileType.ELSE
    val fileName get() = ftpFile.name
    val createdOn get() = ftpFile.timestamp
    val uid get() = ftpAddress
    val isDirectory get() = ftpFile.isDirectory
    val size get() = ftpFile.size

    suspend fun build(
        context: Context,
        baseAddress: String,
        ftpFile: FTPFile,
        ftpClient: FTPClient,
    ): FtpFileModel {
        this.ftpFile = ftpFile
        this.fileType = getFileType(ftpFile.name)
        this.ftpAddress = "$baseAddress${ftpClient.printWorkingDirectory()}/${ftpFile.name}"
        this.filePath = "${ftpClient.printWorkingDirectory()}/${ftpFile.name}"

        when (this.fileType) {
            FileType.IMAGE -> {
                //noop
            }
        }
        return this
    }

    fun getCredentialModel(): FtpUrlModel {
        return FtpUrlModel.fromUrl(ftpAddress)
    }
}