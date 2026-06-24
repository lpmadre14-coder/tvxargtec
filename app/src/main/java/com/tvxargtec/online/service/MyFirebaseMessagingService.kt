package com.tvxargtec.online.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tvxargtec.online.utils.ApiClient
import com.tvxargtec.online.utils.AuthManager
import com.tvxargtec.online.utils.NotificationHelper
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

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

        message.data.let { data ->
            if (data.isNotEmpty()) {
                handleDataMessage(data)
                return
            }
        }

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
        try {
            val authManager = AuthManager.getInstance(this)
            val authToken = authManager.getToken()
            if (authToken == null) {
                Log.d(TAG, "Usuario no autenticado, saltando registro FCM")
                return
            }

            val json = JSONObject().apply {
                put("token", token)
            }

            val url = URL("https://apitvxargtec.duckdns.org/api/fcm/register")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer $authToken")
            conn.doOutput = true
            conn.connectTimeout = 15000
            conn.readTimeout = 15000

            OutputStreamWriter(conn.outputStream).use { writer ->
                writer.write(json.toString())
                writer.flush()
            }

            val responseCode = conn.responseCode
            Log.d(TAG, "FCM token registrado en servidor: $responseCode")
            conn.disconnect()
        } catch (e: Exception) {
            Log.e(TAG, "Error registrando FCM token: ${e.message}")
        }
    }
}
