package com.example.pre29

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.pre29.admininfo.DeviceAdminReceiver
import com.example.pre29.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var deviceAdminSample: ComponentName
    lateinit var dpm: DevicePolicyManager

    var binder: ClipboardService.ClipboardBinder? = null
        private set

    val requestAdminLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        this.binding.root.invalidate()
    }

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
    }

    private val clipboardServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            binder = p1 as ClipboardService.ClipboardBinder
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            binder = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        deviceAdminSample = ComponentName(applicationContext, DeviceAdminReceiver::class.java)
        dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        setContentView(binding.root)

        Intent(this, ClipboardService::class.java).also {
            bindService(it, clipboardServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()

        unbindService(clipboardServiceConnection)
    }
}