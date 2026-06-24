package com.tvxargtec.online

import android.app.Application
import com.google.android.gms.cast.framework.CastContext
import com.tvxargtec.online.database.AppDatabase
import com.tvxargtec.online.database.entity.FavoriteEntity
import com.tvxargtec.online.utils.LocalDataManager
import com.tvxargtec.online.utils.NotificationHelper

class TvxargtecApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            NotificationHelper.createNotificationChannels(this)
        } catch (_: Exception) {}
        try {
            CastContext.getSharedInstance(this)
        } catch (_: Exception) {}
        try {
            migrateFavoritesToRoom()
        } catch (_: Exception) {}
    }

    private fun migrateFavoritesToRoom() {
        val prefs = getSharedPreferences("tvxargtec_data", MODE_PRIVATE)
        if (prefs.getBoolean("migrated_to_room", false)) return
        try {
            val localData = LocalDataManager(this)
            val oldFavs = localData.getFavorites()
            if (oldFavs.isNotEmpty()) {
                val db = AppDatabase.getInstance(this)
                for (item in oldFavs) {
                    db.favoriteDao().addFavorite(FavoriteEntity(item.id))
                }
            }
            prefs.edit().putBoolean("migrated_to_room", true).apply()
        } catch (e: Exception) {
            prefs.edit().putBoolean("migrated_to_room", true).apply()
        }
    }
}
