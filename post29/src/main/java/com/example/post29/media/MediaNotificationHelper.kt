package com.example.post29.media

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import com.example.post29.Post29Application.Companion.NOTIFICATION_CHANNEL
import com.example.post29.R

class MediaNotificationHelper(private val context: Context) {
    fun createSimpleNotification(): Notification {
        val notificationBuilder = NotificationCompat
            .Builder(context, NOTIFICATION_CHANNEL)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_launcher_foreground)

        return notificationBuilder.build()
    }

    fun createMediaNotification(
        mediaSessionToken: MediaSessionCompat.Token,
        playbackState: PlaybackStateCompat,
        metadata: MediaMetadataCompat
    ) : Notification {
        val isPlaying = playbackState.state == PlaybackStateCompat.STATE_PLAYING ||
                playbackState.state == PlaybackStateCompat.STATE_BUFFERING

        return NotificationCompat
            .Builder(context, NOTIFICATION_CHANNEL)
            .setContentTitle(metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE) ?: "Unknown Title")
            .setContentText(metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST) ?: "Unknown Artist")
            .setSubText(metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setStyle(MediaStyle().setMediaSession(mediaSessionToken))
            .setOnlyAlertOnce(true)
            .setOngoing(isPlaying)
            .addAction(
                NotificationCompat.Action(
                    R.drawable.ic_play,
                    "Play",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackStateCompat.ACTION_PLAY
                    )
                )
            )
            .addAction(
                NotificationCompat.Action(
                    R.drawable.ic_pause,
                    "Pause",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackStateCompat.ACTION_PAUSE
                    )
                )
            )
            .build()
    }
}