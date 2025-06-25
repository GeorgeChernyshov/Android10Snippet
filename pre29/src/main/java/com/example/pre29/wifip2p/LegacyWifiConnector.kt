package com.example.pre29.wifip2p

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.SupplicantState
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager

class LegacyWifiConnector(
    private val context: Context,
    private val onNetworkEvent: (String) -> Unit
) {
    private var targetSsid: String? = null
    private var currentlyConnectedToTarget = false
    private var connectAttemptInitiated = false

    private val wifiManager = context.applicationContext
        .getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val wifiStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                WifiManager.NETWORK_STATE_CHANGED_ACTION -> {
                    val networkInfo = intent.getParcelableExtra<NetworkInfo>(
                        WifiManager.EXTRA_NETWORK_INFO
                    )

                    val currentWifiInfo = wifiManager.connectionInfo
                    if (networkInfo != null && currentWifiInfo != null)
                        handleNetworkInfo(networkInfo, currentWifiInfo)
                }
            }
        }
    }

    fun connectToNetwork(ssid: String, passphrase: String?) {
        targetSsid = ssid
        currentlyConnectedToTarget = false
        connectAttemptInitiated = true
        registerReceiver()

        val wifiConfig = WifiConfiguration()
        wifiConfig.SSID = "\"$ssid\""

        if (passphrase.isNullOrEmpty())
            wifiConfig.allowedKeyManagement.set(
                WifiConfiguration.KeyMgmt.NONE
            )
        else
            wifiConfig.preSharedKey = "\"$passphrase\""

        val existingConfigs = wifiManager.configuredNetworks

        val networkId = existingConfigs
            ?.find { it.SSID == wifiConfig.SSID }
            ?.networkId ?: wifiManager.addNetwork(wifiConfig)

        if (networkId == -1) {
            onNetworkEvent.invoke("Network Unavailable")
            connectAttemptInitiated = false

            return
        }

        if (!wifiManager.enableNetwork(networkId, true)) {
            onNetworkEvent.invoke("Network Unavailable")
            connectAttemptInitiated = false
        }
    }

    private fun handleNetworkInfo(
        networkInfo: NetworkInfo,
        wifiInfo: WifiInfo
    ) {
        val normalizedSsid = normalizeSsid(wifiInfo.ssid)
        if (normalizedSsid == targetSsid) {
            when (networkInfo.detailedState) {
                NetworkInfo.DetailedState.CONNECTED -> {
                    onNetworkEvent.invoke("Network available")
                    currentlyConnectedToTarget = true
                    connectAttemptInitiated = false
                }

                NetworkInfo.DetailedState.DISCONNECTED -> {
                    when {
                        currentlyConnectedToTarget -> {
                            onNetworkEvent.invoke("Network lost")
                            currentlyConnectedToTarget = false
                        }

                        connectAttemptInitiated -> {
                            onNetworkEvent.invoke("Network unavailable")
                            connectAttemptInitiated = false
                        }
                    }
                }

                NetworkInfo.DetailedState.FAILED,
                NetworkInfo.DetailedState.BLOCKED -> {
                    onNetworkEvent.invoke("Network unavailable")
                    connectAttemptInitiated = false
                    currentlyConnectedToTarget = false
                }

                else -> {}
            }
        } else if (
            networkInfo.detailedState == NetworkInfo.DetailedState.DISCONNECTED
                && currentlyConnectedToTarget
        ) {
            onNetworkEvent.invoke("Network lost")
            currentlyConnectedToTarget = false
            connectAttemptInitiated = false
        }
    }

    private fun handleSupplicantStateChanged(
        newState: SupplicantState,
        error: Int,
        wifiInfo: WifiInfo
    ) {
        val normalizedSsid = normalizeSsid(wifiInfo.ssid)
        if (normalizedSsid == targetSsid) {
            when (newState) {
                SupplicantState.COMPLETED -> {
                    onNetworkEvent.invoke("Network available")
                    currentlyConnectedToTarget = true
                    connectAttemptInitiated = false
                }

                SupplicantState.DISCONNECTED -> {
                    when {
                        currentlyConnectedToTarget -> {
                            onNetworkEvent.invoke("Network lost")
                            currentlyConnectedToTarget = false
                        }

                        connectAttemptInitiated -> {
                            onNetworkEvent.invoke("Network unavailable")
                            connectAttemptInitiated = false
                        }
                    }
                }

                SupplicantState.INVALID -> {
                    onNetworkEvent.invoke("Network unavailable")
                    connectAttemptInitiated = false
                    currentlyConnectedToTarget = false
                }

                else -> {}
            }
        }
    }

    private fun registerReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)
        context.registerReceiver(wifiStateReceiver, intentFilter)
    }

    private fun normalizeSsid(ssid: String?): String? {
        if (ssid.isNullOrEmpty()) {
            return null
        }

        return ssid.trim('"')
    }
}