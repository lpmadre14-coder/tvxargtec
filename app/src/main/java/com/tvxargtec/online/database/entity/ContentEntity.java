package com.tvxargtec.online.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entidad de contenido para la base de datos local
 */
@Entity(tableName = "content")
public class ContentEntity {
    
    @PrimaryKey
    @NonNull
    public String id;
    
    public String title;
    public String description;
    public String posterUrl;
    public String thumbnailUrl;
    public String category;
    public double rating;
    public int duration;
    public String releaseDate;
    public String videoUrl;
    public long lastUpdated;

    public ContentEntity(String id, String title, String description, String posterUrl, 
                        String category, double rating, String videoUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.posterUrl = posterUrl;
        this.category = category;
        this.rating = rating;
        this.videoUrl = videoUrl;
        this.lastUpdated = System.currentTimeMillis();
    }
}
