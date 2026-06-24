package com.tvxargtec.online.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.tvxargtec.online.R
import com.tvxargtec.online.activity.PlayAty

class AudioPlaybackService : Service() {

    private var player: ExoPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this).build()
        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    stopSelf()
                }
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val url = intent.getStringExtra(EXTRA_URL) ?: return START_NOT_STICKY
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Tvxargtec"

                val notificationIntent = Intent(this, PlayAty::class.java).apply {
                    putExtra("url", url)
                    putExtra("title", title)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

                val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL)
                    .setContentTitle(title)
                    .setContentText("Reproduciendo en segundo plano")
                    .setSmallIcon(android.R.drawable.ic_media_play)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .build()

                startForeground(ONGOING_NOTIFICATION_ID, notification)

                player?.apply {
                    setMediaItem(MediaItem.fromUri(Uri.parse(url)))
                    prepare()
                    play()
                }
            }
            ACTION_STOP -> {
                player?.stop()
                player?.release()
                player = null
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        player?.release()
        player = null
        super.onDestroy()
    }

    companion object {
        private const val NOTIFICATION_CHANNEL = "audio_playback"
        private const val ONGOING_NOTIFICATION_ID = 1001
        private const val ACTION_PLAY = "com.tvxargtec.online.action.PLAY"
        private const val ACTION_STOP = "com.tvxargtec.online.action.STOP"
        private const val EXTRA_URL = "url"
        private const val EXTRA_TITLE = "title"

        @JvmStatic fun start(context: Context, url: String, title: String) {
            val intent = Intent(context, AudioPlaybackService::class.java).apply {
                action = ACTION_PLAY
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_TITLE, title)
            }
            context.startForegroundService(intent)
        }

        @JvmStatic fun stop(context: Context) {
            val intent = Intent(context, AudioPlaybackService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
