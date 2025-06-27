package com.example.post29.telephony

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object CallScreeningRepository {

    private val _callInfo = MutableLiveData<CallInfo?>(null)
    val callInfo: LiveData<CallInfo?> = _callInfo

    var spamNumber: String? = null

    fun setCallInfo(info: CallInfo) {
        _callInfo.value = info
    }
}