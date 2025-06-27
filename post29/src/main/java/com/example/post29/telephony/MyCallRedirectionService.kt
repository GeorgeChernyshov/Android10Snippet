package com.example.post29.telephony

import android.net.Uri
import android.os.Build
import android.telecom.CallRedirectionService
import android.telecom.PhoneAccountHandle
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.Q)
class MyCallRedirectionService : CallRedirectionService() {

    override fun onPlaceCall(
        handle: Uri,
        initialPhoneAccount: PhoneAccountHandle,
        allowInteractiveResponse: Boolean
    ) {
        val originalNumber = handle.schemeSpecificPart
        if (originalNumber == "0987654321") {
            cancelCall()

            return
        }

        if (originalNumber == "1234567890") {
            val newNumber = CallRedirectionRepository.phoneNumber
            if (newNumber != null) {
                val newHandle = Uri.fromParts(
                    handle.scheme,
                    newNumber,
                    handle.fragment
                )

                redirectCall(
                    newHandle,
                    initialPhoneAccount,
                    false
                )

                return
            }
        }

        placeCallUnmodified()
    }

    companion object {
        const val TAG = "MyCallRedirectionSvc"
    }
}