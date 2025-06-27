package com.example.post29.identifiers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.system.OsConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.post29.MainActivity
import com.example.post29.R
import com.example.post29.databinding.FragmentIdentifiersBinding
import java.net.InetSocketAddress

class IdentifiersFragment : Fragment() {

    private lateinit var binding: FragmentIdentifiersBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIdentifiersBinding.inflate(inflater, container, false)

        requestPermission(Manifest.permission.READ_PHONE_STATE)

        with (binding) {
            Thread {
                val connectionOwnerUid = getConnectionOwnerUid()
                val serial = getBuildSerial()
                Handler(Looper.getMainLooper()).post {
                    connectionUidTextView.text = connectionOwnerUid
                    serialTextView.text = serial
                }
            }

            goToNextScreenButton.setOnClickListener {
                findNavController().navigate(
                    R.id.action_IdentifiersFragment_to_PrivacyChangesFragment
                )
            }
        }

        return binding.root
    }

    private fun requestPermission(permission: String) {
        if (
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val mainActivity = requireActivity() as MainActivity
            mainActivity.requestPermissionLauncher.launch(permission)
        }
    }

    private fun getBuildSerial(): String {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            getString(R.string.error_build_serial_not_implemented)
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            try {
                Build.getSerial()
            }
            catch (ex: SecurityException) {
                ex.message.toString()
            }
        } else getString(R.string.error_build_serial_not_available)
    }

    private fun getConnectionOwnerUid(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val connectivityManager = requireActivity()
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

                connectivityManager.getConnectionOwnerUid(
                    OsConstants.IPPROTO_TCP,
                    InetSocketAddress(1234),
                    InetSocketAddress(5050)
                ).toString()
            }
            catch (ex: SecurityException) {
                ex.message.toString()
            }
        } else {
            getString(R.string.error_get_uid_not_available)
        }
    }
}