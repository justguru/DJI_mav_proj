package com.lossurvey.drone.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
fun TelemetryBar(state: DroneState, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .background(LOSColors.Surface1)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TelemetryItem("BAT", "${state.batteryPercent}%",
            when {
                state.batteryPercent > 50 -> LOSColors.Success
                state.batteryPercent > 25 -> LOSColors.Warning
                else -> LOSColors.Error
            })
        TelemetryItem("GPS", "${state.gpsSignal}/5", LOSColors.AccentWhite)
        TelemetryItem("ALT", "${state.altitudeM.toInt()}M", LOSColors.AccentWhite)
        TelemetryItem("HDG", "${state.headingDegrees.toInt()}°", LOSColors.AccentWhite)
        TelemetryItem(
            "RTK",
            if (state.rtkLocked) "FIX ±${"%.2f".format(state.rtkAccuracyM)}m" else "SEARCH",
            if (state.rtkLocked) LOSColors.Success else LOSColors.Warning
        )
        TelemetryItem("MODE", state.flightMode, LOSColors.SecondaryText)
    }
}

@Composable
fun TelemetryItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(2.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
