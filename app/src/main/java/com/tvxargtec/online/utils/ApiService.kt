package com.tvxargtec.online.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

object ApiService {

    private const val BASE_URL = "https://apitvxargtec.duckdns.org/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    suspend fun getCategories(): List<Category> = try {
        apiCall("api/content")
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun getChannels(categoryId: String = ""): List<Channel> = try {
        val endpoint = if (categoryId.isEmpty()) "api/content" else "api/content?category=$categoryId"
        apiCall(endpoint)
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun searchChannels(query: String): List<Channel> = try {
        apiCall("api/content?search=$query")
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun login(email: String, password: String): User? = try {
        val body = mapOf("email" to email, "password" to password)
        apiCall("api/login", body)
    } catch (e: Exception) {
        null
    }

    suspend fun register(email: String, password: String): User? = try {
        val body = mapOf("email" to email, "password" to password)
        apiCall("api/register", body)
    } catch (e: Exception) {
        null
    }

    suspend fun getVipPlans(): List<VipPlan> = try {
        apiCall("api/vip/plans")
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun getEpgNow(channelName: String): EpgNowResponse? = try {
        val endpoint = "api/epg?channel_name=${java.net.URLEncoder.encode(channelName, "UTF-8")}"
        withContext(Dispatchers.IO) {
            val url = BASE_URL + endpoint
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
            if (response.isSuccessful && body != null) {
                gson.fromJson(body, EpgNowResponse::class.java)
            } else null
        }
    } catch (e: Exception) {
        null
    }

    private suspend inline fun <reified T> apiCall(endpoint: String, body: Map<String, String>? = null): T {
        return withContext(Dispatchers.IO) {
            val url = BASE_URL + endpoint
            val requestBuilder = Request.Builder().url(url)

            if (body != null) {
                val json = gson.toJson(body)
                requestBuilder.post(okhttp3.RequestBody.create(
                    "application/json".toMediaType(), json
                ))
            }

            val response = client.newCall(requestBuilder.build()).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody == null) {
                throw IOException("Error $endpoint: ${response.code}")
            }

            val apiResponse: ApiResponse<T> = gson.fromJson(responseBody, object : TypeToken<ApiResponse<T>>() {}.type)
            apiResponse.data ?: throw IOException("Empty response")
        }
    }
}
