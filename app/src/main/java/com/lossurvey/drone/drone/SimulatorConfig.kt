package com.lossurvey.drone.drone

object SimulatorConfig {
    /**
     * Set to true to run without a real DJI drone. Emits mock telemetry and
     * runs missions as fake progress with placeholder image captures.
     * Toggle to false once a real DJI Mavic 3E + RC Pro is wired in and the
     * MSDK v5 dependencies are uncommented in app/build.gradle.kts.
     */
    const val ENABLED = true

    const val MOCK_BATTERY_START = 92
    const val MOCK_GPS_SAT = 5
    const val MOCK_RTK_ACCURACY_M = 0.018
    const val MOCK_LAT = 53.3498
    const val MOCK_LON = -6.2603
    const val MOCK_DRAIN_PER_CAPTURE_PCT = 1
}
