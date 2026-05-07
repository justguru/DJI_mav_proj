package com.lossurvey.drone.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun CaptureFlash(visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(60)),
        exit = fadeOut(tween(220))
    ) {
        Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.85f)))
    }
}
