package com.lossurvey.drone.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lossurvey.drone.data.models.Mission
import com.lossurvey.drone.data.models.SurveyType
import com.lossurvey.drone.ui.theme.LOSColors
import kotlinx.coroutines.launch

@Composable
fun MissionCard(mission: Mission, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale.value)
            .clickable {
                scope.launch {
                    scale.animateTo(0.97f, spring(stiffness = Spring.StiffnessHigh))
                    scale.animateTo(1f, spring(stiffness = Spring.StiffnessMedium))
                    onClick()
                }
            },
        colors = CardDefaults.cardColors(containerColor = LOSColors.Surface2),
        border = BorderStroke(1.dp, LOSColors.Border),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(48.dp)
                    .background(LOSColors.Surface3, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (mission.type == SurveyType.TOWER)
                        Icons.Default.CellTower else Icons.Default.Terrain,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    mission.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Spacer(Modifier.size(2.dp))
                Text(
                    "${mission.sites.size} SITES  •  ${mission.type.name}",
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(Modifier.width(8.dp))
            StatusChip(mission.status)
        }
    }
}
