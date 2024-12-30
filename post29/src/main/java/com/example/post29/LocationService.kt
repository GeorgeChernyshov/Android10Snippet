package com.example.post29

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationRequestLA = LocationRequest().apply {
        interval = 60000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_LOW_POWER
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val a = result
        }
    }

    override fun onCreate() {
        super.onCreate()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        Handler(Looper.getMainLooper()).postDelayed({
            fusedLocationClient.requestLocationUpdates(
                locationRequestLA,
                locationCallback,
                Looper.getMainLooper()
            )
        }, 10000)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}