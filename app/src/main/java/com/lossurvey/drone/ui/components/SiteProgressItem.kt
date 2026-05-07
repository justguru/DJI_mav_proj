package com.lossurvey.drone.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lossurvey.drone.data.models.Site
import com.lossurvey.drone.data.models.SurveyType
import com.lossurvey.drone.ui.theme.LOSColors

@Composable
fun SiteProgressItem(
    site: Site,
    isCurrent: Boolean,
    capturesAtSite: Int,
    expectedCaptures: Int,
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        isCurrent -> LOSColors.AccentWhite
        site.status.name == "COMPLETED" -> LOSColors.Success
        site.status.name == "FAILED" -> LOSColors.Error
        else -> LOSColors.Border
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) LOSColors.Surface3 else LOSColors.Surface2
        ),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    site.siteId,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White
                )
                Spacer(Modifier.weight(1f))
                SiteStatusChip(site.status)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "${"%.5f".format(site.latitude)}, ${"%.5f".format(site.longitude)}",
                style = MaterialTheme.typography.bodySmall,
                color = LOSColors.SecondaryText
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (site.type == SurveyType.TOWER)
                    "TOWER • H: ${site.heights.joinToString(",") { it.toInt().toString() }}m • Az ${site.azimuthDegrees?.toInt() ?: 0}°"
                else
                    "GREENFIELD • H: ${site.surveyHeightM?.toInt() ?: 0}m • 12 captures",
                style = MaterialTheme.typography.bodySmall,
                color = LOSColors.SecondaryText
            )
            if (isCurrent && expectedCaptures > 0) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = (capturesAtSite.toFloat() / expectedCaptures).coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth(),
                    color = LOSColors.AccentWhite,
                    trackColor = LOSColors.Border
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "$capturesAtSite / $expectedCaptures captured",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
