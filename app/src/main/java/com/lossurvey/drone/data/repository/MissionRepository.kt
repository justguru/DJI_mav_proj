package com.lossurvey.drone.data.repository

import com.lossurvey.drone.data.db.entities.CaptureLogEntity
import com.lossurvey.drone.data.models.Mission
import com.lossurvey.drone.data.models.MissionStatus
import com.lossurvey.drone.data.models.Site
import com.lossurvey.drone.data.models.SiteStatus
import kotlinx.coroutines.flow.Flow

interface MissionRepository {
    fun observeMissions(): Flow<List<Mission>>
    suspend fun getMission(id: Long): Mission?
    fun observeMission(id: Long): Flow<Mission?>
    suspend fun saveMission(mission: Mission, sites: List<Site>): Long
    suspend fun updateMissionStatus(id: Long, status: MissionStatus)
    suspend fun updateSiteStatus(siteId: Long, status: SiteStatus)
    suspend fun completeMission(id: Long)
    suspend fun deleteMission(mission: Mission)

    suspend fun logCapture(entry: CaptureLogEntity): Long
    suspend fun getCaptures(missionId: Long): List<CaptureLogEntity>
    fun observeCaptures(missionId: Long): Flow<List<CaptureLogEntity>>
}
