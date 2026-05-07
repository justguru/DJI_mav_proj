package com.lossurvey.drone.data.models

data class Site(
    val id: Long = 0,
    val missionId: Long,
    val siteId: String,
    val latitude: Double,
    val longitude: Double,
    val type: SurveyType,

    // Type 1 — Tower fields
    val towerLatitude: Double? = null,
    val towerLongitude: Double? = null,
    val heights: List<Double> = emptyList(),
    val azimuthDegrees: Double? = null,
    val cameraType: String = "WIDE",
    val distanceFromTower: Double = 10.0,

    // Type 2 — Greenfield fields
    val surveyHeightM: Double? = null,

    val status: SiteStatus = SiteStatus.PENDING,
    val capturedCount: Int = 0
)

enum class SiteStatus { PENDING, FLYING, CAPTURING, COMPLETED, FAILED }
