package com.smartspend.app.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
    val amber: Color = Color(0xFFE5A962),
    val amberSubtle: Color = Color(0xFFFAF0DE),
    val periwinkle: Color = Color(0xFF6C8EBF),
    val periwinkleSubtle: Color = Color(0xFFEAF1F8),
    val coral: Color = Color(0xFFD97D72),
    val coralSubtle: Color = Color(0xFFFCECE9),
    val sage: Color = Color(0xFF6B9A89),
    val sageSubtle: Color = Color(0xFFE8F2EE),
    val lavender: Color = Color(0xFF9D8EC2),
    val lavenderSubtle: Color = Color(0xFFF2EEF9)
)

// 1. CLASSIC PASTEL (LIGHT)
val LightAtelierPalette = AtelierColorPalette(
    canvas = LightAtelierCanvas,
    surfaceChalk = LightAtelierSurfaceChalk,
    surfaceChalkHigh = LightAtelierSurfaceChalkHigh,
    hairline = LightAtelierHairline,
    hairlineDark = LightAtelierHairlineDark,
    primaryInk = LightAtelierPrimaryInk,
    inkMuted = LightAtelierInkMuted,
    inkSubtle = LightAtelierInkSubtle,
    amber = Color(0xFFE5A962),
    amberSubtle = Color(0xFFFAF0DE),
    periwinkle = Color(0xFF6C8EBF),
    periwinkleSubtle = Color(0xFFEAF1F8),
    coral = Color(0xFFD97D72),
    coralSubtle = Color(0xFFFCECE9),
    sage = Color(0xFF6B9A89),
    sageSubtle = Color(0xFFE8F2EE),
    lavender = Color(0xFF9D8EC2),
    lavenderSubtle = Color(0xFFF2EEF9)
)

// 2. OBSIDIAN PASTEL (DARK)
val DarkAtelierPalette = AtelierColorPalette(
    canvas = DarkAtelierCanvas,
    surfaceChalk = DarkAtelierSurfaceChalk,
    surfaceChalkHigh = DarkAtelierSurfaceChalkHigh,
    hairline = DarkAtelierHairline,
    hairlineDark = DarkAtelierHairlineDark,
    primaryInk = DarkAtelierPrimaryInk,
    inkMuted = DarkAtelierInkMuted,
    inkSubtle = DarkAtelierInkSubtle,
    amber = Color(0xFFF0B86E),
    amberSubtle = Color(0xFF2C2216),
    periwinkle = Color(0xFF8EA8D6),
    periwinkleSubtle = Color(0xFF1B2433),
    coral = Color(0xFFE58B82),
    coralSubtle = Color(0xFF2E1B19),
    sage = Color(0xFF7EAB99),
    sageSubtle = Color(0xFF182A24),
    lavender = Color(0xFFB8A9DC),
    lavenderSubtle = Color(0xFF241D33)
)

// 3. PASTEL LAVENDER / MAUVE (LIGHT)
val LavenderLightPalette = AtelierColorPalette(
    canvas = Color(0xFFF8F6FB),
    surfaceChalk = Color(0xFFEFEBF6),
    surfaceChalkHigh = Color(0xFFE4DCF0),
    hairline = Color(0xFFDFD8EB),
    hairlineDark = Color(0xFFC3B8D6),
    primaryInk = Color(0xFF201B2E),
    inkMuted = Color(0xFF6D677D),
    inkSubtle = Color(0xFF9F98B0),
    amber = Color(0xFFE8BA7A),
    amberSubtle = Color(0xFFFAF2E4),
    periwinkle = Color(0xFF8B7BB5),
    periwinkleSubtle = Color(0xFFEFEAF8),
    coral = Color(0xFFD67C96),
    coralSubtle = Color(0xFFFCEBF0),
    sage = Color(0xFF729E8B),
    sageSubtle = Color(0xFFE9F3EE),
    lavender = Color(0xFF8B7BB5),
    lavenderSubtle = Color(0xFFEFEAF8)
)

// 4. PASTEL SAND & TERRACOTTA / DUNE (LIGHT)
val SandLightPalette = AtelierColorPalette(
    canvas = Color(0xFFFAF7F2),
    surfaceChalk = Color(0xFFF2EAE0),
    surfaceChalkHigh = Color(0xFFE8DED1),
    hairline = Color(0xFFE4DDD2),
    hairlineDark = Color(0xFFC9BEB0),
    primaryInk = Color(0xFF2C221A),
    inkMuted = Color(0xFF7A6F65),
    inkSubtle = Color(0xFFA69A8E),
    amber = Color(0xFFDFA35C),
    amberSubtle = Color(0xFFFAF0E1),
    periwinkle = Color(0xFF6E8FA8),
    periwinkleSubtle = Color(0xFFEBF1F5),
    coral = Color(0xFFD4836A),
    coralSubtle = Color(0xFFFCEEEA),
    sage = Color(0xFF739B87),
    sageSubtle = Color(0xFFE9F3ED),
    lavender = Color(0xFF9E88AF),
    lavenderSubtle = Color(0xFFF4EFF8)
)

