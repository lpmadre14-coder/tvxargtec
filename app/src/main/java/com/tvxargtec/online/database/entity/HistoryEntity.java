package com.tvxargtec.online.database.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad de historial para la base de datos local
 */
@Entity(
    tableName = "history",
    indices = {@Index("contentId")},
    foreignKeys = @ForeignKey(
        entity = ContentEntity.class,
        parentColumns = "id",
        childColumns = "contentId",
        onDelete = ForeignKey.CASCADE
    )
)
public class HistoryEntity {
    
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String contentId;
    public int watchProgress;
    public long lastWatched;

    public HistoryEntity(String contentId, int watchProgress) {
        this.contentId = contentId;
        this.watchProgress = watchProgress;
        this.lastWatched = System.currentTimeMillis();
    }
}
