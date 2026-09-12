package com.smartspend.app.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

// ==========================================
// 1. CLASSIC PASTEL TOKENS (LIGHT & DARK)
// ==========================================

// Light Classic Pastel Tokens
val LightAtelierCanvas = Color(0xFFFBFBF9)
val LightAtelierSurfaceChalk = Color(0xFFF3F3EF)
val LightAtelierSurfaceChalkHigh = Color(0xFFE8E8E3)
val LightAtelierHairline = Color(0xFFE2E0D8)
val LightAtelierHairlineDark = Color(0xFFC8C7C0)
val LightAtelierPrimaryInk = Color(0xFF1E232A)
val LightAtelierInkMuted = Color(0xFF6C727D)
val LightAtelierInkSubtle = Color(0xFF9CA1AB)

// Dark Obsidian Pastel Tokens
val DarkAtelierCanvas = Color(0xFF121519)
val DarkAtelierSurfaceChalk = Color(0xFF1A1E24)
val DarkAtelierSurfaceChalkHigh = Color(0xFF242A33)
val DarkAtelierHairline = Color(0xFF2B323D)
val DarkAtelierHairlineDark = Color(0xFF404A59)
val DarkAtelierPrimaryInk = Color(0xFFF0F2F5)
val DarkAtelierInkMuted = Color(0xFF9FA6B2)
val DarkAtelierInkSubtle = Color(0xFF727A87)

// ==========================================
// 2. DYNAMIC THEME COMPOSITION ACCESSORS
// ==========================================

val AtelierCanvas: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAtelierColors.current.canvas

val AtelierSurfaceChalk: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAtelierColors.current.surfaceChalk

val AtelierSurfaceChalkHigh: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAtelierColors.current.surfaceChalkHigh

val AtelierHairline: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAtelierColors.current.hairline

val AtelierHairlineDark: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAtelierColors.current.hairlineDark

val AtelierPrimaryInk: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAtelierColors.current.primaryInk

val AtelierInkMuted: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAtelierColors.current.inkMuted

val AtelierInkSubtle: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAtelierColors.current.inkSubtle

// Semantic Pastel Accents (Derived sRGB Harmonics)
val AtelierAmber: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAtelierColors.current.amber

val AtelierAmberSubtle: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAtelierColors.current.amberSubtle

val AtelierPeriwinkle: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAtelierColors.current.periwinkle

val AtelierPeriwinkleSubtle: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAtelierColors.current.periwinkleSubtle

val AtelierCoral: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAtelierColors.current.coral

val AtelierCoralSubtle: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAtelierColors.current.coralSubtle

val AtelierSage: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAtelierColors.current.sage

val AtelierSageSubtle: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAtelierColors.current.sageSubtle

val AtelierLavender: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAtelierColors.current.lavender

val AtelierLavenderSubtle: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalAtelierColors.current.lavenderSubtle

// Compatibility mappings
val BrandPrimary: Color @Composable @ReadOnlyComposable get() = AtelierPrimaryInk
val BrandSecondary: Color @Composable @ReadOnlyComposable get() = AtelierPeriwinkle
val BrandAccent: Color @Composable @ReadOnlyComposable get() = AtelierAmber
val BrandBackground: Color @Composable @ReadOnlyComposable get() = AtelierCanvas
val BrandSurface: Color @Composable @ReadOnlyComposable get() = AtelierCanvas
val BrandSurfaceVariant: Color @Composable @ReadOnlyComposable get() = AtelierSurfaceChalk

val StatusSuccess: Color @Composable @ReadOnlyComposable get() = AtelierSage
val StatusWarning: Color @Composable @ReadOnlyComposable get() = AtelierAmber
val StatusDanger: Color @Composable @ReadOnlyComposable get() = AtelierCoral
val StatusInfo: Color @Composable @ReadOnlyComposable get() = AtelierPeriwinkle

val TextPrimary: Color @Composable @ReadOnlyComposable get() = AtelierPrimaryInk
val TextSecondary: Color @Composable @ReadOnlyComposable get() = AtelierInkMuted
val TextTertiary: Color @Composable @ReadOnlyComposable get() = AtelierInkSubtle

val PastelPink: Color @Composable @ReadOnlyComposable get() = AtelierCoralSubtle
val PastelBlue: Color @Composable @ReadOnlyComposable get() = AtelierPeriwinkleSubtle
val PastelGreen: Color @Composable @ReadOnlyComposable get() = AtelierSageSubtle
val PastelYellow: Color @Composable @ReadOnlyComposable get() = AtelierAmberSubtle
val PastelPurple: Color @Composable @ReadOnlyComposable get() = AtelierLavenderSubtle
val PastelOrange: Color @Composable @ReadOnlyComposable get() = AtelierAmberSubtle
val PastelTeal: Color @Composable @ReadOnlyComposable get() = AtelierSageSubtle

