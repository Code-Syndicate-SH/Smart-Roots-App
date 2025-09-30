package com.example.smarthydro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable


private val LightColors = lightColorScheme(
    primary = SO_Primary_L, onPrimary = Color.White,
    secondary = SO_Secondary_L, onSecondary = Color.White,
    tertiary = SO_Tertiary_L, onTertiary = Color.White,
    background = SO_Bg_L, onBackground = SO_OnSurf_L,
    surface = SO_Surf_L, onSurface = SO_OnSurf_L,
    surfaceVariant = SO_SurfVar_L, onSurfaceVariant = SO_OnSurf_L.copy(alpha = 0.80f),
    outline = SO_OnSurf_L.copy(alpha = 0.20f)
)

private val DarkColors = darkColorScheme(
    primary = SO_Primary_D, onPrimary = Color.Black,
    secondary = SO_Secondary_D, onSecondary = Color.Black,
    tertiary = SO_Tertiary_D, onTertiary = Color.Black,
    background = SO_Bg_D, onBackground = SO_OnSurf_D,
    surface = SO_Surf_D, onSurface = SO_OnSurf_D,
    surfaceVariant = SO_SurfVar_D, onSurfaceVariant = SO_OnSurf_D.copy(alpha = 0.85f),
    outline = SO_OnSurf_D.copy(alpha = 0.24f)
)

@Composable
fun SmartHydroTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        shapes = Shapes,
        content = content
    )
}