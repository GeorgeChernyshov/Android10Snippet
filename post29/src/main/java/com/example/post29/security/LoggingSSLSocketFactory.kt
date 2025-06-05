package com.example.post29.security

import android.util.Log
import java.net.InetAddress
import java.net.Socket
import javax.net.ssl.HandshakeCompletedListener
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

class LoggingSSLSocketFactory(
    private val delegate: SSLSocketFactory,
    private val handshakeListener: HandshakeCompletedListener
) : SSLSocketFactory() {

    override fun createSocket(
        s: Socket?,
        host: String?,
        port: Int,
        autoClose: Boolean
    ): Socket {
        val socket = delegate.createSocket(s, host, port, autoClose) as SSLSocket
        customizeSocket(socket)

        return socket
    }

    override fun createSocket(p0: String?, p1: Int): Socket {
        val socket = delegate.createSocket(p0, p1) as SSLSocket
        customizeSocket(socket)

        return socket
    }

    override fun createSocket(
        p0: String?,
        p1: Int,
        p2: InetAddress?,
        p3: Int
    ): Socket {
        val socket = delegate.createSocket(p0, p1, p2, p3) as SSLSocket
        customizeSocket(socket)

        return socket
    }

    override fun createSocket(p0: InetAddress?, p1: Int): Socket {
        val socket = delegate.createSocket(p0, p1) as SSLSocket
        customizeSocket(socket)

        return socket
    }

    override fun createSocket(
        p0: InetAddress?,
        p1: Int,
        p2: InetAddress?,
        p3: Int
    ): Socket {
        val socket = delegate.createSocket(p0, p1, p2, p3) as SSLSocket
        customizeSocket(socket)

        return socket
    }

    override fun getDefaultCipherSuites(): Array<String> = delegate.defaultCipherSuites
    override fun getSupportedCipherSuites(): Array<String> = delegate.supportedCipherSuites

    private fun customizeSocket(socket: SSLSocket) {
        socket.addHandshakeCompletedListener(handshakeListener)
        socket.enabledCipherSuites = arrayOf("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256")
    }
}