package com.lossurvey.drone.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lossurvey.drone.data.db.entities.SiteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SiteDao {

    @Query("SELECT * FROM sites WHERE missionId = :missionId ORDER BY id ASC")
    suspend fun getSitesForMission(missionId: Long): List<SiteEntity>

    @Query("SELECT * FROM sites WHERE missionId = :missionId ORDER BY id ASC")
    fun observeSitesForMission(missionId: Long): Flow<List<SiteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sites: List<SiteEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(site: SiteEntity): Long

    @Update
    suspend fun update(site: SiteEntity)

    @Query("UPDATE sites SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("UPDATE sites SET capturedCount = :count WHERE id = :id")
    suspend fun updateCapturedCount(id: Long, count: Int)

    @Query("DELETE FROM sites WHERE missionId = :missionId")
    suspend fun deleteForMission(missionId: Long)
}
