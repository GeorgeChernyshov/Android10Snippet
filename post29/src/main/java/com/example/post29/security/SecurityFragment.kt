package com.example.post29.security

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.post29.R
import com.example.post29.databinding.FragmentSecurityBinding
import javax.net.ssl.SSLContext

class SecurityFragment : Fragment() {

    private lateinit var binding: FragmentSecurityBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSecurityBinding.inflate(inflater, container, false)

        with(binding) {
            tlsCypherSuiteChoiceHint.text = getString(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    R.string.security_tls_cypher_suite_static
                else R.string.security_tls_cypher_suite_custom
            )
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val protocols = SSLContext.getDefault()
            .defaultSSLParameters
            .protocols

        with(binding) {
            tlsVersionsTextView.text = protocols.joinToString(", ")
            tlsConnectionButton.setOnClickListener {
                val task = TLSRequestTask()
                task.execute()

                tlsConnectionResultLayout.isVisible = true
                tlsConnectionResultTextView.text = task.get()
                tlsProtocolTypeTextView.text = task.handshakeCompletedListener.protocol
                tlsCypherSuiteTextView.text = task.handshakeCompletedListener.cipherSuite
            }
        }
    }
}