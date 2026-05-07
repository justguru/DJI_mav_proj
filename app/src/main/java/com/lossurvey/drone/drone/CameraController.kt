package com.lossurvey.drone.drone

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Captures photos with proper lens / pitch / exposure setup.
 *
 * Simulator mode writes a synthesized JPEG containing the metadata stamp.
 * REAL-DRONE WIRING:
 *  - val camera = (DJISDKManager.product as Aircraft).camera
 *  - camera.setActiveLens(WIDE | ZOOM); camera.setShootPhotoMode(SINGLE)
 *  - aircraft.gimbal.rotate(Rotation.Builder().pitch(...).mode(ABSOLUTE_ANGLE)...build())
 *  - camera.setExposureMode(PROGRAM); camera.startShootPhoto { ... }
 */
@Singleton
class CameraController @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun configureAndCapture(
        cameraType: String,
        pitchDegrees: Double,
        targetFile: File,
        stamp: CaptureStamp
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            if (SimulatorConfig.ENABLED) {
                writePlaceholderImage(targetFile, stamp)
                delay(400)
                return@withContext Result.success(targetFile)
            }

            // TODO: REAL CAPTURE
            // 1. select lens
            // 2. set photo mode
            // 3. rotate gimbal
            // 4. exposure
            // 5. startShootPhoto and download via media manager into targetFile
            delay(800)
            Result.success(targetFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun writePlaceholderImage(file: File, stamp: CaptureStamp) {
        val w = 1280
        val h = 720
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(Color.rgb(20, 20, 20))

        val border = Paint().apply {
            color = Color.rgb(232, 232, 232); style = Paint.Style.STROKE; strokeWidth = 4f
        }
        canvas.drawRect(20f, 20f, (w - 20).toFloat(), (h - 20).toFloat(), border)

        val title = Paint().apply {
            color = Color.WHITE; textSize = 56f; isAntiAlias = true; isFakeBoldText = true
        }
        val body = Paint().apply {
            color = Color.rgb(200, 200, 200); textSize = 30f; isAntiAlias = true; typeface =
                android.graphics.Typeface.MONOSPACE
        }
        val accent = Paint().apply {
            color = Color.rgb(76, 175, 80); textSize = 30f; isAntiAlias = true; typeface =
                android.graphics.Typeface.MONOSPACE
        }

        canvas.drawText("LOS SURVEY • SIMULATED CAPTURE", 60f, 110f, title)
        canvas.drawText("SITE   ${stamp.siteId}", 60f, 200f, body)
        canvas.drawText("TYPE   ${stamp.surveyType}", 60f, 250f, body)
        canvas.drawText("LAT    ${"%.7f".format(stamp.lat)}", 60f, 300f, body)
        canvas.drawText("LON    ${"%.7f".format(stamp.lon)}", 60f, 350f, body)
        canvas.drawText("ALT    ${"%.1f".format(stamp.altM)} m", 60f, 400f, body)
        canvas.drawText("AZ     ${"%.0f".format(stamp.azimuth)} deg", 60f, 450f, body)
        canvas.drawText("CAM    ${stamp.cameraType}  PITCH ${"%.0f".format(stamp.pitch)}", 60f, 500f, body)
        canvas.drawText("RTK    ±${"%.3f".format(stamp.rtkAcc)} m", 60f, 550f, accent)
        canvas.drawText("BAT    ${stamp.battery}%", 60f, 600f, body)
        canvas.drawText("TS     ${stamp.timestamp}", 60f, 650f, body)

        FileOutputStream(file).use { out ->
            bmp.compress(Bitmap.CompressFormat.JPEG, 88, out)
        }
        bmp.recycle()
    }

    data class CaptureStamp(
        val siteId: String,
        val surveyType: String,
        val lat: Double,
        val lon: Double,
        val altM: Double,
        val azimuth: Double,
        val pitch: Double,
        val cameraType: String,
        val rtkAcc: Double,
        val battery: Int,
        val timestamp: Long
    )
}
