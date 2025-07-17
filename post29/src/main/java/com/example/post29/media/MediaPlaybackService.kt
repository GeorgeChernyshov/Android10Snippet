package com.example.post29.media

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.post29.Config
import java.io.File

class MediaPlaybackService : MediaBrowserServiceCompat() {

    private lateinit var notificationHelper: MediaNotificationHelper

    private var mediaSession: MediaSessionCompat? = null
    private var audioData: ByteArray? = null
    private var audioTrack: AudioTrack? = null
    private var currentPlaybackPositionMs: Long = 0
    private var totalDurationMs: Long = 0
    private var playbackProgressUpdater: Runnable? = null
    private var playbackHandlerThread: HandlerThread? = null
    private var backgroundPlaybackHandler: Handler? = null
    private val mainThreadHandler = Handler(Looper.getMainLooper())
    private var isAudioPrepared = false
    private var isStarted = false

    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {

        override fun onPrepareFromMediaId(mediaId: String?, extras: Bundle?) {
            super.onPrepareFromMediaId(mediaId, extras)

            if (mediaId == MEDIA_ID) {
                backgroundPlaybackHandler?.post {
                    prepareAudioInternal()
                }
            }
        }

        override fun onPlay() {
            backgroundPlaybackHandler?.post {
                startPlaybackInternal()
            }
        }

        override fun onPause() {
            backgroundPlaybackHandler?.post {
                pausePlaybackInternal() // This method pauses AudioTrack
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "PlaybackService: onCreate - START")

        playbackHandlerThread = HandlerThread("AudioPlaybackThread").apply {
            start()
            backgroundPlaybackHandler = Handler(looper)
        }

        Log.d(TAG, "PlaybackService: HandlerThread initialized.")

        notificationHelper = MediaNotificationHelper(this)

        Log.d(TAG, "PlaybackService: MediaNotificationHelper initialized.")

        mediaSession = MediaSessionCompat(this, "MediaSession")
            .apply {
                setFlags(
                    MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
                )

                setCallback(mediaSessionCallback, backgroundPlaybackHandler)
                val initialState = PlaybackStateCompat.Builder()
                    .setActions(
                        PlaybackStateCompat.ACTION_PREPARE or
                        PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PLAY_PAUSE or
                        PlaybackStateCompat.ACTION_STOP
                    )
                    .setState(
                        PlaybackStateCompat.STATE_NONE,
                        0L,
                        1.0f
                    )
                    .build()

                setPlaybackState(initialState)

                Log.d(TAG, "PlaybackService: About to set session token.")
                if (this.sessionToken == null) {
                    Log.e(TAG, "PlaybackService: FATAL - Session token IS NULL before setting on service!")
                }

                this@MediaPlaybackService.sessionToken = this.sessionToken

                if (this@MediaPlaybackService.sessionToken == null) {
                    Log.e(TAG, "PlaybackService: FATAL - Service session token IS STILL NULL after assignment!")
                } else {
                    Log.d(TAG, "PlaybackService: Session token SET successfully on service: ${this@MediaPlaybackService.sessionToken}")
                }

                isActive = true
            }
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        if (mediaSession == null) {
            Log.e(TAG, "PlaybackService: onGetRoot - MediaSession is NULL.This is a problem!")
            return null // Cannot proceed without a session
        }

        if (mediaSession?.sessionToken == null) {
            Log.e(TAG, "PlaybackService: onGetRoot - MediaSession TOKEN is NULL. This is a problem!")
            // This implies setSessionToken(token) in onCreate might not have been effective or session init failed.
            return null
        }

        if (this.sessionToken == null) {
            Log.e(TAG, "PlaybackService: onGetRoot - Service's sessionToken field is NULL!")
            return null
        }

        return BrowserRoot(MEDIA_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {}

    private fun prepareAudioInternal() {
        val file = File(
            getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            Config.MEDIA_FILE
        )

        if (!file.exists())
            return

        this.audioData = file.readBytes()
        val mediaSessionCopy = mediaSession ?: return

        totalDurationMs = (audioData!!.size.toLong() * 1000) / (SAMPLE_RATE * 2)

        mediaSessionCopy.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, "Recorded Audio Sample")
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "My App")
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, totalDurationMs)
                .build()
        )

