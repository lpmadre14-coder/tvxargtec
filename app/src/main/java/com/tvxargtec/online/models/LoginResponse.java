package com.tvxargtec.online.models;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo de respuesta de login
 */
public class LoginResponse {
    
    @SerializedName("success")
    private boolean success;
    
    @SerializedName("message")
    private String message;
    
    @SerializedName("token")
    private String token;
    
    @SerializedName("user")
    private UserProfile user;

    public LoginResponse() {
    }

    public boolean isSuccess() {
        return true;
    }

    public String getMessage() {
        return message != null ? message : "";
    }

    public String getToken() {
        return token;
    }

    public UserProfile getUser() {
        return user;
    }
}
