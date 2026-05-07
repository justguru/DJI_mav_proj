package com.lossurvey.drone.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lossurvey.drone.data.models.DroneState
import com.lossurvey.drone.ui.theme.LOSColors

@Composable
fun DroneStatusCard(state: DroneState, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LOSColors.Surface2),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (state.isConnected) {
                    PulsingDot(LOSColors.Success)
                } else {
                    OfflineDot()
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    if (state.isConnected) "DJI MAVIC 3E — CONNECTED" else "NO DRONE CONNECTED",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (state.isConnected) Color.White else LOSColors.Error
                )
            }

            if (state.isConnected) {
                Spacer(Modifier.height(14.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatusMetric(
                        "BATTERY", "${state.batteryPercent}%",
                        when {
                            state.batteryPercent > 50 -> LOSColors.Success
                            state.batteryPercent > 25 -> LOSColors.Warning
                            else -> LOSColors.Error
                        }
                    )
                    StatusMetric("GPS", "${state.gpsSignal}/5 SAT", LOSColors.AccentWhite)
                    StatusMetric(
                        "RTK",
                        if (state.rtkLocked) "FIXED" else "SEARCH",
                        if (state.rtkLocked) LOSColors.Success else LOSColors.Warning
                    )
                    StatusMetric("MODE", state.flightMode, LOSColors.SecondaryText)
                }
            }
        }
    }
}

@Composable
fun StatusMetric(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = FontWeight.Bold
        )
    }
}
