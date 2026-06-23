package com.tvxargtec.online.utils

import android.content.Context
import android.net.Uri
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.exoplayer.offline.DefaultDownloadIndex
import androidx.media3.exoplayer.offline.DefaultDownloaderFactory
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import java.io.File

class OfflineManager private constructor(private val context: Context) {

    private val downloadManager: DownloadManager
    private val cache: SimpleCache
    private val downloadTracker = mutableMapOf<String, Download>()
    private val callbacks = mutableMapOf<String, DownloadCallback>()

    interface DownloadCallback {
        fun onProgress(contentId: String, percent: Float)
        fun onCompleted(contentId: String)
        fun onFailed(contentId: String, error: String)
    }

    init {
        val cacheDir = File(context.cacheDir, "tvxargtec_offline")
        cache = SimpleCache(cacheDir, NoOpCacheEvictor())

        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent(Util.getUserAgent(context, "Tvxargtec"))
            .setConnectTimeoutMs(15000)
            .setReadTimeoutMs(15000)

        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(dataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        val databaseProvider = StandaloneDatabaseProvider(context)
        val downloadIndex = DefaultDownloadIndex(databaseProvider)
        val downloaderFactory = DefaultDownloaderFactory(cacheDataSourceFactory)
        downloadManager = DownloadManager(context, downloadIndex, downloaderFactory)

        downloadManager.addListener(object : DownloadManager.Listener {
            override fun onDownloadChanged(
                downloadManager: DownloadManager,
                download: Download,
                finalException: Exception?
            ) {
                downloadTracker[download.request.id] = download
                notifyListeners(download)
            }

            override fun onDownloadRemoved(
                downloadManager: DownloadManager,
                download: Download
            ) {
                downloadTracker.remove(download.request.id)
            }
        })
    }

    fun downloadContent(
        contentId: String,
        title: String,
        url: String,
        callback: DownloadCallback? = null
    ) {
        val downloadRequest = DownloadRequest.Builder(contentId, Uri.parse(url))
            .setCustomCacheKey(contentId)
            .setData(title.toByteArray())
            .build()

        downloadManager.addDownload(downloadRequest)

        if (callback != null) {
            callbacks[contentId] = callback
        }
    }

    fun getDownloads(): List<Download> {
        return downloadManager.currentDownloads
    }

    fun removeDownload(contentId: String) {
        downloadManager.removeDownload(contentId)
        callbacks.remove(contentId)
    }

    fun isDownloaded(contentId: String): Boolean {
        return downloadTracker.containsKey(contentId) &&
                downloadTracker[contentId]?.state == Download.STATE_COMPLETED
    }

    fun getProgress(contentId: String): Float {
        val download = downloadTracker[contentId] ?: return 0f
        if (download.state == Download.STATE_COMPLETED) return 1f
        if (download.contentLength > 0) {
            return download.bytesDownloaded.toFloat() / download.contentLength.toFloat()
        }
        return 0f
    }

    fun release() {
        downloadManager.release()
        cache.release()
    }

    private fun notifyListeners(download: Download) {
        val callback = callbacks[download.request.id] ?: return
        when (download.state) {
            Download.STATE_COMPLETED -> callback.onCompleted(download.request.id)
            Download.STATE_FAILED -> callback.onFailed(
                download.request.id,
                download.failureReason?.toString() ?: "Unknown error"
            )
            Download.STATE_DOWNLOADING -> callback.onProgress(
                download.request.id,
                getProgress(download.request.id)
            )
        }
    }

    companion object {
        private var instance: OfflineManager? = null

        @Synchronized
        fun getInstance(context: Context): OfflineManager {
            if (instance == null) {
                instance = OfflineManager(context.applicationContext)
            }
            return instance!!
        }
    }
}
