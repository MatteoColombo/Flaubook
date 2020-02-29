package it.speedcubing.flaubook.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import it.speedcubing.flaubook.R

class NotificationBuilder(private val context: Context) {

    private var platformNotificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val playAction = NotificationCompat.Action(
        R.drawable.notification_play,
        context.getString(R.string.play),
        MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PLAY)
    )
    private val pauseAction = NotificationCompat.Action(
        R.drawable.notification_pause   ,
        context.getString(R.string.pause),
        MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_PAUSE)
    )

    private val rewindAction = NotificationCompat.Action(
        R.drawable.replay_10,
        context.getString(R.string.replay_10),
        MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_REWIND)
    )

    private val forwardAction = NotificationCompat.Action(
        R.drawable.forward_10,
        context.getString(R.string.forward_10),
        MediaButtonReceiver.buildMediaButtonPendingIntent(
            context,
            PlaybackStateCompat.ACTION_FAST_FORWARD
        )
    )

    private val stopPendingIntent =
        MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP)


    fun buildNotification(token: MediaSessionCompat.Token): Notification {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !channelExists()) {
            createNotificationChannel()
        }

        val controller = MediaControllerCompat(context, token)
        val meta = controller.metadata
        val playbackState = controller.playbackState

        val builder = NotificationCompat.Builder(
            context,
            NOTIFICATION_CHANNEL_ID
        )

        builder.addAction(rewindAction)
        if (playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
            builder.addAction(pauseAction)
        } else {
            builder.addAction(playAction)
        }

        builder.addAction(forwardAction)


        val style = MediaStyle()
            .setCancelButtonIntent(controller.sessionActivity)
            .setMediaSession(token)
            .setCancelButtonIntent(stopPendingIntent)
            .setShowCancelButton(true)
            .setShowActionsInCompactView(0,1)

        return builder.setContentIntent(controller.sessionActivity)
            .setContentTitle(meta.album)
            .setContentText(meta.title)
            .setLargeIcon(meta.description.iconBitmap)
            .setOnlyAlertOnce(true)
            .setAutoCancel(false)
            .setDeleteIntent(stopPendingIntent)
            .setSmallIcon(R.drawable.notification_icon)
            .setColor(ContextCompat.getColor(context,android.R.color.white))
            .setStyle(style)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

    }

    private fun channelExists() = platformNotificationManager.getNotificationChannel(
        NOTIFICATION_CHANNEL_ID
    ) != null

    private fun createNotificationChannel() {

        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.notification_channel_description)
        }

        platformNotificationManager.createNotificationChannel(notificationChannel)
    }


}

private const val NOTIFICATION_CHANNEL_ID = "flaubook_player_channel"