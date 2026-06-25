package com.tvxargtec.online.utils

import android.content.Context
import android.content.SharedPreferences

class ParentalControlHelper(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val sessionUnblocked = mutableSetOf<String>()

    companion object {
        private const val PREFS_NAME = "parental_control"
        private const val KEY_PIN = "pc_pin"
        private const val KEY_BLOCKED = "pc_blocked_categories"
        private const val DEFAULT_PIN = "525452"
        private const val SEPARATOR = ","
    }

    fun isPinSet(): Boolean {
        return prefs.contains(KEY_PIN)
    }

    fun getPin(): String {
        return prefs.getString(KEY_PIN, DEFAULT_PIN) ?: DEFAULT_PIN
    }

    fun setPin(pin: String) {
        prefs.edit().putString(KEY_PIN, pin).apply()
    }

    fun verifyPin(input: String): Boolean {
        return input == getPin()
    }

    fun getBlockedCategories(): Set<String> {
        val raw = prefs.getString(KEY_BLOCKED, "") ?: ""
        if (raw.isEmpty()) return emptySet()
        return raw.split(SEPARATOR).filter { it.isNotEmpty() }.toSet()
    }

    fun setBlockedCategories(categories: Set<String>) {
        val raw = categories.joinToString(SEPARATOR)
        prefs.edit().putString(KEY_BLOCKED, raw).apply()
    }

    fun isCategoryBlocked(categoryId: String): Boolean {
        if (categoryId.isEmpty()) return false
        if (sessionUnblocked.contains(categoryId)) return false
        return getBlockedCategories().contains(categoryId)
    }

    fun unblockForSession(categoryId: String) {
        sessionUnblocked.add(categoryId)
    }

    fun clearSessionUnblocks() {
        sessionUnblocked.clear()
    }
}
