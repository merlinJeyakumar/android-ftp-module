package com.nativedevps.ftp.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.nativedevps.ftp.decodeBitmapToSize
import com.nativedevps.ftp.flushedInputStream
import com.support.utills.Log
import com.support.utills.file.FileType
import com.support.utills.file.getFileType
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile

class FtpFileModel {
    lateinit var ftpFile: FTPFile
    var filePath: String = ""
    var fileType: FileType = FileType.ELSE
    var thumbnail: Bitmap? = null
    val fileName get() = ftpFile.name
    val createdOn get() = ftpFile.timestamp
    val uid get() = ftpFile.rawListing
    val isDirectory get() = ftpFile.isDirectory
    val size get() = ftpFile.size

    suspend fun build(
        ftpFile: FTPFile,
        ftpClient: FTPClient,
    ): FtpFileModel {
        this.ftpFile = ftpFile
        this.fileType = getFileType(ftpFile.name)
        when (this.fileType) {
            FileType.IMAGE -> {
                /*try {
                    ftpClient.flushedInputStream(ftpFile.name).use {
                        thumbnail = BitmapFactory.decodeStream(it)
                    }
                } catch (e: Exception) {
                }*/
            }
        }
        return this
    }
}