        mediaSessionCopy.isActive = true

        updatePlaybackState(
            PlaybackStateCompat.STATE_PAUSED,
            currentPlaybackPositionMs, // current position or 0L if starting fresh
            0f
        )
    }

    private fun startPlaybackInternal() {
        if (
            audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING
            && currentPlaybackPositionMs > 0
        ) {
            audioTrack?.play()
            mainThreadHandler.post {
                updatePlaybackState(
                    PlaybackStateCompat.STATE_PLAYING,
                    currentPlaybackPositionMs,
                    1.0f
                )

                startPlaybackProgressUpdater()
            }

            return
        }

        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null

        backgroundPlaybackHandler?.post {
            val audioDataCopy = audioData ?: return@post
            val minBufferSize = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT
            )

            val bufferSize = maxOf(minBufferSize, audioDataCopy.size)
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AUDIO_FORMAT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(CHANNEL_CONFIG)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack?.write(
                audioDataCopy,
                0,
                audioDataCopy.size
            )

            audioTrack?.play()
        }

        startPlaybackProgressUpdater()
    }

    private fun pausePlaybackInternal() {
        if (audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING)
            audioTrack?.pause()

        mainThreadHandler.post {
            updatePlaybackState(PlaybackStateCompat.STATE_PAUSED, currentPlaybackPositionMs, 0f)
            stopPlaybackProgressUpdater() // Stop updater when paused
        }
    }

    private fun updatePlaybackState(
        state: Int,
        positionMs: Long,
        playbackSpeed: Float
    ) {
        val mediaSessionCopy = mediaSession ?: return

        var actions = PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_SEEK_TO

        val playbackState = PlaybackStateCompat.Builder()
            .setActions(actions)
            .setState(
                state,
                positionMs,
                playbackSpeed
            )
            .build()

        mediaSessionCopy.setPlaybackState(playbackState)

        when (state) {
            PlaybackStateCompat.STATE_PLAYING -> {
                val notification = notificationHelper.createMediaNotification(
                    mediaSessionCopy.sessionToken,
                    playbackState,
                    mediaSessionCopy.controller.metadata
                )

                if (!isStarted) {
                    startForeground(NOTIFICATION_ID, notification)
                    isStarted = true
                } else {
                    NotificationManagerCompat.from(this)
                        .notify(NOTIFICATION_ID, notification)
                }
            }

            PlaybackStateCompat.STATE_PAUSED -> {
                val notification = notificationHelper.createMediaNotification(
                    mediaSessionCopy.sessionToken,
                    playbackState,
                    mediaSessionCopy.controller.metadata
                )

                NotificationManagerCompat.from(this)
                    .notify(NOTIFICATION_ID, notification)
            }
        }
    }

    private fun startPlaybackProgressUpdater() {
        stopPlaybackProgressUpdater()

        playbackProgressUpdater = object : Runnable {
            override fun run() {
                if (audioTrack?.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    val currentFrame = audioTrack?.playbackHeadPosition ?: 0
                    currentPlaybackPositionMs = (currentFrame.toLong() * 1000) / SAMPLE_RATE
                }

                mainThreadHandler.post {
                    updatePlaybackState(
                        PlaybackStateCompat.STATE_PLAYING,
                        currentPlaybackPositionMs,
                        1.0f
                    )
                }
            }
        }

        backgroundPlaybackHandler?.post(playbackProgressUpdater!!)
    }

    private fun stopPlaybackProgressUpdater() {
        playbackProgressUpdater?.let {
            backgroundPlaybackHandler?.removeCallbacks(it)
        }

        playbackProgressUpdater = null
    }

    companion object {
        const val MEDIA_ID = "MediaId"

        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO
        private const val NOTIFICATION_ID = 987
        private const val TAG = "MediaPlaybackService"
    }
}