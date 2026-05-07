package com.lossurvey.drone.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.lossurvey.drone.data.models.DroneState
import com.lossurvey.drone.data.models.Site
import com.lossurvey.drone.ui.theme.LOSColors

/**
 * Light-weight schematic plot of sites + drone position.
 * Replace with Mapbox Composable once the Mapbox token is configured in
 * res/values/strings.xml (mapbox_access_token).
 */
@Composable
fun MissionMapPreview(
    droneState: DroneState,
    sites: List<Site>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .background(LOSColors.Surface1, RoundedCornerShape(12.dp))
            .border(1.dp, LOSColors.Border, RoundedCornerShape(12.dp))
    ) {
        Canvas(Modifier.fillMaxSize().padding(12.dp)) {
            val pts = sites.map { it.latitude to it.longitude } +
                if (droneState.isConnected) listOf(droneState.latitude to droneState.longitude) else emptyList()
            if (pts.isEmpty()) return@Canvas

            val lats = pts.map { it.first }
            val lons = pts.map { it.second }
            val minLat = lats.min(); val maxLat = lats.max()
            val minLon = lons.min(); val maxLon = lons.max()
            val latRange = (maxLat - minLat).coerceAtLeast(0.0005)
            val lonRange = (maxLon - minLon).coerceAtLeast(0.0005)

            fun project(lat: Double, lon: Double): Offset {
                val x = ((lon - minLon) / lonRange).toFloat() * size.width
                val y = (1f - ((lat - minLat) / latRange).toFloat()) * size.height
                return Offset(x.coerceIn(8f, size.width - 8f), y.coerceIn(8f, size.height - 8f))
            }

            // Grid
            val grid = Color(0xFF222222)
            for (i in 1..3) {
                drawLine(
                    grid,
                    Offset(0f, size.height * i / 4f),
                    Offset(size.width, size.height * i / 4f),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                )
                drawLine(
                    grid,
                    Offset(size.width * i / 4f, 0f),
                    Offset(size.width * i / 4f, size.height),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                )
            }

            // Sites
            sites.forEachIndexed { _, site ->
                val p = project(site.latitude, site.longitude)
                drawCircle(LOSColors.AccentWhite, radius = 8f, center = p)
                drawCircle(
                    LOSColors.Surface3,
                    radius = 8f,
                    center = p,
                    style = Stroke(2f)
                )
            }

            // Drone
            if (droneState.isConnected) {
                val d = project(droneState.latitude, droneState.longitude)
                drawCircle(LOSColors.Success, radius = 10f, center = d)
                drawCircle(
                    LOSColors.Success.copy(alpha = 0.25f),
                    radius = 22f,
                    center = d
                )
            }
        }
        Text(
            "MAP PREVIEW (replace with Mapbox once token set)",
            style = MaterialTheme.typography.labelSmall,
            color = LOSColors.SecondaryText,
            modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)
        )
    }
}
