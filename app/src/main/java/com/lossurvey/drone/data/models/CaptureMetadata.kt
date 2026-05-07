package com.lossurvey.drone.data.models

import kotlinx.serialization.Serializable

@Serializable
data class CaptureMetadata(
    val siteId: String,
    val surveyType: String,
    val latitude: Double,
    val longitude: Double,
    val altitudeM: Double,
    val azimuthDegrees: Double,
    val cameraPitchDegrees: Double,
    val cameraType: String,
    val imagePath: String,
    val timestamp: Long,
    val rtkAccuracyM: Double,
    val batteryPercent: Int
)
