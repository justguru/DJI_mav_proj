package com.lossurvey.drone.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lossurvey.drone.data.db.entities.MissionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MissionDao {

    @Query("SELECT * FROM missions ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<MissionEntity>>

    @Query("SELECT * FROM missions WHERE id = :id")
    suspend fun getById(id: Long): MissionEntity?

    @Query("SELECT * FROM missions WHERE id = :id")
    fun observeById(id: Long): Flow<MissionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mission: MissionEntity): Long

    @Update
    suspend fun update(mission: MissionEntity)

    @Query("UPDATE missions SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("UPDATE missions SET completedSites = :count WHERE id = :id")
    suspend fun updateCompletedCount(id: Long, count: Int)

    @Query("UPDATE missions SET completedAt = :ts, status = :status WHERE id = :id")
    suspend fun markCompleted(id: Long, ts: Long, status: String)

    @Delete
    suspend fun delete(mission: MissionEntity)
}
