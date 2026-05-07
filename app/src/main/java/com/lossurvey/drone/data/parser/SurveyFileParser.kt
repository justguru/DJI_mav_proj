package com.lossurvey.drone.data.parser

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.lossurvey.drone.data.models.Site
import com.lossurvey.drone.data.models.SurveyType
import com.opencsv.CSVReaderBuilder
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject

class SurveyFileParser @Inject constructor() {

    fun parseFile(uri: Uri, context: Context): ParserResult {
        return try {
            val fileName = getFileName(uri, context)
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return ParserResult.Error("Cannot open file")

            val rows: List<Map<String, String>> = inputStream.use { stream ->
                when {
                    fileName.endsWith(".xlsx", ignoreCase = true) -> parseXlsx(stream)
                    fileName.endsWith(".xls", ignoreCase = true) -> parseXlsx(stream)
                    fileName.endsWith(".csv", ignoreCase = true) -> parseCsv(stream)
                    else -> return ParserResult.Error("Unsupported file type. Use .csv or .xlsx")
                }
            }

            if (rows.isEmpty()) return ParserResult.Error("File is empty")

            val columns = rows.first().keys.map { it.uppercase() }
            val surveyType = when {
                columns.containsAll(listOf("TOWER_LAT", "TOWER_LON")) -> SurveyType.TOWER
                columns.contains("SURVEY_HEIGHT_M") -> SurveyType.GREENFIELD
                else -> return ParserResult.Error(
                    "Cannot detect survey type. Tower files need TOWER_LAT + TOWER_LON; " +
                        "Greenfield files need SURVEY_HEIGHT_M."
                )
            }

            val sites = rows.mapIndexedNotNull { index, row ->
                parseSiteRow(row, surveyType, index)
            }

            val errors = validateSites(sites)

            ParserResult.Success(
                surveyType = surveyType,
                sites = sites,
                validationErrors = errors,
                fileName = fileName
            )
        } catch (e: Exception) {
            ParserResult.Error("Parse failed: ${e.message ?: e.javaClass.simpleName}")
        }
    }

    private fun parseXlsx(stream: InputStream): List<Map<String, String>> {
        val workbook = WorkbookFactory.create(stream)
        try {
            val sheet = workbook.getSheetAt(0) ?: return emptyList()
            val headerRow = sheet.getRow(0) ?: return emptyList()
            val headers = (0 until headerRow.lastCellNum).map { idx ->
                headerRow.getCell(idx)?.stringCellValue?.trim()?.uppercase().orEmpty()
            }
            val rows = mutableListOf<Map<String, String>>()
            for (rowIndex in 1..sheet.lastRowNum) {
                val row = sheet.getRow(rowIndex) ?: continue
                val map = mutableMapOf<String, String>()
                headers.forEachIndexed { i, header ->
                    if (header.isNotEmpty()) {
                        map[header] = cellToString(row.getCell(i))
                    }
                }
                if (map.values.any { it.isNotEmpty() }) rows.add(map)
            }
            return rows
        } finally {
            workbook.close()
        }
    }

    private fun cellToString(cell: org.apache.poi.ss.usermodel.Cell?): String {
        if (cell == null) return ""
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue.trim()
            CellType.NUMERIC -> {
                val v = cell.numericCellValue
                if (v == v.toLong().toDouble()) v.toLong().toString() else v.toString()
            }
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> try { cell.stringCellValue.trim() } catch (_: Exception) {
                cell.numericCellValue.toString()
            }
            else -> ""
        }
    }

    private fun parseCsv(stream: InputStream): List<Map<String, String>> {
        val reader = CSVReaderBuilder(InputStreamReader(stream)).build()
        val allRows = reader.readAll()
        if (allRows.isEmpty()) return emptyList()
        val headers = allRows[0].map { it.trim().uppercase() }
        return allRows.drop(1).mapNotNull { row ->
            if (row.all { it.isBlank() }) return@mapNotNull null
            headers.zip(row.map { it.trim() }).toMap()
        }
    }

    private fun parseSiteRow(row: Map<String, String>, type: SurveyType, index: Int): Site? {
        return try {
            val siteId = row["SITE_ID"]?.takeIf { it.isNotBlank() }
                ?: "SITE${"%03d".format(index + 1)}"
            val lat = row["LATITUDE"]?.toDoubleOrNull() ?: return null
            val lon = row["LONGITUDE"]?.toDoubleOrNull() ?: return null

            when (type) {
                SurveyType.TOWER -> Site(
                    siteId = siteId,
                    missionId = 0,
                    latitude = lat,
                    longitude = lon,
                    type = type,
                    towerLatitude = row["TOWER_LAT"]?.toDoubleOrNull(),
                    towerLongitude = row["TOWER_LON"]?.toDoubleOrNull(),
                    heights = parseHeights(row["HEIGHTS_M"] ?: row["HEIGHT_M"] ?: ""),
                    azimuthDegrees = row["AZIMUTH_DEGREES"]?.toDoubleOrNull(),
                    cameraType = (row["CAMERA_TYPE"] ?: "WIDE").uppercase(),
                    distanceFromTower = row["DISTANCE_FROM_TOWER_M"]?.toDoubleOrNull() ?: 10.0
                )

                SurveyType.GREENFIELD -> Site(
                    siteId = siteId,
                    missionId = 0,
                    latitude = lat,
                    longitude = lon,
                    type = type,
                    surveyHeightM = row["SURVEY_HEIGHT_M"]?.toDoubleOrNull() ?: 30.0
                )
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseHeights(raw: String): List<Double> =
        raw.split(",", ";", "|").mapNotNull { it.trim().toDoubleOrNull() }

    private fun validateSites(sites: List<Site>): List<String> {
        val errors = mutableListOf<String>()
        sites.forEach { site ->
            if (site.latitude !in -90.0..90.0)
                errors.add("${site.siteId}: Invalid latitude ${site.latitude}")
            if (site.longitude !in -180.0..180.0)
                errors.add("${site.siteId}: Invalid longitude ${site.longitude}")
            if (site.type == SurveyType.TOWER) {
                if (site.heights.isEmpty())
                    errors.add("${site.siteId}: No heights defined")
                if (site.heights.any { it <= 0 || it > 120 })
                    errors.add("${site.siteId}: Height out of range (0-120m)")
                if (site.azimuthDegrees != null && site.azimuthDegrees !in 0.0..360.0)
                    errors.add("${site.siteId}: Invalid azimuth ${site.azimuthDegrees}")
                if (site.towerLatitude == null || site.towerLongitude == null)
                    errors.add("${site.siteId}: Missing tower coordinates")
            } else {
                val h = site.surveyHeightM
                if (h == null || h <= 0 || h > 120)
                    errors.add("${site.siteId}: Survey height invalid")
            }
        }
        return errors
    }

    private fun getFileName(uri: Uri, context: Context): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && nameIndex >= 0) it.getString(nameIndex) else "survey_file"
        } ?: uri.lastPathSegment ?: "survey_file"
    }
}
