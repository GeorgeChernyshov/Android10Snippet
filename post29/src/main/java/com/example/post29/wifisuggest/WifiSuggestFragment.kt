package com.example.post29.wifisuggest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.post29.Config
import com.example.post29.databinding.FragmentWifiSuggestBinding

class WifiSuggestFragment : Fragment() {

    private lateinit var binding: FragmentWifiSuggestBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWifiSuggestBinding.inflate(inflater, container, false)

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val wifiManager = requireContext().getSystemService(
            Context.WIFI_SERVICE
        ) as WifiManager

        val suggestion = WifiNetworkSuggestion.Builder()
            .setSsid(Config.WIFI_SSID)
            .setWpa2Passphrase(Config.WIFI_PASSWORD)
            .build()

        val status = wifiManager.addNetworkSuggestions(listOf(suggestion));
        if (status != WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS)
            return

        val intentFilter = IntentFilter(
            WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION
        )

        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (
                    !intent.action
                        .equals(
                            WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION
                        )
                ) { return }

                binding.wifiSuggestConnected.visibility = View.VISIBLE
            }
        }

        requireContext().registerReceiver(
            broadcastReceiver,
            intentFilter
        )
    }
}