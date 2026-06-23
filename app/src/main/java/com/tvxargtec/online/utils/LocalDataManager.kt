package com.tvxargtec.online.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class LocalDataManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("tvxargtec_data", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun addFavorite(channel: ChannelItem) {
        val list = getFavorites().toMutableList()
        if (list.none { it.id == channel.id }) {
            list.add(channel)
            saveList("favorites", list)
        }
    }

    fun removeFavorite(channelId: String) {
        val list = getFavorites().toMutableList()
        list.removeAll { it.id == channelId }
        saveList("favorites", list)
    }

    fun isFavorite(channelId: String): Boolean {
        return getFavorites().any { it.id == channelId }
    }

    fun getFavorites(): List<ChannelItem> {
        return getChannelList("favorites")
    }

    fun addToHistory(channel: ChannelItem) {
        try {
            val list = getHistory().toMutableList()
            // Eliminar si ya existe para moverlo al principio
            list.removeAll { it.id == channel.id }
            list.add(0, channel)
            if (list.size > 50) list.removeAt(list.lastIndex)
            saveList("history", list)
        } catch (e: Exception) {
            // Si hay error de casteo por datos corruptos, reiniciamos el historial
            val newList = listOf(channel)
            saveList("history", newList)
        }
    }

    fun getHistory(): List<ChannelItem> {
        return getChannelList("history")
    }

    private fun <T> saveList(key: String, list: List<T>) {
        prefs.edit().putString(key, gson.toJson(list)).apply()
    }

    private fun getChannelList(key: String): List<ChannelItem> {
        val json = prefs.getString(key, null) ?: return emptyList()
        return try {
            val listType: Type = object : TypeToken<ArrayList<ChannelItem>>() {}.type
            gson.fromJson(json, listType) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

data class ChannelItem(
    val id: String,
    @JvmField val title: String,
    @JvmField val url: String,
    val logo: String = "",
    val category: String = ""
)
