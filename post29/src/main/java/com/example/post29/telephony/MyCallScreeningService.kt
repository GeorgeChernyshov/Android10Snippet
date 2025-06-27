package com.example.post29.telephony

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.Q)
class MyCallScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val phoneNumberUri = callDetails.handle
        val phoneNumber = phoneNumberUri?.schemeSpecificPart
        val presentation = callDetails.callDirection

        Log.i(TAG, "onScreenCall for number: $phoneNumber, " +
                "URI: $phoneNumberUri, " +
                "Direction: ${callDetails.callDirection}"
        )

        val isSpam: Boolean
        val callerName: String?

        if (phoneNumber == CallScreeningRepository.spamNumber) {
            isSpam = true
            callerName = "Suspected Spam Inc."
        } else {
            isSpam = false
            callerName = "Friendly Caller LLC"
        }

        CallScreeningRepository.setCallInfo(
            CallInfo(
                phoneNumberUri = phoneNumberUri,
                phoneNumber = phoneNumber,
                callerName = callerName,
                callDirection = presentation,
                isSpam = isSpam
            )
        )

        val responseBuilder = CallResponse.Builder()

        if (isSpam) {
            Log.i(TAG, "Call from $phoneNumber identified as SPAM.")
            responseBuilder
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(false)
        } else {
            Log.i(TAG, "Call from $phoneNumber identified as NOT SPAM.")
            responseBuilder
                .setDisallowCall(false)
                .setRejectCall(false)
                .setSkipCallLog(false)
                .setSkipNotification(false)
        }

        respondToCall(callDetails, responseBuilder.build())
    }

    companion object {
        const val TAG = "MyCallScreeningService"
    }
}