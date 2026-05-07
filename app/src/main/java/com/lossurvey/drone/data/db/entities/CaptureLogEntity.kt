package com.lossurvey.drone.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "capture_logs",
    indices = [Index("missionId"), Index("siteId")]
)
data class CaptureLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val missionId: Long,
    val siteId: String,
    val imagePath: String,
    val latitude: Double,
    val longitude: Double,
    val altitudeM: Double,
    val azimuthDegrees: Double,
    val timestamp: Long,
    val rtkAccuracyM: Double,
    val batteryPercent: Int,
    val cameraType: String
)
