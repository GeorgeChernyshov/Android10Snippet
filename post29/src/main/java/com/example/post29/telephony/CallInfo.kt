package com.example.post29.telephony

import android.net.Uri

data class CallInfo(
    val phoneNumberUri: Uri?,
    val phoneNumber: String?,
    val callerName: String?,
    val callDirection: Int,
    val isSpam: Boolean
)