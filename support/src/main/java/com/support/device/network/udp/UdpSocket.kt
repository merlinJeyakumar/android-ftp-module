package com.support.device.network.udp

import android.content.Context
import io.reactivex.rxjava3.core.ObservableEmitter

import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.functions.Cancellable
import org.jetbrains.anko.runOnUiThread
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.*


object UdpSocket {
    private fun getCancellable(udpSocket: DatagramSocket): Cancellable {
        return Cancellable {
            if (!udpSocket.isClosed) {
                udpSocket.close()
            }
        }
    }

    /**
     * creates an Observable that will emit all UDP datagrams of a UDP port.
     *
     *
     * This will be an infinite stream that ends when the observer unsubscribes, or when an error
     * occurs.
     *
     */
    fun create(port: Int, byteSize: Int): io.reactivex.rxjava3.core.Observable<UdpConnection> {
        return io.reactivex.rxjava3.core.Observable.create { emitter: ObservableEmitter<UdpConnection> ->
            try {
                emitter.onNext(
                    UdpConnection(
                        UdpConnectionStatus.CONNECTING
                    )
                )
                val datagramSocket = DatagramSocket(null)
                datagramSocket.reuseAddress = true;
                datagramSocket.broadcast = true;
                datagramSocket.bind(InetSocketAddress(port));

                val buffer = ByteArray(byteSize)
                val datagramPacket = DatagramPacket(buffer, buffer.size)
                emitter.onNext(
                    UdpConnection(
                        UdpConnectionStatus.CONNECTED
                    )
                )
                while (true) {
                    datagramSocket.receive(datagramPacket)
                    if (datagramPacket.length == 0) {
                        println("Read zero bytes")
                    } else {
                        println(Arrays.toString(datagramPacket.data))
                    }
                    if (!emitter.isDisposed) {
                        emitter.onNext(
                            UdpConnection(
                                UdpConnectionStatus.RECEIVED,
                                datagramPacket = datagramPacket,
                                message = datagramPacket.getPacketString()
                            )
                        )
                    }
                    datagramPacket.length = buffer.size
                }
            } catch (e: Exception) {
                if (!emitter.isDisposed) {
                    emitter.onNext(
                        UdpConnection(
                            UdpConnectionStatus.RECEIVED,
                            throwable = e.fillInStackTrace()
                        )
                    )
                }
            }
        }
    }


    fun createThread(
        context: Context,
        PORT: Int,
        PACKET_BYTE_SIZE: Int,
        udpConnectionListener: UdpConnectionListener
    ): Thread {
        val datagramSocket = DatagramSocket(null)
        datagramSocket.reuseAddress = true;
        datagramSocket.broadcast = true;
        datagramSocket.bind(InetSocketAddress(PORT));

        val buffer = ByteArray(PACKET_BYTE_SIZE)
        val datagramPacket = DatagramPacket(buffer, buffer.size)
        val thread: Thread = object : Thread() {
            override fun run() {
                try {
                    while (true) {
                        datagramSocket.receive(datagramPacket)
                        if (datagramPacket.length == 0) {
                            println("Read zero bytes")
                        } else {
                            println(Arrays.toString(datagramPacket.data))
                        }
                        val message = datagramPacket.getPacketString()
                        context.runOnUiThread {
                            udpConnectionListener.onUdpConnectionStateChanged(
                                udpConnectionStatus = UdpConnectionStatus.RECEIVED,
                                datagramPacket = datagramPacket,
                                message = message
                            )
                        }
                        datagramPacket.length = buffer.size
                    }
                } catch (e: InterruptedException) {
                    context.runOnUiThread {
                        udpConnectionListener.onUdpConnectionStateChanged(
                            udpConnectionStatus = UdpConnectionStatus.ERROR,
                            throwable = e
                        )
                    }
                    e.printStackTrace()
                    this.interrupt()
                }
            }
        }
        thread.start()
        return thread
    }

    fun sendUdp(ipAddress: String, port: Int, message: String): Single<Unit> {
        return Single.create { singleEmitter ->
            val inetAddress = InetAddress.getByName(ipAddress)
            val datagramSocket = DatagramSocket()
            try {
                val datagramPacket = DatagramPacket(
                    message.toByteArray(),
                    message.toByteArray().size, inetAddress, port
                )
                datagramSocket.send(datagramPacket)
                if (!singleEmitter.isDisposed) {
                    singleEmitter.onSuccess(Unit)
                }
            } catch (e: Exception) {
                if (!singleEmitter.isDisposed) {
                    singleEmitter.onError(e)
                }
            }
        }
    }

    fun DatagramPacket.getPacketString(): String {
        return String(
            this.data,
            this.offset,
            this.length
        )
    }

    data class UdpConnection(
        val udpConnectionStatus: UdpConnectionStatus,
        val datagramPacket: DatagramPacket? = null,
        val message: String? = null,
        val throwable: Throwable? = null
    )
}