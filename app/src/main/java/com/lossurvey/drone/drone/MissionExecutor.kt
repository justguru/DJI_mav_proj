package com.lossurvey.drone.drone

import com.lossurvey.drone.data.db.entities.CaptureLogEntity
import com.lossurvey.drone.data.models.FlightLogEntry
import com.lossurvey.drone.data.models.Mission
import com.lossurvey.drone.data.models.MissionStatus
import com.lossurvey.drone.data.models.Site
import com.lossurvey.drone.data.models.SiteStatus
import com.lossurvey.drone.data.models.SurveyType
import com.lossurvey.drone.data.preferences.AppPreferences
import com.lossurvey.drone.data.repository.MissionRepository
import com.lossurvey.drone.storage.MetadataWriter
import com.lossurvey.drone.storage.ProjectFolderManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class ExecutionState(
    val status: MissionStatus = MissionStatus.PENDING,
    val mission: Mission? = null,
    val currentSiteIndex: Int = 0,
    val currentSite: Site? = null,
    val waypointIndex: Int = 0,
    val totalWaypointsAtSite: Int = 0,
    val capturedAtSite: Int = 0,
    val totalCaptured: Int = 0,
    val totalExpected: Int = 0,
    val lastError: String? = null
)

data class PreflightResult(val passed: Boolean, val errors: List<String>)

