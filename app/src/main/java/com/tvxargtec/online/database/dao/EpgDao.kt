package com.tvxargtec.online.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tvxargtec.online.database.entity.EpgProgrammeEntity

@Dao
interface EpgDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgrammes(programmes: List<EpgProgrammeEntity>)

    @Query("SELECT * FROM epg_programmes WHERE channelId = :channelId ORDER BY startTime ASC")
    suspend fun getProgrammesForChannel(channelId: String): List<EpgProgrammeEntity>

    @Query("SELECT * FROM epg_programmes WHERE channelId = :channelId AND startTime <= :now AND endTime >= :now LIMIT 1")
    suspend fun getCurrentProgramme(channelId: String, now: Long): EpgProgrammeEntity?

    @Query("SELECT * FROM epg_programmes WHERE channelId = :channelId AND startTime > :now ORDER BY startTime ASC LIMIT 1")
    suspend fun getNextProgramme(channelId: String, now: Long): EpgProgrammeEntity?

    @Query("SELECT * FROM epg_programmes WHERE channelId = :channelId AND startTime >= :from AND endTime <= :to ORDER BY startTime ASC")
    suspend fun getProgrammesInRange(channelId: String, from: Long, to: Long): List<EpgProgrammeEntity>

    @Query("DELETE FROM epg_programmes WHERE endTime < :before")
    suspend fun deleteOldProgrammes(before: Long)

    @Query("DELETE FROM epg_programmes WHERE channelId = :channelId")
    suspend fun deleteProgrammesForChannel(channelId: String)
}
