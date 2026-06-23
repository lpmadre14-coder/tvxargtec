package com.tvxargtec.online.service

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tvxargtec.online.utils.NotificationHelper
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        var latestToken: String? = null
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        latestToken = token
        Log.d(TAG, "Nuevo FCM token: $token")
        sendTokenToServer(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Mensaje recibido de: ${message.from}")

        // Notificación con datos estructurados
        message.data.let { data ->
            if (data.isNotEmpty()) {
                handleDataMessage(data)
                return
            }
        }

        // Notificación desde consola Firebase (título + cuerpo)
        message.notification?.let { notification ->
            handleNotificationMessage(
                notification.title ?: "Tvxargtec",
                notification.body ?: ""
            )
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"] ?: "alert"
        val title = data["title"] ?: "Tvxargtec"
        val body = data["body"] ?: ""
        val url = data["url"]
        val imageUrl = data["image"]

        when (type) {
            "content" -> {
                if (url != null) {
                    NotificationHelper.showContentNotification(this, title, body, url)
                } else {
                    NotificationHelper.showNotification(this, title, body)
                }
            }
            "promo" -> {
                NotificationHelper.showPromoNotification(this, title, body)
            }
            "live" -> {
                NotificationHelper.showNotification(
                    this, "🔴 EN VIVO: $title", body,
                    NotificationHelper.CHANNEL_ALERTS, url
                )
            }
            else -> {
                NotificationHelper.showNotification(this, title, body)
            }
        }
    }

    private fun handleNotificationMessage(title: String, body: String) {
        NotificationHelper.showNotification(this, title, body)
    }

    private fun sendTokenToServer(token: String) {
        // TODO: Enviar token al backend para asociarlo al usuario
        // ApiService.post("/fcm/register", mapOf("token" to token))
        Log.d(TAG, "Token listo para enviar al servidor: $token")
    }
}
