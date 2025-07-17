package com.example.post29.media

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.post29.R
import com.example.post29.databinding.FragmentMediaBinding
import com.example.post29.media.MediaProjectionService.MediaProjectionServiceState

class MediaFragment : Fragment() {

    private lateinit var binding: FragmentMediaBinding
    private lateinit var mediaProjectionLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestAudioPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var notificationHelper: MediaNotificationHelper

    private var mediaProjectionManager: MediaProjectionManager? = null
    private var mediaProjection: MediaProjection? = null

    private var isRecording = false

    private var mediaBrowser: MediaBrowserCompat? = null
    private var mediaController: MediaControllerCompat? = null
    private var mediaProjectionBinder: MediaProjectionService.MediaProjectionServiceBinder? = null
        set(value) {
            value?.setStateListener(object : MediaProjectionService.ServiceStateListener {
                override fun onStateChanged(newState: MediaProjectionServiceState) {
                    isRecording = (newState == MediaProjectionServiceState.RECORDING)

                    with(binding) {
                        audioCaptureButton.text = if (newState == MediaProjectionServiceState.RECORDING)
                            getString(R.string.stop_capture)
                        else getString(R.string.start_capture)

                        sendAudioNotificationButton.visibility = if (newState == MediaProjectionServiceState.READY)
                            View.VISIBLE
                        else View.GONE
                    }
                }
            })

            field = value
        }

    private val mainThreadHandler = Handler(Looper.getMainLooper())

    private val projectionServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mediaProjectionBinder = service as MediaProjectionService.MediaProjectionServiceBinder
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mediaProjectionBinder = null
        }
    }

    private val playbackServiceConnectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()

            if (mediaBrowser?.sessionToken == null)
                return

            mediaController = MediaControllerCompat(
                requireActivity(),
                mediaBrowser!!.sessionToken
            )

            mediaController?.registerCallback(
                mediaControllerCallback,
                mainThreadHandler
            )
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()

            mediaController?.unregisterCallback(mediaControllerCallback)
            mediaController = null
        }
    }

    private val mediaControllerCallback = object : MediaControllerCompat.Callback() {
        override fun onSessionDestroyed() {
            super.onSessionDestroyed()

            mediaBrowser?.disconnect()
            mediaController = null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMediaBinding.inflate(inflater, container, false)

        mediaProjectionManager = requireContext().getSystemService(
            Context.MEDIA_PROJECTION_SERVICE
        ) as MediaProjectionManager

        mediaProjectionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                mediaProjection = mediaProjectionManager?.getMediaProjection(
                    result.resultCode,
                    result.data!!
                )

                mediaProjectionBinder?.setMediaProjection(mediaProjection)
            }
        }

        requestAudioPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted)
                requestMediaProjection()
        }

        notificationHelper = MediaNotificationHelper(requireContext())



        requireContext().bindService(
            Intent(
                requireContext(),
                MediaProjectionService::class.java
            ),
            projectionServiceConnection,
            Context.BIND_AUTO_CREATE
        )

        mediaBrowser = MediaBrowserCompat(
            requireActivity(),
            ComponentName(requireActivity(), MediaPlaybackService::class.java),
            playbackServiceConnectionCallback,
            null
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with (binding) {
            sharingAudioInputHint.text = getString(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                    R.string.media_audio_sharing_hint_post29
                else R.string.media_audio_sharing_hint_pre29
            )

            audioCaptureButton.setOnClickListener {
                if (isRecording)
                    stopAudioCapture()
                else checkPermissionsAndStart()
            }

            sendAudioNotificationButton.setOnClickListener {
                prepareAndShowMediaNotification()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (mediaBrowser?.isConnected == false)
            mediaBrowser?.connect()
    }

    private fun stopAudioCapture() {
        val intent = Intent(
            requireContext(),
            MediaProjectionService::class.java
        ).apply {
            action = MediaProjectionService.ActionValues.STOP.actionName
        }

        ContextCompat.startForegroundService(
            requireContext(),
            intent
        )
    }

    private fun checkPermissionsAndStart() {
        if (
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            requestMediaProjection()
        } else {
            requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun requestMediaProjection() {
        val serviceIntent = Intent(
            requireContext(),
            MediaProjectionService::class.java
        ).apply {
            action = MediaProjectionService.ActionValues.START.actionName
        }

        ContextCompat.startForegroundService(requireContext(), serviceIntent)

        mediaProjectionManager?.let {
            mediaProjectionLauncher.launch(it.createScreenCaptureIntent())
        }
    }

    private fun prepareAndShowMediaNotification() = mediaBrowser?.let { browser ->
        if (!browser.isConnected) return@let

        mediaController?.transportControls
            ?.prepareFromMediaId(
                MediaPlaybackService.MEDIA_ID,
                null
            )
    }
}