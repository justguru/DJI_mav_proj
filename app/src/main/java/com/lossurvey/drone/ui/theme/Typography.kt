package com.lossurvey.drone.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.lossurvey.drone.R

val RajdhaniBold = FontFamily(Font(R.font.rajdhani_bold, FontWeight.Bold))
val RajdhaniMedium = FontFamily(Font(R.font.rajdhani_medium, FontWeight.Medium))
val IBMPlexMono = FontFamily(Font(R.font.ibm_plex_mono_regular, FontWeight.Normal))

val LOSTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = RajdhaniBold,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        letterSpacing = 2.sp,
        color = Color.White
    ),
    displayMedium = TextStyle(
        fontFamily = RajdhaniBold,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        letterSpacing = 1.5.sp,
        color = Color.White
    ),
    titleLarge = TextStyle(
        fontFamily = RajdhaniBold,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = 1.sp
    ),
    titleMedium = TextStyle(
        fontFamily = RajdhaniMedium,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp
    ),
    titleSmall = TextStyle(
        fontFamily = RajdhaniMedium,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.5.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = IBMPlexMono,
        fontSize = 15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = IBMPlexMono,
        fontSize = 13.sp
    ),
    bodySmall = TextStyle(
        fontFamily = IBMPlexMono,
        fontSize = 11.sp
    ),
    labelLarge = TextStyle(
        fontFamily = RajdhaniMedium,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = IBMPlexMono,
        fontSize = 12.sp,
        color = Color(0xFFA0A0A0)
    ),
    labelSmall = TextStyle(
        fontFamily = IBMPlexMono,
        fontSize = 10.sp,
        letterSpacing = 1.sp,
        color = Color(0xFFA0A0A0)
    )
)
