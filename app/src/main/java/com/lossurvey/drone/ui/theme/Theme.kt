package com.lossurvey.drone.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.app.Activity

private val LOSColorScheme = darkColorScheme(
    background = LOSColors.Background,
    onBackground = LOSColors.PrimaryText,
    surface = LOSColors.Surface2,
    onSurface = LOSColors.PrimaryText,
    surfaceVariant = LOSColors.Surface3,
    onSurfaceVariant = LOSColors.SecondaryText,
    primary = LOSColors.AccentWhite,
    onPrimary = LOSColors.Background,
    secondary = LOSColors.SecondaryText,
    onSecondary = LOSColors.PrimaryText,
    tertiary = LOSColors.AccentWhite,
    onTertiary = LOSColors.Background,
    error = LOSColors.Error,
    onError = LOSColors.PrimaryText,
    outline = LOSColors.Border,
    outlineVariant = LOSColors.Surface3
)

@Composable
fun LOSSurveyTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = LOSColors.Surface1.toArgb()
            window.navigationBarColor = LOSColors.Surface1.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = LOSColorScheme,
        typography = LOSTypography,
        shapes = LOSShapes,
        content = content
    )
}
