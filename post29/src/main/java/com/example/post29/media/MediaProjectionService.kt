package com.example.post29.media

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.projection.MediaProjection
import android.os.Binder
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.example.post29.Config
import com.example.post29.Post29Application.Companion.NOTIFICATION_CHANNEL
import com.example.post29.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MediaProjectionService : Service() {

    private var audioRecord: AudioRecord? = null
    private var audioCaptureThread: Thread? = null
    private var mediaProjection: MediaProjection? = null
        set(value) {
            field = value
            startAudioCapture()
        }

    private val binder = MediaProjectionServiceBinder()
    private val notificationHelper = MediaNotificationHelper(this)

    private val isRecording
        get() = (binder.state == MediaProjectionServiceState.RECORDING)

    override fun onBind(intent: Intent?) = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = ActionValues.from(intent?.action)
        when (action) {
            ActionValues.START -> startService()
            ActionValues.STOP -> stopService()
            else -> {}
        }

        return START_NOT_STICKY
    }

    private fun startService() = startForeground(
        254,
        notificationHelper.createSimpleNotification()
    )

    private fun stopService() {
        stopAudioCapture()
        mediaProjection?.stop()
        mediaProjection = null

        stopForeground(true)
        stopSelf()
    }

    private fun startAudioCapture() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (isRecording) return

            val projection = mediaProjection ?: return

            val config = AudioPlaybackCaptureConfiguration.Builder(projection)
                .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                .addMatchingUsage(AudioAttributes.USAGE_GAME)
                .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setEncoding(AUDIO_FORMAT)
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(CHANNEL_CONFIG)
                .build()

            val minBufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT
            )

            val bufferSize = minBufferSize * 2

            audioRecord = AudioRecord.Builder()
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(bufferSize)
                .setAudioPlaybackCaptureConfig(config)
                .build()

            audioRecord?.startRecording()
            binder.state = MediaProjectionServiceState.RECORDING
            audioCaptureThread = Thread {
                val file = File(
                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                    Config.MEDIA_FILE
                )

                if (!file.exists())
                    file.createNewFile()

                val audioOutputStream = FileOutputStream(file)
                val capturedAudioSamples = ByteArray(bufferSize)
                while (isRecording) {
                    val bytesRead = audioRecord?.read(
                        capturedAudioSamples,
                        0,
                        bufferSize
                    )

                    if (
                        bytesRead != null &&
                        bytesRead != AudioRecord.ERROR_INVALID_OPERATION
                    ) {
                        audioOutputStream.write(
                            capturedAudioSamples,
                            0,
                            bytesRead
                        )
                    }
                }

                binder.state = MediaProjectionServiceState.READY
                audioOutputStream.close()
            }

            audioCaptureThread?.start()
        }
    }

    private fun stopAudioCapture() {
        if (
            !isRecording &&
            audioRecord == null &&
            mediaProjection == null
        ) {
            return
        }

        binder.state = MediaProjectionServiceState.STOPPED

        audioCaptureThread?.join(500)
        audioCaptureThread = null
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    enum class ActionValues(val actionName: String) {
        START("start"),
        STOP("stop");

        companion object {
            fun from(actionName: String?): ActionValues? {
                return ActionValues.values()
                    .find { it.actionName == actionName }
            }
        }
    }

    inner class MediaProjectionServiceBinder : Binder() {
        var state = MediaProjectionServiceState.DEFAULT
            set(value) {
                field = value
                notifyListener()
            }

        private var listener: ServiceStateListener? = null
        private val mainThreadHandler = Handler(Looper.getMainLooper())

        fun setMediaProjection(mediaProjection: MediaProjection?) {
            this@MediaProjectionService.mediaProjection = mediaProjection
        }

        fun setStateListener(listener: ServiceStateListener) {
            this.listener = listener
        }

        fun removeStateListener() {
            this.listener = null
        }

        private fun notifyListener() {
            mainThreadHandler.post {
                listener?.onStateChanged(state)
            }
        }
    }

    interface ServiceStateListener {
        fun onStateChanged(newState: MediaProjectionServiceState)
    }

    enum class MediaProjectionServiceState {
        DEFAULT, RECORDING, STOPPED, READY;
    }

    object Extras {
        const val ACTION = "action"
        const val DATA = "data"
        const val RESULT_CODE = "resultCode"
    }

    companion object {
        private const val CAPTURE_FILENAME = "audio_capture.pcm"
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val SAMPLE_RATE = 44100 // Hz
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    }
}