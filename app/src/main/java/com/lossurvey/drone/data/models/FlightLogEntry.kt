package com.lossurvey.drone.data.models

import kotlinx.serialization.Serializable

@Serializable
data class FlightLogEntry(
    val timestamp: Long,
    val event: String,
    val siteId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val batteryPercent: Int = 0,
    val rtkAccuracy: Double = 0.0,
    val errorMessage: String = ""
)
