package com.lossurvey.drone.drone

import com.lossurvey.drone.data.models.DroneState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelemetryMonitor @Inject constructor(
    private val djiManager: DJIManager,
    private val rtkManager: RTKManager
) {
    val telemetry: Flow<DroneState> = djiManager.droneState

    val warnings: Flow<List<TelemetryWarning>> = djiManager.droneState.map { state ->
        buildList {
            if (state.isConnected && state.batteryPercent in 1..29) add(TelemetryWarning.LowBattery(state.batteryPercent))
            if (state.batteryPercent in 1..19) add(TelemetryWarning.CriticalBattery(state.batteryPercent))
            if (state.isConnected && state.gpsSignal < 4) add(TelemetryWarning.WeakGps(state.gpsSignal))
            if (state.isConnected && !state.rtkLocked) add(TelemetryWarning.RtkSearching)
        }
    }
}

sealed class TelemetryWarning(val label: String) {
    data class LowBattery(val percent: Int) : TelemetryWarning("BATTERY $percent%")
    data class CriticalBattery(val percent: Int) : TelemetryWarning("CRITICAL BAT $percent%")
    data class WeakGps(val sat: Int) : TelemetryWarning("GPS $sat/5")
    object RtkSearching : TelemetryWarning("RTK SEARCHING")
}
