package com.example.smarthydro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = SO_Primary_L,
    secondary = SO_Secondary_L,
    tertiary = SO_Tertiary_L,
    background = SO_Bg_L,
    surface = SO_Surf_L,
    onSurface = SO_OnSurf_L,
    error = SO_Danger,
)

private val DarkColorScheme = darkColorScheme(
    primary = SO_Primary_D,
    secondary = SO_Secondary_D,
    tertiary = SO_Tertiary_D,
    background = SO_Bg_D,
    surface = SO_Surf_D,
    onSurface = SO_OnSurf_D,
    error = SO_Danger,
)

@Composable
fun SmartHydroTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography(),
        shapes = Shapes,
        content = content
    )
}