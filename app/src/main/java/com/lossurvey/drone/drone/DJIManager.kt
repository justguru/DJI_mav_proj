package com.lossurvey.drone.drone

import android.content.Context
import com.lossurvey.drone.data.models.DroneState
import com.lossurvey.drone.data.preferences.AppPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

sealed class ConnectionEvent {
    object Connected : ConnectionEvent()
    object Disconnected : ConnectionEvent()
    data class Error(val message: String) : ConnectionEvent()
}

/**
 * Owns DJI MSDK v5 lifecycle and exposes drone telemetry as flows.
 * Reacts to AppPreferences.simulatorMode toggle at runtime.
 *
 * REAL-DRONE WIRING (when simulatorMode == false):
 *  - Uncomment DJI dependencies in app/build.gradle.kts
 *  - In initReal(): DJISDKManager.getInstance().registerApp(context, callback)
 *  - In setupListeners(): subscribe via FlightControllerKey + BatteryKey + KeyTools
 *  - Map state.aircraftLocation, attitude.yaw, isFlying into DroneState
 */
@Singleton
class DJIManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: AppPreferences
) {
    private val _droneState = MutableStateFlow(DroneState())
    val droneState: StateFlow<DroneState> = _droneState.asStateFlow()

    private val _connectionEvent = MutableSharedFlow<ConnectionEvent>(replay = 1)
    val connectionEvent: SharedFlow<ConnectionEvent> = _connectionEvent.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var simJob: Job? = null

    fun init() {
        scope.launch {
            prefs.flow
                .map { it.simulatorMode }
                .distinctUntilChanged()
                .collect { simulator ->
                    if (simulator) startSimulator() else initReal()
                }
        }
    }

    private fun startSimulator() {
        simJob?.cancel()
        simJob = scope.launch {
            delay(800)
            _droneState.value = DroneState(
                isConnected = true,
                batteryPercent = SimulatorConfig.MOCK_BATTERY_START,
                gpsSignal = SimulatorConfig.MOCK_GPS_SAT,
                rtkLocked = false,
                rtkAccuracyM = 5.0,
                latitude = SimulatorConfig.MOCK_LAT,
                longitude = SimulatorConfig.MOCK_LON,
                altitudeM = 0.0,
                headingDegrees = 0.0,
                isFlying = false,
                flightMode = "GPS"
            )
            _connectionEvent.tryEmit(ConnectionEvent.Connected)

            delay(2200)
            _droneState.value = _droneState.value.copy(
                rtkLocked = true,
                rtkAccuracyM = SimulatorConfig.MOCK_RTK_ACCURACY_M,
                flightMode = "RTK"
            )
        }
    }

    private fun initReal() {
        simJob?.cancel()
        _droneState.value = DroneState()
        // TODO: real MSDK init
        // DJISDKManager.getInstance().registerApp(context, sdkCallback)
    }

    fun simulateBatteryDrain(deltaPercent: Int) {
        val s = _droneState.value
        _droneState.value = s.copy(
            batteryPercent = (s.batteryPercent - deltaPercent).coerceAtLeast(0)
        )
    }

    fun simulateMove(lat: Double, lon: Double, alt: Double, heading: Double) {
        val s = _droneState.value
        _droneState.value = s.copy(
            latitude = lat,
            longitude = lon,
            altitudeM = alt,
            headingDegrees = heading,
            isFlying = alt > 0.5,
            flightMode = if (s.rtkLocked) "RTK" else "GPS"
        )
    }

    fun setFlightMode(mode: String) {
        _droneState.value = _droneState.value.copy(flightMode = mode)
    }

    fun isReadyToFly(): Boolean {
        val s = _droneState.value
        val cfg = prefs.current
        return s.isConnected &&
            s.batteryPercent >= cfg.minBatteryPercent &&
            s.gpsSignal >= cfg.minGpsSatellites
    }

    fun shutdown() {
        simJob?.cancel()
    }
}
