package com.lossurvey.drone.data.repository

import com.lossurvey.drone.data.db.dao.CaptureLogDao
import com.lossurvey.drone.data.db.dao.MissionDao
import com.lossurvey.drone.data.db.dao.SiteDao
import com.lossurvey.drone.data.db.entities.CaptureLogEntity
import com.lossurvey.drone.data.models.Mission
import com.lossurvey.drone.data.models.MissionStatus
import com.lossurvey.drone.data.models.Site
import com.lossurvey.drone.data.models.SiteStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MissionRepositoryImpl @Inject constructor(
    private val missionDao: MissionDao,
    private val siteDao: SiteDao,
    private val captureLogDao: CaptureLogDao
) : MissionRepository {

    override fun observeMissions(): Flow<List<Mission>> =
        missionDao.observeAll().map { entities ->
            entities.map { entity ->
                val sites = siteDao.getSitesForMission(entity.id).map { it.toModel() }
                entity.toModel(sites)
            }
        }

    override suspend fun getMission(id: Long): Mission? {
        val entity = missionDao.getById(id) ?: return null
        val sites = siteDao.getSitesForMission(id).map { it.toModel() }
        return entity.toModel(sites)
    }

    override fun observeMission(id: Long): Flow<Mission?> =
        combine(
            missionDao.observeById(id),
            siteDao.observeSitesForMission(id)
        ) { mission, sites ->
            mission?.toModel(sites.map { it.toModel() })
        }

    override suspend fun saveMission(mission: Mission, sites: List<Site>): Long {
        val missionId = missionDao.insert(mission.toEntity())
        val siteEntities = sites.map { it.copy(missionId = missionId).toEntity(missionId) }
        siteDao.insertAll(siteEntities)
        return missionId
    }

    override suspend fun updateMissionStatus(id: Long, status: MissionStatus) {
        missionDao.updateStatus(id, status.name)
    }

    override suspend fun updateSiteStatus(siteId: Long, status: SiteStatus) {
        siteDao.updateStatus(siteId, status.name)
    }

    override suspend fun completeMission(id: Long) {
        missionDao.markCompleted(id, System.currentTimeMillis(), MissionStatus.COMPLETED.name)
    }

    override suspend fun deleteMission(mission: Mission) {
        missionDao.delete(mission.toEntity())
    }

    override suspend fun logCapture(entry: CaptureLogEntity): Long =
        captureLogDao.insert(entry)

    override suspend fun getCaptures(missionId: Long): List<CaptureLogEntity> =
        captureLogDao.getForMission(missionId)

    override fun observeCaptures(missionId: Long): Flow<List<CaptureLogEntity>> =
        captureLogDao.observeForMission(missionId)
}
