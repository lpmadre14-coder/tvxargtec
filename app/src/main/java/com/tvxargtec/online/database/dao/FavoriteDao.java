package com.tvxargtec.online.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.tvxargtec.online.database.entity.FavoriteEntity;

import java.util.List;

/**
 * Data Access Object para gestionar favoritos en la base de datos local
 */
@Dao
public interface FavoriteDao {
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void addFavorite(FavoriteEntity favorite);
    
    @Delete
    void removeFavorite(FavoriteEntity favorite);
    
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    List<FavoriteEntity> getAllFavorites();
    
    @Query("SELECT * FROM favorites WHERE contentId = :contentId")
    FavoriteEntity getFavoriteByContentId(String contentId);
    
    @Query("SELECT COUNT(*) FROM favorites WHERE contentId = :contentId")
    int isFavorite(String contentId);
    
    @Query("DELETE FROM favorites WHERE contentId = :contentId")
    void deleteFavoriteByContentId(String contentId);
    
    @Query("DELETE FROM favorites")
    void clearAllFavorites();
}
