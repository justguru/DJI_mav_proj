package com.lossurvey.drone.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lossurvey.drone.ui.theme.LOSColors

@Composable
fun PulsingDot(
    color: Color,
    size: Dp = 10.dp,
    pulse: Boolean = true,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "pulse")
    val alpha by transition.animateFloat(
        initialValue = if (pulse) 0.35f else 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    androidx.compose.foundation.layout.Box(
        modifier
            .size(size)
            .background(color = color.copy(alpha = if (pulse) alpha else 1f), shape = CircleShape)
    )
}

@Composable
fun StaticDot(color: Color, size: Dp = 10.dp, modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Box(
        modifier
            .size(size)
            .background(color = color, shape = CircleShape)
    )
}

@Composable
fun OfflineDot(modifier: Modifier = Modifier) =
    StaticDot(LOSColors.Error, modifier = modifier)
