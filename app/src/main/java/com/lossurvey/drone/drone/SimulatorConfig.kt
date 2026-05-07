package com.lossurvey.drone.drone

/**
 * Internal constants for simulator behaviour. The on/off toggle now lives in
 * AppPreferences (Settings screen) and is read at runtime.
 */
object SimulatorConfig {
    const val MOCK_BATTERY_START = 92
    const val MOCK_GPS_SAT = 5
    const val MOCK_RTK_ACCURACY_M = 0.018
    const val MOCK_LAT = 53.3498
    const val MOCK_LON = -6.2603
    const val MOCK_DRAIN_PER_CAPTURE_PCT = 1
}
