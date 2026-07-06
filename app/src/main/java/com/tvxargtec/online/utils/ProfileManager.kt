package com.tvxargtec.online.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

data class UserProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val avatar: String = "",
    val isPinProtected: Boolean = false,
    val pin: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

class ProfileManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getProfiles(): List<UserProfile> {
        val json = prefs.getString(KEY_PROFILES, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<ArrayList<UserProfile>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun createProfile(name: String, avatar: String = ""): UserProfile {
        val profile = UserProfile(
            id = UUID.randomUUID().toString(),
            name = name,
            avatar = avatar,
            createdAt = System.currentTimeMillis()
        )
        val profiles = getProfiles().toMutableList()
        profiles.add(profile)
        saveProfiles(profiles)
        return profile
    }

    fun deleteProfile(id: String) {
        val profiles = getProfiles().toMutableList()
        profiles.removeAll { it.id == id }
        saveProfiles(profiles)
        if (getActiveProfile().id == id) {
            val remaining = profiles.toList()
            if (remaining.isNotEmpty()) {
                setActiveProfile(remaining.first().id)
            } else {
                prefs.edit().remove(KEY_ACTIVE_PROFILE).apply()
            }
        }
    }

    fun updateProfile(profile: UserProfile) {
        val profiles = getProfiles().toMutableList()
        val index = profiles.indexOfFirst { it.id == profile.id }
        if (index >= 0) {
            profiles[index] = profile
            saveProfiles(profiles)
        }
    }

    fun getActiveProfile(): UserProfile {
        val activeId = prefs.getString(KEY_ACTIVE_PROFILE, null) ?: ""
        val profiles = getProfiles()
        return profiles.firstOrNull { it.id == activeId }
            ?: profiles.firstOrNull()
            ?: UserProfile(name = "Invitado")
    }

    fun setActiveProfile(id: String) {
        prefs.edit().putString(KEY_ACTIVE_PROFILE, id).apply()
    }

    fun verifyPin(id: String, input: String): Boolean {
        val profile = getProfiles().firstOrNull { it.id == id } ?: return false
        return profile.isPinProtected && profile.pin == input
    }

    private fun saveProfiles(profiles: List<UserProfile>) {
        prefs.edit().putString(KEY_PROFILES, gson.toJson(profiles)).apply()
    }

    companion object {
        private const val PREFS_NAME = "tvxargtec_profiles"
        private const val KEY_PROFILES = "profiles"
        private const val KEY_ACTIVE_PROFILE = "active_profile"
    }
}
