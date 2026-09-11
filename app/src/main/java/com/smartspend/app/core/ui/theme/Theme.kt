package com.smartspend.app.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = BrandAccent.copy(alpha = 0.15f),
    onPrimaryContainer = BrandSecondary,
    secondary = BrandSecondary,
    onSecondary = Color.White,
    background = BrandBackground,
    onBackground = TextPrimary,
    surface = BrandSurface,
    onSurface = TextPrimary,
    surfaceVariant = BrandSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    error = StatusDanger,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandAccent,
    onPrimary = Color.Black,
    secondary = BrandPrimary,
    onSecondary = Color.White,
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFA0A0A0),
    error = StatusDanger,
    onError = Color.White
)

@Composable
fun SmartSpendTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
