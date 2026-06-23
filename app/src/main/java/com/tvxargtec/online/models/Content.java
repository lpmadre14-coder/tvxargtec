package com.tvxargtec.online.models;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo de contenido (película, serie, etc.)
 */
public class Content {
    
    @SerializedName("id")
    private String id;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("poster_url")
    private String posterUrl;
    
    @SerializedName("thumbnail_url")
    private String thumbnailUrl;
    
    @SerializedName("category")
    private String category;
    
    @SerializedName("rating")
    private double rating;
    
    @SerializedName("duration")
    private int duration;
    
    @SerializedName("release_date")
    private String releaseDate;
    
    @SerializedName("video_url")
    private String videoUrl;
    
    @SerializedName("is_favorite")
    private boolean isFavorite;
    
    @SerializedName("watch_progress")
    private int watchProgress;

    public Content() {
    }

    public Content(String id, String title, String description, String posterUrl, 
                   String category, double rating, String videoUrl) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.posterUrl = posterUrl;
        this.category = category;
        this.rating = rating;
        this.videoUrl = videoUrl;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public int getWatchProgress() {
        return watchProgress;
    }

    public void setWatchProgress(int watchProgress) {
        this.watchProgress = watchProgress;
    }
}
