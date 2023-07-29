package com.example.pre29

import android.app.Service
import android.content.ClipboardManager
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import java.io.FileDescriptor

class ClipboardService : Service() {

    private lateinit var clipboardManager: ClipboardManager

    var currentClip: String? = null
    var test = "Test"

    private val binder = ClipboardBinder()

    override fun onCreate() {
        super.onCreate()

        clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.addPrimaryClipChangedListener(ClipboardListener())
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class ClipboardBinder : Binder() {
        fun getClipboardData(): String? {
            return currentClip
        }
    }

    inner class ClipboardListener() : ClipboardManager.OnPrimaryClipChangedListener {
        override fun onPrimaryClipChanged() {
            currentClip = clipboardManager.primaryClip
                ?.getItemAt(0)
                ?.text
                ?.toString()
        }
    }
}