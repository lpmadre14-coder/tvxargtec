package com.tvxargtec.online.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.tvxargtec.online.database.entity.HistoryEntity;

import java.util.List;

/**
 * Data Access Object para gestionar historial en la base de datos local
 */
@Dao
public interface HistoryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertHistory(HistoryEntity history);
    
    @Update
    void updateHistory(HistoryEntity history);
    
    @Delete
    void deleteHistory(HistoryEntity history);
    
    @Query("SELECT * FROM history ORDER BY lastWatched DESC")
    List<HistoryEntity> getAllHistory();
    
    @Query("SELECT * FROM history WHERE contentId = :contentId")
    HistoryEntity getHistoryByContentId(String contentId);
    
    @Query("SELECT * FROM history ORDER BY lastWatched DESC LIMIT :limit")
    List<HistoryEntity> getRecentHistory(int limit);
    
    @Query("UPDATE history SET watchProgress = :progress, lastWatched = :timestamp WHERE contentId = :contentId")
    void updateWatchProgress(String contentId, int progress, long timestamp);
    
    @Query("DELETE FROM history WHERE contentId = :contentId")
    void deleteHistoryByContentId(String contentId);
    
    @Query("DELETE FROM history")
    void clearAllHistory();
}