@Singleton
class MissionExecutor @Inject constructor(
    private val waypointBuilder: WaypointMissionBuilder,
    private val cameraController: CameraController,
    private val djiManager: DJIManager,
    private val rtkManager: RTKManager,
    private val metadataWriter: MetadataWriter,
    private val projectFolderManager: ProjectFolderManager,
    private val repository: MissionRepository,
    private val prefs: AppPreferences
) {
    private val _executionState = MutableStateFlow(ExecutionState())
    val executionState: StateFlow<ExecutionState> = _executionState.asStateFlow()

    private val _captureFlash = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val captureFlash: SharedFlow<Unit> = _captureFlash.asSharedFlow()

    @Volatile private var paused = false
    @Volatile private var aborted = false

    fun pause() { paused = true }
    fun resume() { paused = false }
    fun abort() { aborted = true; paused = false }

    fun validatePreflight(
        mission: Mission,
        allowGpsFallback: Boolean = prefs.current.allowGpsFallback
    ): PreflightResult {
        val errors = mutableListOf<String>()
        val state = djiManager.droneState.value
        val cfg = prefs.current

        if (!state.isConnected) errors.add("Drone not connected")
        if (state.batteryPercent < cfg.minBatteryPercent)
            errors.add("Battery low: ${state.batteryPercent}% (need ≥${cfg.minBatteryPercent}%)")
        if (state.gpsSignal < cfg.minGpsSatellites)
            errors.add("GPS signal weak: ${state.gpsSignal}/${cfg.minGpsSatellites} required")
        if (!rtkManager.isLocked && !(allowGpsFallback || cfg.allowGpsFallback))
            errors.add("RTK not fixed")
        if (mission.sites.isEmpty()) errors.add("No sites in mission")

        return PreflightResult(errors.isEmpty(), errors)
    }

    suspend fun executeMission(
        mission: Mission,
        allowGpsFallback: Boolean = prefs.current.allowGpsFallback
    ): Result<Unit> {
        val validation = validatePreflight(mission, allowGpsFallback)
        if (!validation.passed) return Result.failure(IllegalStateException(validation.errors.joinToString(", ")))

        aborted = false
        paused = false

        val totalExpected = mission.sites.sumOf { plannedFor(it).waypoints.size }
        _executionState.value = ExecutionState(
            status = MissionStatus.FLYING,
            mission = mission,
            totalExpected = totalExpected
        )
        repository.updateMissionStatus(mission.id, MissionStatus.FLYING)
        metadataWriter.logFlightEvent(
            FlightLogEntry(
                timestamp = System.currentTimeMillis(),
                event = "MISSION_STARTED"
            ),
            mission.projectFolderPath
        )

        var totalCaptured = 0

        mission.sites.forEachIndexed { index, site ->
            if (aborted) return@forEachIndexed
            val planned = plannedFor(site)
            _executionState.value = _executionState.value.copy(
                currentSiteIndex = index,
                currentSite = site,
                totalWaypointsAtSite = planned.waypoints.size,
                capturedAtSite = 0
            )
            repository.updateSiteStatus(site.id, SiteStatus.FLYING)
            metadataWriter.logFlightEvent(
                FlightLogEntry(
                    timestamp = System.currentTimeMillis(),
                    event = "SITE_START",
                    siteId = site.siteId,
                    latitude = site.latitude,
                    longitude = site.longitude
                ),
                mission.projectFolderPath
            )

            try {
                executeSite(mission, site, planned) { capture ->
                    totalCaptured += 1
                    _executionState.value = _executionState.value.copy(
                        totalCaptured = totalCaptured,
                        capturedAtSite = _executionState.value.capturedAtSite + 1,
                        waypointIndex = _executionState.value.waypointIndex + 1
                    )
                    _captureFlash.tryEmit(Unit)
                }
                repository.updateSiteStatus(site.id, SiteStatus.COMPLETED)
                metadataWriter.logFlightEvent(
                    FlightLogEntry(
                        timestamp = System.currentTimeMillis(),
                        event = "SITE_COMPLETE",
                        siteId = site.siteId
                    ),
                    mission.projectFolderPath
                )
            } catch (e: Exception) {
                repository.updateSiteStatus(site.id, SiteStatus.FAILED)
                _executionState.value = _executionState.value.copy(lastError = e.message)
                metadataWriter.logFlightEvent(
                    FlightLogEntry(
                        timestamp = System.currentTimeMillis(),
                        event = "SITE_FAILED",
                        siteId = site.siteId,
                        errorMessage = e.message ?: "Unknown error"
                    ),
                    mission.projectFolderPath
                )
            }
        }

        if (aborted) {
            _executionState.value = _executionState.value.copy(status = MissionStatus.ABORTED)
            repository.updateMissionStatus(mission.id, MissionStatus.ABORTED)
            metadataWriter.logFlightEvent(
                FlightLogEntry(System.currentTimeMillis(), "MISSION_ABORTED"),
                mission.projectFolderPath
            )
        } else {
            _executionState.value = _executionState.value.copy(status = MissionStatus.COMPLETED)
            repository.completeMission(mission.id)
            metadataWriter.logFlightEvent(
                FlightLogEntry(System.currentTimeMillis(), "MISSION_COMPLETE"),
                mission.projectFolderPath
            )
        }

        return Result.success(Unit)
    }

    private suspend fun executeSite(
        mission: Mission,
        site: Site,
        planned: PlannedMission,
        onCapture: (CaptureLogEntity) -> Unit
    ) {
        val imagesDir = projectFolderManager.getImagesFolder(mission.projectFolderPath)

        for (wp in planned.waypoints) {
            if (aborted) return
            while (paused && !aborted) delay(250)

            // SIMULATE flight to waypoint
            djiManager.simulateMove(wp.latitude, wp.longitude, wp.altitudeM, wp.headingDegrees)
            delay(1500)

            val state = djiManager.droneState.value
            val accuracy = if (state.rtkLocked) state.rtkAccuracyM else 1.5

            val fileName = imageFileName(site, wp)
            val targetFile = File(imagesDir, fileName)
            val stamp = CameraController.CaptureStamp(
                siteId = site.siteId,
                surveyType = site.type.name,
                lat = state.latitude,
                lon = state.longitude,
                altM = state.altitudeM,
                azimuth = wp.headingDegrees,
                pitch = wp.gimbalPitchDegrees,
                cameraType = wp.cameraType,
                rtkAcc = accuracy,
                battery = state.batteryPercent,
                timestamp = System.currentTimeMillis()
            )
            val captured = retryCapture(targetFile, stamp, wp)

            if (captured.isSuccess) {
                val entry = CaptureLogEntity(
                    missionId = mission.id,
                    siteId = site.siteId,
                    imagePath = targetFile.absolutePath,
                    latitude = state.latitude,
                    longitude = state.longitude,
                    altitudeM = state.altitudeM,
                    azimuthDegrees = wp.headingDegrees,
                    timestamp = stamp.timestamp,
                    rtkAccuracyM = accuracy,
                    batteryPercent = state.batteryPercent,
                    cameraType = wp.cameraType
                )
                repository.logCapture(entry)
                metadataWriter.addCapture(
                    com.lossurvey.drone.data.models.CaptureMetadata(
                        siteId = site.siteId,
                        surveyType = site.type.name,
                        latitude = state.latitude,
                        longitude = state.longitude,
                        altitudeM = state.altitudeM,
                        azimuthDegrees = wp.headingDegrees,
                        cameraPitchDegrees = wp.gimbalPitchDegrees,
                        cameraType = wp.cameraType,
                        imagePath = targetFile.absolutePath,
                        timestamp = stamp.timestamp,
                        rtkAccuracyM = accuracy,
                        batteryPercent = state.batteryPercent
                    ),
                    mission.projectFolderPath
                )
                onCapture(entry)
                if (prefs.current.simulatorMode) {
                    djiManager.simulateBatteryDrain(SimulatorConfig.MOCK_DRAIN_PER_CAPTURE_PCT)
                }
            } else {
                metadataWriter.logFlightEvent(
                    FlightLogEntry(
                        timestamp = System.currentTimeMillis(),
                        event = "CAPTURE_SKIPPED",
                        siteId = site.siteId,
                        errorMessage = captured.exceptionOrNull()?.message ?: "capture failed"
                    ),
                    mission.projectFolderPath
                )
            }

            delay(800)
        }
    }

    private suspend fun retryCapture(
        file: File,
        stamp: CameraController.CaptureStamp,
        wp: PlannedWaypoint
    ): Result<File> {
        repeat(3) { attempt ->
            val r = cameraController.configureAndCapture(
                cameraType = wp.cameraType,
                pitchDegrees = wp.gimbalPitchDegrees,
                targetFile = file,
                stamp = stamp
            )
            if (r.isSuccess) return r
            delay(500L * (attempt + 1))
        }
        return Result.failure(IllegalStateException("capture failed after 3 attempts"))
    }

    private fun plannedFor(site: Site): PlannedMission =
        if (site.type == SurveyType.TOWER) waypointBuilder.buildTowerMission(site)
        else waypointBuilder.buildGreenfieldMission(site)

    private fun imageFileName(site: Site, wp: PlannedWaypoint): String {
        val az = "%03d".format(wp.headingDegrees.toInt())
        return if (site.type == SurveyType.TOWER) {
            val h = wp.altitudeM.toInt()
            "${site.siteId}_H${h}M_AZ$az.jpg"
        } else {
            "${site.siteId}_GF_AZ$az.jpg"
        }
    }
}
