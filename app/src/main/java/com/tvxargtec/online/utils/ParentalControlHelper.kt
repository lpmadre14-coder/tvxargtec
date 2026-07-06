package com.tvxargtec.online.utils

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ParentalControlHelper(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val sessionUnblocked = mutableSetOf<String>()

    companion object {
        private const val PREFS_NAME = "parental_control"
        private const val KEY_PIN = "pc_pin"
        private const val KEY_BLOCKED = "pc_blocked_categories"
        private const val KEY_TIME_LIMIT = "pc_time_limit_minutes"
        private const val KEY_USAGE_DATE = "pc_usage_date"
        private const val KEY_USAGE_TODAY = "pc_usage_today_minutes"
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

    fun getTimeLimitMinutes(): Int {
        return prefs.getInt(KEY_TIME_LIMIT, 0)
    }

    fun setTimeLimitMinutes(minutes: Int) {
        prefs.edit().putInt(KEY_TIME_LIMIT, minutes).apply()
    }

    fun todayUsageMinutes(): Int {
        resetIfNewDay()
        return prefs.getInt(KEY_USAGE_TODAY, 0)
    }

    fun addUsageMinutes(minutes: Int) {
        resetIfNewDay()
        val current = prefs.getInt(KEY_USAGE_TODAY, 0)
        prefs.edit().putInt(KEY_USAGE_TODAY, current + minutes).apply()
    }

    fun isTimeLimitReached(): Boolean {
        val limit = getTimeLimitMinutes()
        if (limit <= 0) return false
        return todayUsageMinutes() >= limit
    }

    fun remainingTimeMinutes(): Int {
        val limit = getTimeLimitMinutes()
        if (limit <= 0) return Int.MAX_VALUE
        return (limit - todayUsageMinutes()).coerceAtLeast(0)
    }

    private fun resetIfNewDay() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val savedDate = prefs.getString(KEY_USAGE_DATE, "")
        if (savedDate != today) {
            prefs.edit()
                .putString(KEY_USAGE_DATE, today)
                .putInt(KEY_USAGE_TODAY, 0)
                .apply()
        }
    }
}
