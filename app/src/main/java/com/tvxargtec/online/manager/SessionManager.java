package com.tvxargtec.online.manager;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Gestor centralizado de sesión de usuario
 */
public class SessionManager {
    
    private static final String PREF_NAME = "user_data";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_AVATAR = "user_avatar";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_PLAN_STATUS = "plan_status";
    private static final String KEY_PLAN_EXPIRY = "plan_expiry";
    private static final String KEY_TOKEN_EXPIRY = "token_expiry";
    
    private SharedPreferences sharedPreferences;
    private static SessionManager instance;

    private SessionManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    // Métodos de Sesión
    public void saveSession(String userId, String name, String email, String authToken, String planStatus) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_AUTH_TOKEN, authToken);
        editor.putString(KEY_PLAN_STATUS, planStatus);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_TOKEN_EXPIRY, System.currentTimeMillis() + (24 * 60 * 60 * 1000)); // 24 horas
        editor.apply();
    }

    public void clearSession() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public boolean isTokenExpired() {
        long expiryTime = sharedPreferences.getLong(KEY_TOKEN_EXPIRY, 0);
        return System.currentTimeMillis() > expiryTime;
    }

    // Getters
    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, "");
    }

    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "Usuario");
    }

    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, "");
    }

    public String getUserAvatar() {
        return sharedPreferences.getString(KEY_USER_AVATAR, "");
    }

    public String getAuthToken() {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, "");
    }

    public String getRefreshToken() {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, "");
    }

    public String getPlanStatus() {
        return sharedPreferences.getString(KEY_PLAN_STATUS, "Free");
    }

    public String getPlanExpiry() {
        return sharedPreferences.getString(KEY_PLAN_EXPIRY, "N/A");
    }

    // Setters
    public void setUserName(String name) {
        sharedPreferences.edit().putString(KEY_USER_NAME, name).apply();
    }

    public void setUserEmail(String email) {
        sharedPreferences.edit().putString(KEY_USER_EMAIL, email).apply();
    }

    public void setUserAvatar(String avatarUrl) {
        sharedPreferences.edit().putString(KEY_USER_AVATAR, avatarUrl).apply();
    }

    public void setAuthToken(String token) {
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply();
    }

    public void setRefreshToken(String token) {
        sharedPreferences.edit().putString(KEY_REFRESH_TOKEN, token).apply();
    }

    public void setPlanStatus(String status) {
        sharedPreferences.edit().putString(KEY_PLAN_STATUS, status).apply();
    }

    public void setPlanExpiry(String expiry) {
        sharedPreferences.edit().putString(KEY_PLAN_EXPIRY, expiry).apply();
    }

    public void setTokenExpiry(long expiryTime) {
        sharedPreferences.edit().putLong(KEY_TOKEN_EXPIRY, expiryTime).apply();
    }
}
