package com.example.post29.security

import android.os.AsyncTask
import org.apache.http.conn.ssl.AllowAllHostnameVerifier
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext

class TLSRequestTask(
    val handshakeCompletedListener: LoggingHandshakeCompletedListener = LoggingHandshakeCompletedListener()
) : AsyncTask<String, Any, String>() {

    override fun doInBackground(vararg p0: String?): String {
        val urlConnection = URL(PATH)
            .openConnection() as HttpsURLConnection

        urlConnection.connectTimeout = 5000
        urlConnection.readTimeout = 5000
        urlConnection.hostnameVerifier = AllowAllHostnameVerifier()

        val sslContext = SSLContext.getDefault()
        urlConnection.sslSocketFactory = LoggingSSLSocketFactory(
            delegate = sslContext.socketFactory,
            handshakeListener = handshakeCompletedListener
        )

        return urlConnection
            .inputStream
            .readBytes()
            .decodeToString()
    }

    companion object {
        private const val PATH = "https://192.168.1.51:8443/welcome.txt"
    }
}