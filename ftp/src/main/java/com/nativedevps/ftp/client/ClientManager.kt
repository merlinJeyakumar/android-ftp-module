package com.nativedevps.ftp.client

import android.content.Context
import org.apache.commons.net.ftp.FTPClient

class ClientManager(context: Context) : IClientManager() {
    private var ftp = FTPClient()

    fun setCredentials() {

    }
}