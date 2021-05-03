package com.device.network.udp

import java.net.DatagramPacket

interface UdpConnectionListener {
    fun onUdpConnectionStateChanged(
        udpConnectionStatus: UdpConnectionStatus,
        datagramPacket: DatagramPacket? = null,
        message:String?=null,
        throwable: Throwable? = null
    )
}