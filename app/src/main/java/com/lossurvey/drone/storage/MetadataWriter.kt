package com.lossurvey.drone.storage

import com.lossurvey.drone.data.models.CaptureMetadata
import com.lossurvey.drone.data.models.FlightLogEntry
import com.lossurvey.drone.data.models.Site
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Collections
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetadataWriter @Inject constructor(
    private val json: Json
) {
    private val flightLog: MutableList<FlightLogEntry> = Collections.synchronizedList(mutableListOf())
    private val captures: MutableList<CaptureMetadata> = Collections.synchronizedList(mutableListOf())

    @Synchronized
    fun addCapture(metadata: CaptureMetadata, projectFolder: String) {
        captures.add(metadata)
        File(projectFolder, "metadata.json").writeText(
            json.encodeToString(ListSerializer(CaptureMetadata.serializer()), captures.toList())
        )
    }

    @Synchronized
    fun writeSiteMetadata(site: Site, projectFolder: String) {
        File(projectFolder, "metadata.json").writeText(
            json.encodeToString(ListSerializer(CaptureMetadata.serializer()), captures.toList())
        )
    }

    @Synchronized
    fun logFlightEvent(entry: FlightLogEntry, projectFolder: String) {
        flightLog.add(entry)
        File(projectFolder, "flight_log.json").writeText(
            json.encodeToString(ListSerializer(FlightLogEntry.serializer()), flightLog.toList())
        )
    }

    fun reset() {
        flightLog.clear()
        captures.clear()
    }
}
