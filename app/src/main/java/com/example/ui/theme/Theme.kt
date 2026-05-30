package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberDarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color(0xFF1D1B20),
    primaryContainer = CyberPurple,
    onPrimaryContainer = CyberCyan,
    secondary = MutedLabel,
    onSecondary = Color(0xFF313033),
    secondaryContainer = Color(0xFF313033),
    onSecondaryContainer = Color(0xFFE2E2E6),
    tertiary = CyberGreen,
    onTertiary = Color(0xFF1A1C1E),
    tertiaryContainer = Color(0xFF2D2F33),
    onTertiaryContainer = Color(0xFFE2E2E6),
    background = MidnightDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    error = CyberRed,
    onError = Color(0xFF601410)
)

private val CyberLightColorScheme = lightColorScheme(
    primary = Color(0xFF6750A4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEADDFF),
    onPrimaryContainer = Color(0xFF21005D),
    secondary = Color(0xFF625B71),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF386A20),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB1F18F),
    onTertiaryContainer = Color(0xFF042100),
    background = CyberLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) CyberDarkColorScheme else CyberLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
