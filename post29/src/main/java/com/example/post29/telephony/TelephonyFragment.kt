package com.example.post29.telephony

import android.app.Activity.RESULT_OK
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.post29.R
import com.example.post29.databinding.FragmentTelephonyBinding

@RequiresApi(Build.VERSION_CODES.Q)
class TelephonyFragment : Fragment() {

    private lateinit var binding: FragmentTelephonyBinding
    private lateinit var requestRoleLauncher: ActivityResultLauncher<Intent>

    private val roleManager by lazy {
        requireContext().getSystemService(
            Context.ROLE_SERVICE
        ) as RoleManager
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTelephonyBinding.inflate(inflater, container, false)
        requestRoleLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                checkCallScreeningRole()
                checkCallRedirectionRole()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        checkCallScreeningRole()
        checkCallRedirectionRole()

        with (binding) {
            enableTelephonyButton.setOnClickListener {
                requestCallScreeningRole()
            }

            enableRedirectionButton.setOnClickListener {
                requestCallRedirectionRole()
            }

            markSpamEditText.doOnTextChanged { text, _, _, _ ->
                CallScreeningRepository.spamNumber = text.toString()
            }

            redirectionNumberEditText.doOnTextChanged { text, _, _, _ ->
                CallRedirectionRepository.phoneNumber = text.toString()
            }

            goToNextScreenButton.setOnClickListener {
                findNavController().navigate(
                    R.id.action_TelephonyFragment_to_MediaFragment
                )
            }

            CallScreeningRepository.callInfo
                .observe(viewLifecycleOwner) { info ->
                    if (info != null) {
                        callInfoLayout.visibility = View.VISIBLE

                        phoneNumberUriTextView.text = getString(
                            R.string.telephony_call_info_phone_number_uri,
                            info.phoneNumberUri
                        )

                        phoneNumberTextView.text = getString(
                            R.string.telephony_call_info_phone_number,
                            info.phoneNumber
                        )

                        callerNameTextView.text = getString(
                            R.string.telephony_call_info_caller_name,
                            info.callerName
                        )

                        callDirectionTextView.text = if (
                            info.callDirection == Call.Details.DIRECTION_INCOMING
                        ) {
                            getString(R.string.telephony_call_info_call_incoming)
                        } else {
                            getString(R.string.telephony_call_info_call_outgoing)
                        }

                        isSpamTextView.text = getString(
                            R.string.telephony_call_info_is_spam,
                            info.isSpam.toString()
                        )
                    } else {
                        callInfoLayout.visibility = View.GONE
                    }
                }
        }
    }

    private fun checkCallScreeningRole() {
        with(binding) {
            if (
                roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) &&
                !roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
            ) {
                enableTelephonyLayout.visibility = View.VISIBLE
                telephonyLayout.visibility = View.GONE
            } else {
                enableTelephonyLayout.visibility = View.GONE
                telephonyLayout.visibility = View.VISIBLE
            }
        }
    }

    private fun requestCallScreeningRole() {
        if (
            roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) &&
            !roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
        ) {
            val intent = roleManager.createRequestRoleIntent(
                RoleManager.ROLE_CALL_SCREENING
            )

            requestRoleLauncher.launch(intent)
        }
    }

    private fun checkCallRedirectionRole() {
        with(binding) {
            if (
                roleManager.isRoleAvailable(RoleManager.ROLE_CALL_REDIRECTION) &&
                !roleManager.isRoleHeld(RoleManager.ROLE_CALL_REDIRECTION)
            ) {
                enableRedirectionLayout.visibility = View.VISIBLE
                redirectionLayout.visibility = View.GONE
            } else {
                enableRedirectionLayout.visibility = View.GONE
                redirectionLayout.visibility = View.VISIBLE
            }
        }
    }

    private fun requestCallRedirectionRole() {
        if (
            roleManager.isRoleAvailable(RoleManager.ROLE_CALL_REDIRECTION) &&
            !roleManager.isRoleHeld(RoleManager.ROLE_CALL_REDIRECTION)
        ) {
            val intent = roleManager.createRequestRoleIntent(
                RoleManager.ROLE_CALL_REDIRECTION
            )

            requestRoleLauncher.launch(intent)
        }
    }
}