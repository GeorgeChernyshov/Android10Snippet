package com.example.post29.security

import javax.net.ssl.HandshakeCompletedEvent
import javax.net.ssl.HandshakeCompletedListener
import javax.net.ssl.SSLPeerUnverifiedException
import javax.net.ssl.SSLSession

class LoggingHandshakeCompletedListener : HandshakeCompletedListener {

    private var session: SSLSession? = null
    var protocol: String? = null
    var cipherSuite: String? = null
    var peerName: String? = null

    override fun handshakeCompleted(event: HandshakeCompletedEvent?) {
        session = event?.session
        protocol = session?.protocol
        cipherSuite = session?.cipherSuite

        try {
            peerName = session?.peerPrincipal?.name
        } catch (e: SSLPeerUnverifiedException) {
        }
    }
}