package com.tvxargtec.online.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

data class CustomCategory(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val channelIds: MutableSet<String> = mutableSetOf(),
    val color: Int = 0xFF7C3AED.toInt(),
    val createdAt: Long = System.currentTimeMillis()
)

class CustomCategoryManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getCategories(): List<CustomCategory> {
        val json = prefs.getString(KEY_CATEGORIES, null) ?: return emptyList()
        val type = object : TypeToken<List<CustomCategory>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun saveCategories(categories: List<CustomCategory>) {
        prefs.edit().putString(KEY_CATEGORIES, gson.toJson(categories)).apply()
    }

    fun createCategory(name: String, color: Int = 0xFF7C3AED.toInt()): CustomCategory {
        val category = CustomCategory(name = name, color = color)
        val categories = getCategories().toMutableList()
        categories.add(category)
        saveCategories(categories)
        return category
    }

    fun deleteCategory(id: String) {
        val categories = getCategories().toMutableList()
        categories.removeAll { it.id == id }
        saveCategories(categories)
    }

    fun renameCategory(id: String, newName: String) {
        val categories = getCategories().toMutableList()
        val index = categories.indexOfFirst { it.id == id }
        if (index >= 0) {
            categories[index] = categories[index].copy(name = newName)
            saveCategories(categories)
        }
    }

    fun addChannelToCategory(categoryId: String, channelId: String) {
        val categories = getCategories().toMutableList()
        val index = categories.indexOfFirst { it.id == categoryId }
        if (index >= 0) {
            val updated = categories[index].channelIds.toMutableSet()
            updated.add(channelId)
            categories[index] = categories[index].copy(channelIds = updated)
            saveCategories(categories)
        }
    }

    fun removeChannelFromCategory(categoryId: String, channelId: String) {
        val categories = getCategories().toMutableList()
        val index = categories.indexOfFirst { it.id == categoryId }
        if (index >= 0) {
            val updated = categories[index].channelIds.toMutableSet()
            updated.remove(channelId)
            categories[index] = categories[index].copy(channelIds = updated)
            saveCategories(categories)
        }
    }

    fun getChannelsForCategory(context: Context, categoryId: String): List<Channel> {
        val category = getCategories().find { it.id == categoryId } ?: return emptyList()
        val allChannels = ChannelDataManager.getChannels(context)
        return allChannels.filter { it.id in category.channelIds }
    }

    companion object {
        private const val PREFS_NAME = "custom_categories_prefs"
        private const val KEY_CATEGORIES = "custom_categories"
    }
}
