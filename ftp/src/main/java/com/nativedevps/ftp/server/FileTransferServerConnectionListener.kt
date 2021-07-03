package com.nativedevps.ftp.server

interface FileTransferServerConnectionListener {
    fun whenConnectionStatusChanged(fileTransferConnection: FileTransferConnection)
}

enum class FileTransferServerConnectionStatus {
    Initialized,
    Connected,
    Disconnected,
    Paused,
    Error
}

data class FileTransferServerConnectionProperties(
    var userName: String?=null,
    var password: String? = null,
    var address: String?=null,
    var port: Int?=null,
    var exception: Exception?=null,
    var browsePath: String?=null
)

data class FileTransferConnection(
    val connectionStatus: FileTransferServerConnectionStatus,
    val connectionProperties: FileTransferServerConnectionProperties
)