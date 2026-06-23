package com.tvxargtec.online.database.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad de favorito para la base de datos local
 */
@Entity(
    tableName = "favorites",
    indices = {@Index("contentId")},
    foreignKeys = @ForeignKey(
        entity = ContentEntity.class,
        parentColumns = "id",
        childColumns = "contentId",
        onDelete = ForeignKey.CASCADE
    )
)
public class FavoriteEntity {
    
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String contentId;
    public long addedAt;

    public FavoriteEntity(String contentId) {
        this.contentId = contentId;
        this.addedAt = System.currentTimeMillis();
    }
}
