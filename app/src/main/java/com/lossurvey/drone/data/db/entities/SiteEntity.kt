package com.lossurvey.drone.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sites",
    foreignKeys = [
        ForeignKey(
            entity = MissionEntity::class,
            parentColumns = ["id"],
            childColumns = ["missionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("missionId")]
)
data class SiteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val missionId: Long,
    val siteId: String,
    val latitude: Double,
    val longitude: Double,
    val type: String,
    val towerLatitude: Double? = null,
    val towerLongitude: Double? = null,
    val heightsJson: String = "[]",
    val azimuthDegrees: Double? = null,
    val cameraType: String = "WIDE",
    val distanceFromTower: Double = 10.0,
    val surveyHeightM: Double? = null,
    val status: String = "PENDING",
    val capturedCount: Int = 0
)
