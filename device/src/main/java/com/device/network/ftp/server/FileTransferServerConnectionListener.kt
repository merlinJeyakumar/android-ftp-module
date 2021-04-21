package com.device.network.ftp.server

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

data class FileTransferConnection(
    val fileTransferServerConnectionStatus: FileTransferServerConnectionStatus,
    val exception: Exception?=null
)