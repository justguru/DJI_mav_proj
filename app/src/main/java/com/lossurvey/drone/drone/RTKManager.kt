package com.lossurvey.drone.drone

import android.content.Context
import com.lossurvey.drone.data.preferences.AppPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class RTKState(
    val isConnected: Boolean = false,
    val isFixed: Boolean = false,
    val horizontalAccuracyM: Double = 99.0,
    val satelliteCount: Int = 0
)

/**
 * Tracks RTK module status. In simulator mode it mirrors DJIManager's drone state.
 *
 * REAL-DRONE WIRING:
 *  - aircraft.flightController.rtk.setStateCallback { state -> ... }
 *  - state.positioningSolution == PositioningSolution.FIXED_POINT → isFixed = true
 *  - read state.mobileStationLocation.horizontalAccuracy
 */
@Singleton
class RTKManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val djiManager: DJIManager,
    private val prefs: AppPreferences
) {
    private val _rtkState = MutableStateFlow(RTKState())
    val rtkState: StateFlow<RTKState> = _rtkState.asStateFlow()

    fun init() {
        // Real RTK callbacks subscribe here when SDK is wired in.
    }

    val isLocked: Boolean
        get() = if (prefs.current.simulatorMode) djiManager.droneState.value.rtkLocked
                else _rtkState.value.isFixed

    val accuracy: Double
        get() = if (prefs.current.simulatorMode) djiManager.droneState.value.rtkAccuracyM
                else _rtkState.value.horizontalAccuracyM
}
