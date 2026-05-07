package com.lossurvey.drone.drone

import com.lossurvey.drone.data.models.Site
import com.lossurvey.drone.data.models.SurveyType
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Pure-Kotlin waypoint planner. Returns an SDK-agnostic waypoint list
 * that MissionExecutor translates into either real DJI WaypointMission v5
 * objects or simulator steps.
 */
@Singleton
class WaypointMissionBuilder @Inject constructor() {

    companion object {
        const val DEFAULT_MAX_SPEED_MS = 8.0f
        const val HOVER_STABLE_SECONDS = 3
        const val GREENFIELD_STEP_DEGREES = 30
    }

    fun buildTowerMission(site: Site): PlannedMission {
        require(site.towerLatitude != null && site.towerLongitude != null) {
            "Tower site missing tower coordinates"
        }
        val azimuth = site.azimuthDegrees ?: 0.0
        val waypoints = site.heights.map { height ->
            val (lat, lon) = calculateOffsetPosition(
                towerLat = site.towerLatitude,
                towerLon = site.towerLongitude,
                azimuth = azimuth,
                distanceMeters = site.distanceFromTower
            )
            PlannedWaypoint(
                latitude = lat,
                longitude = lon,
                altitudeM = height,
                headingDegrees = ((azimuth + 180.0) % 360.0),
                gimbalPitchDegrees = -10.0,
                hoverSeconds = HOVER_STABLE_SECONDS,
                cameraType = site.cameraType,
                actionTag = "TOWER_H${height.toInt()}_AZ${azimuth.toInt()}"
            )
        }
        return PlannedMission(
            siteId = site.siteId,
            type = SurveyType.TOWER,
            waypoints = waypoints,
            maxSpeedMs = DEFAULT_MAX_SPEED_MS,
            autoSpeedMs = DEFAULT_MAX_SPEED_MS * 0.7f
        )
    }

    fun buildGreenfieldMission(site: Site): PlannedMission {
        val height = site.surveyHeightM ?: 30.0
        val waypoints = (0 until 360 step GREENFIELD_STEP_DEGREES).map { az ->
            PlannedWaypoint(
                latitude = site.latitude,
                longitude = site.longitude,
                altitudeM = height,
                headingDegrees = az.toDouble(),
                gimbalPitchDegrees = 0.0,
                hoverSeconds = HOVER_STABLE_SECONDS,
                cameraType = site.cameraType,
                actionTag = "GF_AZ$az"
            )
        }
        return PlannedMission(
            siteId = site.siteId,
            type = SurveyType.GREENFIELD,
            waypoints = waypoints,
            maxSpeedMs = DEFAULT_MAX_SPEED_MS,
            autoSpeedMs = 3f
        )
    }

    /**
     * Compute GPS offset position from tower centre.
     * @param azimuth bearing FROM tower TO drone position (degrees)
     */
    private fun calculateOffsetPosition(
        towerLat: Double,
        towerLon: Double,
        azimuth: Double,
        distanceMeters: Double
    ): Pair<Double, Double> {
        val earthRadius = 6371000.0
        val bearing = Math.toRadians(azimuth)
        val lat1 = Math.toRadians(towerLat)
        val lon1 = Math.toRadians(towerLon)
        val d = distanceMeters / earthRadius
        val lat2 = asin(sin(lat1) * cos(d) + cos(lat1) * sin(d) * cos(bearing))
        val lon2 = lon1 + atan2(
            sin(bearing) * sin(d) * cos(lat1),
            cos(d) - sin(lat1) * sin(lat2)
        )
        return Math.toDegrees(lat2) to Math.toDegrees(lon2)
    }
}

data class PlannedWaypoint(
    val latitude: Double,
    val longitude: Double,
    val altitudeM: Double,
    val headingDegrees: Double,
    val gimbalPitchDegrees: Double,
    val hoverSeconds: Int,
    val cameraType: String,
    val actionTag: String
)

data class PlannedMission(
    val siteId: String,
    val type: SurveyType,
    val waypoints: List<PlannedWaypoint>,
    val maxSpeedMs: Float,
    val autoSpeedMs: Float
)
