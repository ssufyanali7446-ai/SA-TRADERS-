package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Color(0xFF1A1100),
    primaryContainer = Color(0xFF3E2D0B),
    onPrimaryContainer = GoldLight,
    secondary = CyanAccent,
    onSecondary = Color(0xFF001A26),
    secondaryContainer = Color(0xFF0B2A3E),
    onSecondaryContainer = Color(0xFFBAE6FD),
    tertiary = BullGreen,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = BearRed,
    onError = Color.White
)

private val LightColorScheme = DarkColorScheme // Always default to professional trading dark theme

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
