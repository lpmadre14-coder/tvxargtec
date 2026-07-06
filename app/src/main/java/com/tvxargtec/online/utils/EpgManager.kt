package com.tvxargtec.online.utils

import android.content.Context
import com.tvxargtec.online.database.AppDatabase
import com.tvxargtec.online.database.entity.EpgProgrammeEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class EpgManager(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val dao = db.epgDao()
    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val CACHE_HOURS = 24
    }

    fun getCurrentProgramme(channelId: String, callback: (EpgProgrammeEntity?) -> Unit) {
        scope.launch {
            val now = System.currentTimeMillis()
            val programme = dao.getCurrentProgramme(channelId, now)
            withContext(Dispatchers.Main) { callback(programme) }
        }
    }

    fun getNextProgramme(channelId: String, callback: (EpgProgrammeEntity?) -> Unit) {
        scope.launch {
            val now = System.currentTimeMillis()
            val programme = dao.getNextProgramme(channelId, now)
            withContext(Dispatchers.Main) { callback(programme) }
        }
    }

    fun getProgrammesInRange(
        channelId: String,
        from: Long,
        to: Long,
        callback: (List<EpgProgrammeEntity>) -> Unit
    ) {
        scope.launch {
            val programmes = dao.getProgrammesInRange(channelId, from, to)
            withContext(Dispatchers.Main) { callback(programmes) }
        }
    }

    fun cacheProgrammes(programmes: List<EpgProgrammeEntity>) {
        scope.launch {
            dao.insertProgrammes(programmes)
        }
    }

    fun isCacheStale(cacheTime: Long): Boolean {
        return System.currentTimeMillis() - cacheTime > CACHE_HOURS * 3600 * 1000
    }

    fun convertToEntity(
        channelId: String,
        title: String,
        description: String,
        category: String,
        startTimeStr: String,
        endTimeStr: String
    ): EpgProgrammeEntity? {
        return try {
            val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            fmt.timeZone = TimeZone.getTimeZone("UTC")
            val start = fmt.parse(startTimeStr)?.time ?: return null
            val end = fmt.parse(endTimeStr)?.time ?: return null
            EpgProgrammeEntity(
                channelId = channelId,
                title = title,
                description = description,
                category = category,
                startTime = start,
                endTime = end
            )
        } catch (e: Exception) {
            null
        }
    }

    fun cleanupOldProgrammes() {
        scope.launch {
            val before = System.currentTimeMillis() - 7 * 24 * 3600 * 1000L
            dao.deleteOldProgrammes(before)
        }
    }
}
