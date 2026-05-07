package com.lossurvey.drone.report

import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.AreaBreak
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.AreaBreakType
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.io.font.constants.StandardFonts
import com.lossurvey.drone.data.db.entities.CaptureLogEntity
import com.lossurvey.drone.data.models.Mission
import com.lossurvey.drone.data.models.Site
import com.lossurvey.drone.data.models.SiteStatus
import com.lossurvey.drone.data.models.SurveyType
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfReportGenerator @Inject constructor() {

    fun generateReport(
        mission: Mission,
        sites: List<Site>,
        captures: List<CaptureLogEntity>,
        projectFolder: String
    ): File {
        val safeName = mission.name.replace(Regex("[^a-zA-Z0-9_-]"), "_").ifEmpty { "mission" }
        val outputFile = File(projectFolder, "report_$safeName.pdf")
        PdfWriter(outputFile).use { writer ->
            val pdf = PdfDocument(writer)
            val document = Document(pdf, PageSize.A4)
            document.setMargins(40f, 40f, 40f, 40f)

            val boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)
            val regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA)
            val monoFont = PdfFontFactory.createFont(StandardFonts.COURIER)

            val black = DeviceRgb(10, 10, 10)
            val darkGrey = DeviceRgb(30, 30, 30)
            val midGrey = DeviceRgb(160, 160, 160)
            val white = DeviceRgb(255, 255, 255)

            // HEADER
            val headerTable = Table(UnitValue.createPercentArray(floatArrayOf(1f))).useAllAvailableWidth()
            headerTable.addCell(
                Cell().add(
                    Paragraph("LINE-OF-SIGHT SURVEY REPORT")
                        .setFont(boldFont).setFontSize(22f).setFontColor(white)
                ).setBackgroundColor(black).setPadding(20f).setBorder(Border.NO_BORDER)
            )
            headerTable.addCell(
                Cell().add(
                    Paragraph("Mission: ${mission.name}  |  Type: ${mission.type}  |  Sites: ${sites.size}")
                        .setFont(regularFont).setFontSize(10f).setFontColor(midGrey)
                ).setBackgroundColor(darkGrey).setPadding(10f).setBorder(Border.NO_BORDER)
            )
            document.add(headerTable)
            document.add(Paragraph("\n"))

            // SUMMARY
            document.add(Paragraph("MISSION SUMMARY").setFont(boldFont).setFontSize(14f))
            val summaryTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 2f))).useAllAvailableWidth()
            val df = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            listOf(
                "Mission Name" to mission.name,
                "Survey Type" to mission.type.name,
                "Total Sites" to "${sites.size}",
                "Completed Sites" to "${sites.count { it.status == SiteStatus.COMPLETED }}",
                "Generated" to df.format(Date()),
                "Total Captures" to "${captures.size}"
            ).forEach { (label, value) ->
                summaryTable.addCell(Cell().add(Paragraph(label).setFont(boldFont).setFontSize(10f)))
                summaryTable.addCell(Cell().add(Paragraph(value).setFont(monoFont).setFontSize(10f)))
            }
            document.add(summaryTable)
            document.add(Paragraph("\n"))

            // PER SITE
            sites.forEachIndexed { siteIndex, site ->
                val siteCaptures = captures.filter { it.siteId == site.siteId }
                document.add(
                    Paragraph("SITE: ${site.siteId}")
                        .setFont(boldFont).setFontSize(13f).setFontColor(white)
                        .setBackgroundColor(darkGrey).setPadding(8f)
                )
                val coordTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 2f))).useAllAvailableWidth()
                val rows = mutableListOf<Pair<String, String>>().apply {
                    add("Latitude" to "%.7f".format(site.latitude))
                    add("Longitude" to "%.7f".format(site.longitude))
                    add("Survey Type" to site.type.name)
                    add("Camera" to site.cameraType)
                    if (site.type == SurveyType.TOWER) {
                        add("Heights" to site.heights.joinToString(", ") { "${it}m" })
                        site.azimuthDegrees?.let { add("Azimuth" to "${it.toInt()}°") }
                        site.towerLatitude?.let { add("Tower Lat" to "%.7f".format(it)) }
                        site.towerLongitude?.let { add("Tower Lon" to "%.7f".format(it)) }
                    } else {
                        add("Survey Height" to "${site.surveyHeightM}m")
                    }
                    add("Captures" to "${siteCaptures.size}")
                    add("Status" to site.status.name)
                }
                rows.forEach { (label, value) ->
                    coordTable.addCell(Cell().add(Paragraph(label).setFont(boldFont).setFontSize(9f)))
                    coordTable.addCell(Cell().add(Paragraph(value).setFont(monoFont).setFontSize(9f)))
                }
                document.add(coordTable)

                if (siteCaptures.isNotEmpty()) {
                    document.add(Paragraph("\nCaptured Images:").setFont(boldFont).setFontSize(10f))
                    val imageTable = Table(UnitValue.createPercentArray(floatArrayOf(1f, 1f))).useAllAvailableWidth()
                    siteCaptures.chunked(2).forEach { pair ->
                        pair.forEach { capture ->
                            val imageFile = File(capture.imagePath)
                            val cell = Cell().setPadding(4f)
                            if (imageFile.exists()) {
                                runCatching {
                                    val img = Image(ImageDataFactory.create(capture.imagePath))
                                        .setAutoScale(true)
                                    cell.add(img)
                                }
                            }
                            cell.add(
                                Paragraph(
                                    "Az: ${capture.azimuthDegrees.toInt()}° | Alt: ${capture.altitudeM.toInt()}m\n" +
                                            "RTK: ±${"%.3f".format(capture.rtkAccuracyM)}m | Bat: ${capture.batteryPercent}%"
                                ).setFont(monoFont).setFontSize(8f)
                            )
                            imageTable.addCell(cell)
                        }
                        if (pair.size == 1) imageTable.addCell(Cell())
                    }
                    document.add(imageTable)
                }
                if (siteIndex < sites.lastIndex) {
                    document.add(AreaBreak(AreaBreakType.NEXT_PAGE))
                }
            }

            document.close()
        }
        return outputFile
    }
}
