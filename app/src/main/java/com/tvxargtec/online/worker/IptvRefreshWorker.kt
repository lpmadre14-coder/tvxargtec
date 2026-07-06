package com.tvxargtec.online.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tvxargtec.online.utils.Channel
import com.tvxargtec.online.utils.ChannelDataManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class IptvRefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            val channels: List<Channel> = suspendCoroutine { continuation ->
                ChannelDataManager.fetchRemoteM3USources(applicationContext, object : ChannelDataManager.DataCallback {
                    override fun onDataLoaded(channels: List<Channel>) {
                        continuation.resume(channels)
                    }

                    override fun onError(e: Exception) {
                        continuation.resumeWithException(e)
                    }
                })
            }
            android.util.Log.i("IptvRefreshWorker", "Refreshed ${channels.size} channels")
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("IptvRefreshWorker", "Refresh failed", e)
            Result.failure()
        }
    }
}
