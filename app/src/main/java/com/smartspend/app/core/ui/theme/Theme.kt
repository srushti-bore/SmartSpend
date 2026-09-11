package com.smartspend.app.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Stable
data class AtelierColorPalette(
    val canvas: Color,
    val surfaceChalk: Color,
    val surfaceChalkHigh: Color,
    val hairline: Color,
    val hairlineDark: Color,
    val primaryInk: Color,
    val inkMuted: Color,
    val inkSubtle: Color,
    val amber: Color = Color(0xFFE2A746),
    val amberSubtle: Color = Color(0xFFF6E6C9),
    val periwinkle: Color = Color(0xFF5B7598),
    val periwinkleSubtle: Color = Color(0xFFE8EDF5),
    val coral: Color = Color(0xFFD66853),
    val coralSubtle: Color = Color(0xFFFCE8E4),
    val sage: Color = Color(0xFF5E8B76),
    val sageSubtle: Color = Color(0xFFE6EFEA)
)

val LightAtelierPalette = AtelierColorPalette(
    canvas = LightAtelierCanvas,
    surfaceChalk = LightAtelierSurfaceChalk,
    surfaceChalkHigh = LightAtelierSurfaceChalkHigh,
    hairline = LightAtelierHairline,
    hairlineDark = LightAtelierHairlineDark,
    primaryInk = LightAtelierPrimaryInk,
    inkMuted = LightAtelierInkMuted,
    inkSubtle = LightAtelierInkSubtle,
    amber = Color(0xFFE2A746),
    amberSubtle = Color(0xFFF6E6C9),
    periwinkle = Color(0xFF5B7598),
    periwinkleSubtle = Color(0xFFE8EDF5),
    coral = Color(0xFFD66853),
    coralSubtle = Color(0xFFFCE8E4),
    sage = Color(0xFF5E8B76),
    sageSubtle = Color(0xFFE6EFEA)
)

val DarkAtelierPalette = AtelierColorPalette(
    canvas = DarkAtelierCanvas,
    surfaceChalk = DarkAtelierSurfaceChalk,
    surfaceChalkHigh = DarkAtelierSurfaceChalkHigh,
    hairline = DarkAtelierHairline,
    hairlineDark = DarkAtelierHairlineDark,
    primaryInk = DarkAtelierPrimaryInk,
    inkMuted = DarkAtelierInkMuted,
    inkSubtle = DarkAtelierInkSubtle,
    amber = Color(0xFFF0B355),
    amberSubtle = Color(0xFF3B2E1E),
    periwinkle = Color(0xFF7E9DC6),
    periwinkleSubtle = Color(0xFF1E293B),
    coral = Color(0xFFE57361),
    coralSubtle = Color(0xFF3E201B),
    sage = Color(0xFF6E9F88),
    sageSubtle = Color(0xFF1B3127)
)

val LocalAtelierColors = staticCompositionLocalOf { LightAtelierPalette }

private val LightColorScheme = lightColorScheme(
    primary = LightAtelierPrimaryInk,
    onPrimary = Color.White,
    primaryContainer = LightAtelierSurfaceChalk,
    onPrimaryContainer = LightAtelierPrimaryInk,
    secondary = Color(0xFF5B7598),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8EDF5),
    onSecondaryContainer = Color(0xFF5B7598),
    tertiary = Color(0xFFE2A746),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFF6E6C9),
    onTertiaryContainer = Color(0xFFE2A746),
    background = LightAtelierCanvas,
    onBackground = LightAtelierPrimaryInk,
    surface = LightAtelierCanvas,
    onSurface = LightAtelierPrimaryInk,
    surfaceVariant = LightAtelierSurfaceChalk,
    onSurfaceVariant = LightAtelierInkMuted,
    outline = LightAtelierHairline,
    outlineVariant = LightAtelierHairlineDark,
    error = Color(0xFFD66853),
    onError = Color.White,
    errorContainer = Color(0xFFFCE8E4),
    onErrorContainer = Color(0xFFD66853)
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkAtelierPrimaryInk,
    onPrimary = Color(0xFF14171B),
    primaryContainer = DarkAtelierSurfaceChalk,
    onPrimaryContainer = DarkAtelierPrimaryInk,
    secondary = Color(0xFF7E9DC6),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color(0xFF7E9DC6),
    tertiary = Color(0xFFF0B355),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF3B2E1E),
    onTertiaryContainer = Color(0xFFF0B355),
    background = DarkAtelierCanvas,
    onBackground = DarkAtelierPrimaryInk,
    surface = DarkAtelierCanvas,
    onSurface = DarkAtelierPrimaryInk,
    surfaceVariant = DarkAtelierSurfaceChalk,
    onSurfaceVariant = DarkAtelierInkMuted,
    outline = DarkAtelierHairline,
    outlineVariant = DarkAtelierHairlineDark,
    error = Color(0xFFE57361),
    onError = Color.White,
    errorContainer = Color(0xFF3E201B),
    onErrorContainer = Color(0xFFE57361)
)

@Composable
fun SmartSpendTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val atelierPalette = if (darkTheme) DarkAtelierPalette else LightAtelierPalette

    CompositionLocalProvider(LocalAtelierColors provides atelierPalette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
