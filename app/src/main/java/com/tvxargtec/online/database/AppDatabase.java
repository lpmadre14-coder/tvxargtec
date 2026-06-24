package com.tvxargtec.online.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.tvxargtec.online.database.dao.ContentDao;
import com.tvxargtec.online.database.dao.FavoriteDao;
import com.tvxargtec.online.database.dao.HistoryDao;
import com.tvxargtec.online.database.entity.ContentEntity;
import com.tvxargtec.online.database.entity.FavoriteEntity;
import com.tvxargtec.online.database.entity.HistoryEntity;

/**
 * Base de datos local con Room para almacenamiento persistente
 */
@Database(
    entities = {ContentEntity.class, FavoriteEntity.class, HistoryEntity.class},
    version = 1,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    
    private static volatile AppDatabase instance;
    
    public abstract ContentDao contentDao();
    public abstract FavoriteDao favoriteDao();
    public abstract HistoryDao historyDao();
    
    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "tvxargtec_database"
                    )
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
                }
            }
        }
        return instance;
    }
}
