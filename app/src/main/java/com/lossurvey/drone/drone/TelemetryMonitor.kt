package com.lossurvey.drone.drone

import com.lossurvey.drone.data.models.DroneState
import com.lossurvey.drone.data.preferences.AppPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelemetryMonitor @Inject constructor(
    private val djiManager: DJIManager,
    private val rtkManager: RTKManager,
    private val prefs: AppPreferences
) {
    val telemetry: Flow<DroneState> = djiManager.droneState

    val warnings: Flow<List<TelemetryWarning>> =
        combine(djiManager.droneState, prefs.flow) { state, settings ->
            buildList {
                if (state.isConnected && state.batteryPercent in 1..settings.criticalBatteryPercent)
                    add(TelemetryWarning.CriticalBattery(state.batteryPercent))
                else if (state.isConnected && state.batteryPercent in 1 until settings.minBatteryPercent)
                    add(TelemetryWarning.LowBattery(state.batteryPercent))
                if (state.isConnected && state.gpsSignal < settings.minGpsSatellites)
                    add(TelemetryWarning.WeakGps(state.gpsSignal))
                if (state.isConnected && !state.rtkLocked && !settings.allowGpsFallback)
                    add(TelemetryWarning.RtkSearching)
            }
        }
}

sealed class TelemetryWarning(val label: String) {
    data class LowBattery(val percent: Int) : TelemetryWarning("BATTERY $percent%")
    data class CriticalBattery(val percent: Int) : TelemetryWarning("CRITICAL BAT $percent%")
    data class WeakGps(val sat: Int) : TelemetryWarning("GPS $sat/5")
    object RtkSearching : TelemetryWarning("RTK SEARCHING")
}
