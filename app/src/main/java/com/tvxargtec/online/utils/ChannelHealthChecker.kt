package com.tvxargtec.online.utils

import android.os.Handler
import android.os.Looper
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object ChannelHealthChecker {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()

    fun checkChannel(url: String, callback: (Boolean) -> Unit) {
        val request = Request.Builder()
            .url(url)
            .head()
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                Handler(Looper.getMainLooper()).post { callback(false) }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.close()
                val alive = response.isSuccessful
                Handler(Looper.getMainLooper()).post { callback(alive) }
            }
        })
    }
}
