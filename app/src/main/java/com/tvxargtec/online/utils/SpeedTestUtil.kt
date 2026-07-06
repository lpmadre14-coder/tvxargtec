package com.tvxargtec.online.utils

import android.os.Handler
import android.os.Looper
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

object SpeedTestUtil {

    private const val TEST_URL = "https://apitvxargtec.duckdns.org/api/health"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val mainHandler = Handler(Looper.getMainLooper())

    @JvmStatic
    fun measureSpeed(callback: Consumer<Double>) {
        val request = Request.Builder()
            .url(TEST_URL)
            .build()

        val startTime = System.nanoTime()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                mainHandler.post { callback.accept(0.0) }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use { resp ->
                    val body = resp.body
                    if (body == null) {
                        mainHandler.post { callback.accept(0.0) }
                        return
                    }

                    val source = body.source()
                    var totalBytes = 0L
                    val buffer = okio.Buffer()

                    try {
                        while (!source.exhausted()) {
                            val bytesRead = source.read(buffer, 8192)
                            if (bytesRead == -1L) break
                            totalBytes += bytesRead
                            buffer.clear()
                        }
                    } catch (_: Exception) {
                    }

                    val elapsedNs = System.nanoTime() - startTime
                    val elapsedSec = elapsedNs / 1_000_000_000.0

                    val speedMbps = if (elapsedSec > 0) {
                        (totalBytes * 8) / (elapsedSec * 1_000_000)
                    } else {
                        0.0
                    }

                    mainHandler.post { callback.accept(speedMbps) }
                }
            }
        })
    }

    @JvmStatic
    fun categorizeSpeed(speedMbps: Double): String {
        return when {
            speedMbps < 1.0 -> "low"
            speedMbps < 5.0 -> "medium"
            speedMbps < 15.0 -> "high"
            else -> "very_high"
        }
    }

    @JvmStatic
    fun getRecommendedQuality(speedMbps: Double): String {
        return when {
            speedMbps < 1.0 -> "480p"
            speedMbps < 5.0 -> "720p"
            speedMbps < 15.0 -> "1080p"
            else -> "4K"
        }
    }
}
