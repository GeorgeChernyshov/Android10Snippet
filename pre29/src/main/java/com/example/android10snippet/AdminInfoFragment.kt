package com.example.android10snippet

import android.app.admin.DeviceAdminReceiver
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.android10snippet.databinding.FragmentAdminInfoBinding
import java.net.NetworkInterface

class AdminInfoFragment : Fragment() {

    private lateinit var binding: FragmentAdminInfoBinding

    private val dpm get() = (requireActivity() as MainActivity).dpm

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminInfoBinding.inflate(inflater, container, false)

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            binding.actualMacAddressTextView.text = dpm.getWifiMacAddress(deviceAdminSample)
//        } else binding.actualMacAddressTextView.text = "dont care"

//        binding.usedMacAddressTextView.text = NetworkInterface.getNetworkInterfaces()
//            .toList()
//            .find { it.name == "wlan0" }
//            ?.hardwareAddress
//            ?.toString()

        dpm.lockNow()
        return binding.root
    }
}