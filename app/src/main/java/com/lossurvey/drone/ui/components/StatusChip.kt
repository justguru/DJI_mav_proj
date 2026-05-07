package com.lossurvey.drone.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lossurvey.drone.data.models.MissionStatus
import com.lossurvey.drone.data.models.SiteStatus
import com.lossurvey.drone.ui.theme.LOSColors

@Composable
fun StatusChip(status: MissionStatus, modifier: Modifier = Modifier) {
    val (color, label) = when (status) {
        MissionStatus.PENDING -> LOSColors.SecondaryText to "PENDING"
        MissionStatus.PREFLIGHT -> LOSColors.AccentWhite to "PREFLIGHT"
        MissionStatus.FLYING -> LOSColors.Warning to "FLYING"
        MissionStatus.PAUSED -> LOSColors.Warning to "PAUSED"
        MissionStatus.COMPLETED -> LOSColors.Success to "DONE"
        MissionStatus.ABORTED -> LOSColors.Error to "ABORTED"
    }
    ChipBox(color, label, modifier)
}

@Composable
fun SiteStatusChip(status: SiteStatus, modifier: Modifier = Modifier) {
    val (color, label) = when (status) {
        SiteStatus.PENDING -> LOSColors.SecondaryText to "PENDING"
        SiteStatus.FLYING -> LOSColors.Warning to "FLYING"
        SiteStatus.CAPTURING -> LOSColors.AccentWhite to "CAPTURING"
        SiteStatus.COMPLETED -> LOSColors.Success to "DONE"
        SiteStatus.FAILED -> LOSColors.Error to "FAILED"
    }
    ChipBox(color, label, modifier)
}

@Composable
private fun ChipBox(color: Color, label: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .border(BorderStroke(1.dp, color), RoundedCornerShape(4.dp))
            .padding(PaddingValues(horizontal = 10.dp, vertical = 4.dp))
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}
