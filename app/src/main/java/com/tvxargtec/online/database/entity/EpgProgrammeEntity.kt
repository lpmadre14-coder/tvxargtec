package com.tvxargtec.online.database.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "epg_programmes",
    indices = [Index(value = ["channelId", "startTime"])]
)
data class EpgProgrammeEntity(
    @androidx.room.PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val channelId: String,
    val title: String,
    val description: String = "",
    val category: String = "",
    val startTime: Long,
    val endTime: Long,
    val cachedAt: Long = System.currentTimeMillis()
)
