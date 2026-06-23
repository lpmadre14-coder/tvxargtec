package com.tvxargtec.online.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.tvxargtec.online.database.entity.ContentEntity;

import java.util.List;

/**
 * Data Access Object para gestionar contenido en la base de datos local
 */
@Dao
public interface ContentDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertContent(ContentEntity content);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllContent(List<ContentEntity> contents);
    
    @Update
    void updateContent(ContentEntity content);
    
    @Delete
    void deleteContent(ContentEntity content);
    
    @Query("SELECT * FROM content WHERE id = :id")
    ContentEntity getContentById(String id);
    
    @Query("SELECT * FROM content WHERE category = :category")
    List<ContentEntity> getContentByCategory(String category);
    
    @Query("SELECT * FROM content ORDER BY lastUpdated DESC LIMIT :limit")
    List<ContentEntity> getRecentContent(int limit);
    
    @Query("SELECT * FROM content WHERE title LIKE '%' || :query || '%'")
    List<ContentEntity> searchContent(String query);
    
    @Query("DELETE FROM content WHERE lastUpdated < :timestamp")
    void deleteOldContent(long timestamp);
}
