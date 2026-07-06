package com.tvxargtec.online.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tvxargtec.online.database.AppDatabase
import com.tvxargtec.online.database.entity.FavoriteEntity
import com.tvxargtec.online.database.entity.HistoryEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

interface BackupCallback {
    fun onComplete(success: Boolean, message: String)
}

class BackupManager(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val baseUrl = "https://apitvxargtec.duckdns.org/"

    fun backup(callback: BackupCallback) {
        val auth = AuthManager.getInstance(context)
        val token = auth.getToken()
        if (token == null) {
            callback.onComplete(false, "No hay sesion activa")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val favorites = db.favoriteDao().getAllFavorites()
                val history = db.historyDao().getAllHistory()
                val customCategories = CustomCategoryManager(context).getCategories()

                val payload = buildBackupPayload(favorites, history, customCategories)

                val mediaType = "application/json".toMediaType()
                val body = payload.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url("${baseUrl}api/backup")
                    .header("Authorization", "Bearer $token")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    callback.onComplete(true, "Respaldo completado exitosamente")
                } else {
                    callback.onComplete(false, "Error del servidor: ${response.code}")
                }
            } catch (e: Exception) {
                callback.onComplete(false, "Error de conexion: ${e.message}")
            }
        }
    }

    fun restore(callback: BackupCallback) {
        val auth = AuthManager.getInstance(context)
        val token = auth.getToken()
        if (token == null) {
            callback.onComplete(false, "No hay sesion activa")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder()
                    .url("${baseUrl}api/backup")
                    .header("Authorization", "Bearer $token")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    callback.onComplete(false, "Error del servidor: ${response.code}")
                    return@launch
                }

                val body = response.body?.string() ?: run {
                    callback.onComplete(false, "Respuesta vacia del servidor")
                    return@launch
                }

                val json = JSONObject(body)
                val data = json.optJSONObject("data") ?: run {
                    callback.onComplete(false, "Formato de respaldo invalido")
                    return@launch
                }

                val db = AppDatabase.getInstance(context)

                val favArray = data.optJSONArray("favorites")
                if (favArray != null) {
                    db.favoriteDao().clearAllFavorites()
                    for (i in 0 until favArray.length()) {
                        val favJson = favArray.getJSONObject(i)
                        val entity = FavoriteEntity(favJson.getString("contentId"))
                        if (favJson.has("addedAt")) {
                            entity.addedAt = favJson.getLong("addedAt")
                        }
                        db.favoriteDao().addFavorite(entity)
                    }
                }

                val histArray = data.optJSONArray("history")
                if (histArray != null) {
                    db.historyDao().clearAllHistory()
                    for (i in 0 until histArray.length()) {
                        val histJson = histArray.getJSONObject(i)
                        val entity = HistoryEntity(
                            histJson.getString("contentId"),
                            histJson.optInt("watchProgress", 0)
                        )
                        if (histJson.has("lastWatched")) {
                            entity.lastWatched = histJson.getLong("lastWatched")
                        }
                        db.historyDao().insertHistory(entity)
                    }
                }

                val catArray = data.optJSONArray("customCategories")
                if (catArray != null) {
                    val type = object : TypeToken<List<CustomCategory>>() {}.type
                    val categories: List<CustomCategory> = gson.fromJson(catArray.toString(), type)
                    CustomCategoryManager(context).saveCategories(categories)
                }

                callback.onComplete(true, "Restauracion completada exitosamente")
            } catch (e: Exception) {
                callback.onComplete(false, "Error de conexion: ${e.message}")
            }
        }
    }

    private fun buildBackupPayload(
        favorites: List<FavoriteEntity>,
        history: List<HistoryEntity>,
        customCategories: List<CustomCategory>
    ): JSONObject {
        val favArray = JSONArray()
        for (fav in favorites) {
            val obj = JSONObject()
            obj.put("contentId", fav.contentId)
            obj.put("addedAt", fav.addedAt)
            favArray.put(obj)
        }

        val histArray = JSONArray()
        for (h in history) {
            val obj = JSONObject()
            obj.put("contentId", h.contentId)
            obj.put("watchProgress", h.watchProgress)
            obj.put("lastWatched", h.lastWatched)
            histArray.put(obj)
        }

        val catArray = JSONArray()
        for (cat in customCategories) {
            val obj = JSONObject()
            obj.put("id", cat.id)
            obj.put("name", cat.name)
            obj.put("channelIds", JSONArray(cat.channelIds.toList()))
            obj.put("color", cat.color)
            obj.put("createdAt", cat.createdAt)
            catArray.put(obj)
        }

        val payload = JSONObject()
        payload.put("favorites", favArray)
        payload.put("history", histArray)
        payload.put("customCategories", catArray)
        payload.put("version", "1.0")
        payload.put("timestamp", System.currentTimeMillis())
        return payload
    }
}
