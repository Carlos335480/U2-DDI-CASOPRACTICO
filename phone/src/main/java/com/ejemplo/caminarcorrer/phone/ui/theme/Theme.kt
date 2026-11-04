package com.ejemplo.caminarcorrer.phone.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF0B6E4F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA8F0D0),
    onPrimaryContainer = Color(0xFF002115),
    secondary = Color(0xFFE07A1F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDCC2),
    onSecondaryContainer = Color(0xFF2D1600),
    background = Color(0xFFF7FBF8),
    onBackground = Color(0xFF191C1A),
    surface = Color(0xFFF7FBF8),
    onSurface = Color(0xFF191C1A),
    surfaceVariant = Color(0xFFDCE5DF),
    onSurfaceVariant = Color(0xFF404944),
    error = Color(0xFFBA1A1A)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8CD5B5),
    onPrimary = Color(0xFF003825),
    primaryContainer = Color(0xFF005138),
    onPrimaryContainer = Color(0xFFA8F0D0),
    secondary = Color(0xFFFFB77C),
    onSecondary = Color(0xFF4A2800),
    secondaryContainer = Color(0xFF6A3C00),
    onSecondaryContainer = Color(0xFFFFDCC2),
    background = Color(0xFF0F1512),
    onBackground = Color(0xFFDFE4E0),
    surface = Color(0xFF0F1512),
    onSurface = Color(0xFFDFE4E0),
    surfaceVariant = Color(0xFF404944),
    onSurfaceVariant = Color(0xFFBFC9C2),
    error = Color(0xFFFFB4AB)
)

@Composable
fun CaminarCorrerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
