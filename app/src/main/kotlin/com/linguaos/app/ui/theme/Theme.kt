package com.linguaos.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary          = BrandIndigo,
    onPrimary        = Color.White,
    primaryContainer = Color(0xFFE0E7FF),
    onPrimaryContainer = BrandIndigoDark,
    secondary        = BrandGreen,
    onSecondary      = Color.White,
    tertiary         = BrandAmber,
    background       = Slate50,
    onBackground     = Slate900,
    surface          = Color.White,
    onSurface        = Slate900,
    surfaceVariant   = Slate100,
    onSurfaceVariant = Slate600,
    outline          = Slate300,
    error            = BrandRose,
    onError          = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary          = Color(0xFF818CF8),
    onPrimary        = Slate900,
    primaryContainer = Color(0xFF3730A3),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary        = BrandGreen,
    onSecondary      = Slate900,
    tertiary         = BrandAmber,
    background       = Slate950,
    onBackground     = Slate100,
    surface          = Slate900,
    onSurface        = Slate100,
    surfaceVariant   = Slate800,
    onSurfaceVariant = Slate400,
    outline          = Slate700,
    error            = Color(0xFFFCA5A5),
    onError          = Slate900,
)

@Composable
fun LinguaOSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = AppTypography,
        content     = content
    )
}
