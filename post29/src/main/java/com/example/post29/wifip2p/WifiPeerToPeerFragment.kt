package com.example.post29.wifip2p

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.post29.Config
import com.example.post29.R
import com.example.post29.databinding.FragmentWifiP2pBinding

class WifiPeerToPeerFragment : Fragment() {

    private lateinit var binding: FragmentWifiP2pBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWifiP2pBinding.inflate(inflater, container, false)

        with(binding) {
            wifiP2pInputSsid.setText(Config.WIFI_SSID)
            wifiP2pInputPassword.setText(Config.WIFI_PASSWORD)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with (binding) {
            wifiP2pConnectToHotspotButton.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val ssid = wifiP2pInputSsid.text.toString()
                    val password = wifiP2pInputPassword.text.toString()

                    val peerToPeerConnector = PeerToPeerConnector(
                        context = requireContext(),
                        onNetworkEvent = { event ->
                            wifiP2pStatusTextView.text = event
                        }
                    )

                    peerToPeerConnector.requestConnection(ssid, password)
                }
            }

            goToNextScreenButton.setOnClickListener {
                findNavController().navigate(
                    R.id.action_WiFiP2PFragment_to_WiFiSuggestFragment
                )
            }
        }
    }
}