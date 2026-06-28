package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = FrostLavender,
    secondary = FrostLavender,
    tertiary = FrostLavender,
    background = FrostDarkBackground,
    surface = FrostSurface,
    onPrimary = FrostDarkPurple,
    onSecondary = Color.White,
    onBackground = FrostTextPrimary,
    onSurface = FrostTextPrimary,
    onSurfaceVariant = FrostTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = VibeCyan,
    secondary = VibeRose,
    tertiary = VibeCyan,
    background = Color(0xFFF9F9FB),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF13111C),
    onSurface = Color(0xFF13111C),
    onSurfaceVariant = Color(0xFF626070)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme by default for VibeTune's sleek design
    dynamicColor: Boolean = false, // Disable dynamic colors by default to preserve the luxury brand identity
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