// 5. PASTEL OCEAN & MIST / NORDIC (LIGHT)
val OceanLightPalette = AtelierColorPalette(
    canvas = Color(0xFFF4F8FA),
    surfaceChalk = Color(0xFFE7F0F5),
    surfaceChalkHigh = Color(0xFFDAE6EE),
    hairline = Color(0xFFDAE4EC),
    hairlineDark = Color(0xFFB5C6D4),
    primaryInk = Color(0xFF17232E),
    inkMuted = Color(0xFF657582),
    inkSubtle = Color(0xFF96A7B5),
    amber = Color(0xFFE5AE6C),
    amberSubtle = Color(0xFFFAF2E4),
    periwinkle = Color(0xFF5E93B5),
    periwinkleSubtle = Color(0xFFE8F3F8),
    coral = Color(0xFFD88373),
    coralSubtle = Color(0xFFFCEEDC),
    sage = Color(0xFF6FA398),
    sageSubtle = Color(0xFFE8F4F1),
    lavender = Color(0xFF938BB8),
    lavenderSubtle = Color(0xFFF0EDF8)
)

val LocalAtelierColors = staticCompositionLocalOf { LightAtelierPalette }

fun getAtelierPalette(themeMode: String, isSystemDark: Boolean): AtelierColorPalette {
    return when (themeMode.uppercase()) {
        "DARK" -> DarkAtelierPalette
        "LIGHT" -> LightAtelierPalette
        "PASTEL_LAVENDER" -> LavenderLightPalette
        "PASTEL_SAND" -> SandLightPalette
        "PASTEL_OCEAN" -> OceanLightPalette
        else -> if (isSystemDark) DarkAtelierPalette else LightAtelierPalette
    }
}

private fun createMaterialColorScheme(palette: AtelierColorPalette, isDark: Boolean) = if (isDark) {
    darkColorScheme(
        primary = palette.primaryInk,
        onPrimary = palette.canvas,
        primaryContainer = palette.surfaceChalk,
        onPrimaryContainer = palette.primaryInk,
        secondary = palette.periwinkle,
        onSecondary = palette.canvas,
        secondaryContainer = palette.periwinkleSubtle,
        onSecondaryContainer = palette.periwinkle,
        tertiary = palette.amber,
        onTertiary = palette.canvas,
        tertiaryContainer = palette.amberSubtle,
        onTertiaryContainer = palette.amber,
        background = palette.canvas,
        onBackground = palette.primaryInk,
        surface = palette.canvas,
        onSurface = palette.primaryInk,
        surfaceVariant = palette.surfaceChalk,
        onSurfaceVariant = palette.inkMuted,
        outline = palette.hairline,
        outlineVariant = palette.hairlineDark,
        error = palette.coral,
        onError = palette.canvas,
        errorContainer = palette.coralSubtle,
        onErrorContainer = palette.coral
    )
} else {
    lightColorScheme(
        primary = palette.primaryInk,
        onPrimary = palette.canvas,
        primaryContainer = palette.surfaceChalk,
        onPrimaryContainer = palette.primaryInk,
        secondary = palette.periwinkle,
        onSecondary = Color.White,
        secondaryContainer = palette.periwinkleSubtle,
        onSecondaryContainer = palette.periwinkle,
        tertiary = palette.amber,
        onTertiary = Color.White,
        tertiaryContainer = palette.amberSubtle,
        onTertiaryContainer = palette.amber,
        background = palette.canvas,
        onBackground = palette.primaryInk,
        surface = palette.canvas,
        onSurface = palette.primaryInk,
        surfaceVariant = palette.surfaceChalk,
        onSurfaceVariant = palette.inkMuted,
        outline = palette.hairline,
        outlineVariant = palette.hairlineDark,
        error = palette.coral,
        onError = Color.White,
        errorContainer = palette.coralSubtle,
        onErrorContainer = palette.coral
    )
}

@Composable
fun SmartSpendTheme(
    themeMode: String = "SYSTEM",
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val isEffectiveDark = when (themeMode.uppercase()) {
        "DARK" -> true
        "LIGHT", "PASTEL_LAVENDER", "PASTEL_SAND", "PASTEL_OCEAN" -> false
        else -> darkTheme
    }

    val palette = getAtelierPalette(themeMode, darkTheme)
    val colorScheme = createMaterialColorScheme(palette, isEffectiveDark)

    CompositionLocalProvider(LocalAtelierColors provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

