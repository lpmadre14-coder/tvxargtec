package com.tvxargtec.online.models;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo de perfil de usuario
 */
public class UserProfile {
    
    @SerializedName("id")
    private String id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("email")
    private String email;
    
    @SerializedName("avatar_url")
    private String avatarUrl;
    
    @SerializedName("plan_type")
    private String planType;
    
    @SerializedName("plan_expiry")
    private String planExpiry;
    
    @SerializedName("created_at")
    private String createdAt;
    
    @SerializedName("updated_at")
    private String updatedAt;

    public UserProfile() {
    }

    public UserProfile(String id, String name, String email, String avatarUrl, 
                       String planType, String planExpiry) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.planType = planType;
        this.planExpiry = planExpiry;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public String getPlanExpiry() {
        return planExpiry;
    }

    public void setPlanExpiry(String planExpiry) {
        this.planExpiry = planExpiry;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
