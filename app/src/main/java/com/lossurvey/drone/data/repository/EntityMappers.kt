package com.lossurvey.drone.data.repository

import com.lossurvey.drone.data.db.entities.MissionEntity
import com.lossurvey.drone.data.db.entities.SiteEntity
import com.lossurvey.drone.data.models.Mission
import com.lossurvey.drone.data.models.MissionStatus
import com.lossurvey.drone.data.models.Site
import com.lossurvey.drone.data.models.SiteStatus
import com.lossurvey.drone.data.models.SurveyType
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true }
private val doubleListSerializer = ListSerializer(Double.serializer())

fun MissionEntity.toModel(sites: List<Site> = emptyList()): Mission = Mission(
    id = id,
    name = name,
    type = runCatching { SurveyType.valueOf(type) }.getOrDefault(SurveyType.TOWER),
    createdAt = createdAt,
    completedAt = completedAt,
    status = runCatching { MissionStatus.valueOf(status) }.getOrDefault(MissionStatus.PENDING),
    sites = sites,
    projectFolderPath = projectFolderPath
)

fun Mission.toEntity(): MissionEntity = MissionEntity(
    id = id,
    name = name,
    type = type.name,
    status = status.name,
    createdAt = createdAt,
    completedAt = completedAt,
    projectFolderPath = projectFolderPath,
    totalSites = sites.size,
    completedSites = sites.count { it.status == SiteStatus.COMPLETED }
)

fun SiteEntity.toModel(): Site = Site(
    id = id,
    missionId = missionId,
    siteId = siteId,
    latitude = latitude,
    longitude = longitude,
    type = runCatching { SurveyType.valueOf(type) }.getOrDefault(SurveyType.TOWER),
    towerLatitude = towerLatitude,
    towerLongitude = towerLongitude,
    heights = runCatching { json.decodeFromString(doubleListSerializer, heightsJson) }
        .getOrDefault(emptyList()),
    azimuthDegrees = azimuthDegrees,
    cameraType = cameraType,
    distanceFromTower = distanceFromTower,
    surveyHeightM = surveyHeightM,
    status = runCatching { SiteStatus.valueOf(status) }.getOrDefault(SiteStatus.PENDING),
    capturedCount = capturedCount
)

fun Site.toEntity(missionId: Long = this.missionId): SiteEntity = SiteEntity(
    id = id,
    missionId = missionId,
    siteId = siteId,
    latitude = latitude,
    longitude = longitude,
    type = type.name,
    towerLatitude = towerLatitude,
    towerLongitude = towerLongitude,
    heightsJson = json.encodeToString(doubleListSerializer, heights),
    azimuthDegrees = azimuthDegrees,
    cameraType = cameraType,
    distanceFromTower = distanceFromTower,
    surveyHeightM = surveyHeightM,
    status = status.name,
    capturedCount = capturedCount
)
