package com.example.post29.wifip2p

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.Q)
class PeerToPeerConnector(
    context: Context,
    private val onNetworkEvent: (String) -> Unit
) {

    private var connectivityManager = context.getSystemService(
        Context.CONNECTIVITY_SERVICE
    ) as ConnectivityManager

    private var activeNetworkCallback: ConnectivityManager.NetworkCallback? = null

    fun requestConnection(ssid: String, passphrase: String?) {
        releaseConnection()

        val specifierBuilder = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)

        if (!passphrase.isNullOrEmpty())
            specifierBuilder.setWpa2Passphrase(passphrase)

        val networkSpecifier = specifierBuilder.build()
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .setNetworkSpecifier(networkSpecifier)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)

                onNetworkEvent.invoke("Network available")
            }

            override fun onUnavailable() {
                super.onUnavailable()

                onNetworkEvent.invoke("Network unavailable")
            }

            override fun onLost(network: Network) {
                super.onLost(network)

                onNetworkEvent.invoke("Network lost")
            }
        }

        this.activeNetworkCallback = networkCallback
        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }

    fun releaseConnection() {
        activeNetworkCallback?.let {
            try {
                connectivityManager.unregisterNetworkCallback(it)
                Log.d(TAG, "Network callback unregistered.")
            } catch (e: IllegalArgumentException) {
                Log.w(TAG, "Callback was already unregistered or never registered.", e)
            }

            activeNetworkCallback = null
        }
    }

    companion object {
        private const val TAG = "PeerToPeerConnector"
    }
}