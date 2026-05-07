package com.lossurvey.drone.data.models

data class DroneState(
    val isConnected: Boolean = false,
    val batteryPercent: Int = 0,
    val gpsSignal: Int = 0,
    val rtkLocked: Boolean = false,
    val rtkAccuracyM: Double = 0.0,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitudeM: Double = 0.0,
    val headingDegrees: Double = 0.0,
    val isFlying: Boolean = false,
    val flightMode: String = "UNKNOWN"
)
