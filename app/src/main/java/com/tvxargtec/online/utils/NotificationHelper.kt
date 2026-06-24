package com.tvxargtec.online.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tvxargtec.online.R
import com.tvxargtec.online.activity.MainAty
import com.tvxargtec.online.activity.PlayAty

object NotificationHelper {

    const val CHANNEL_ALERTS = "tvxargtec_alerts"
    const val CHANNEL_NEW_CONTENT = "tvxargtec_new_content"
    const val CHANNEL_PROMOS = "tvxargtec_promos"
    const val CHANNEL_SYSTEM = "tvxargtec_system"
    const val CHANNEL_AUDIO = "audio_playback"

    private const val NOTIFICATION_ID_BASE = 1000
    private var notificationCounter = 0

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channels = listOf(
            NotificationChannel(
                CHANNEL_ALERTS,
                "Alertas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones importantes del sistema"
                enableVibration(true)
                enableLights(true)
            },
            NotificationChannel(
                CHANNEL_NEW_CONTENT,
                "Nuevo contenido",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Nuevos canales, películas y series agregadas"
            },
            NotificationChannel(
                CHANNEL_PROMOS,
                "Promociones",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ofertas y descuentos VIP"
            },
            NotificationChannel(
                CHANNEL_SYSTEM,
                "Sistema",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Notificaciones del sistema"
            },
            NotificationChannel(
                CHANNEL_AUDIO,
                "Reproducción de audio",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Reproducción de audio en segundo plano"
                setSound(null, null)
            }
        )

        channels.forEach { manager.createNotificationChannel(it) }
    }

    fun showNotification(
        context: Context,
        title: String,
        message: String,
        channelId: String = CHANNEL_ALERTS,
        contentUrl: String? = null,
        smallIcon: Int = android.R.drawable.ic_dialog_info
    ) {
        val intent = if (contentUrl != null) {
            Intent(context, PlayAty::class.java).apply {
                putExtra("url", contentUrl)
                putExtra("title", title)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        } else {
            Intent(context, MainAty::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            context, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                NOTIFICATION_ID_BASE + notificationCounter++,
                notification
            )
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }

    fun showPromoNotification(context: Context, title: String, message: String) {
        showNotification(context, title, message, CHANNEL_PROMOS)
    }

    fun showContentNotification(
        context: Context,
        title: String,
        message: String,
        contentUrl: String
    ) {
        showNotification(context, title, message, CHANNEL_NEW_CONTENT, contentUrl)
    }
}
