package com.support.device.network.ftp.server

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
    val userName: String,
    val password: String? = null,
    val address: String,
    val port: Int,
    val exception: Exception?
)

data class FileTransferConnection(
    val connectionStatus: FileTransferServerConnectionStatus,
    val connectionProperties: FileTransferServerConnectionProperties
)