package com.tvxargtec.online.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

object LocaleHelper {

    private const val PREF_LANG = "pref_language"
    private const val SELECTED_LANG = "selected_language"

    fun setLocale(context: Context, language: String): Context {
        persist(context, language)
        return updateResources(context, language)
    }

    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_LANG, Context.MODE_PRIVATE)
        return prefs.getString(SELECTED_LANG, Locale.getDefault().language) ?: "en"
    }

    private fun persist(context: Context, language: String) {
        val prefs = context.getSharedPreferences(PREF_LANG, Context.MODE_PRIVATE)
        prefs.edit().putString(SELECTED_LANG, language).apply()
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    fun getAvailableLanguages(): List<Pair<String, String>> {
        return listOf(
            "en" to "English",
            "es" to "Español",
            "pt" to "Português",
            "fr" to "Français",
            "de" to "Deutsch",
            "it" to "Italiano",
            "ru" to "Русский",
            "zh" to "中文"
        )
    }
}
