package com.lossurvey.drone.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lossurvey.drone.data.db.entities.CaptureLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CaptureLogDao {

    @Query("SELECT * FROM capture_logs WHERE missionId = :missionId ORDER BY timestamp ASC")
    suspend fun getForMission(missionId: Long): List<CaptureLogEntity>

    @Query("SELECT * FROM capture_logs WHERE missionId = :missionId ORDER BY timestamp ASC")
    fun observeForMission(missionId: Long): Flow<List<CaptureLogEntity>>

    @Query("SELECT * FROM capture_logs WHERE siteId = :siteId AND missionId = :missionId")
    suspend fun getForSite(missionId: Long, siteId: String): List<CaptureLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: CaptureLogEntity): Long

    @Query("SELECT COUNT(*) FROM capture_logs WHERE missionId = :missionId")
    suspend fun countForMission(missionId: Long): Int
}
