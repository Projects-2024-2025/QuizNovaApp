package com.technovix.quiznova.ui.theme

import androidx.compose.ui.graphics.Color

// --- Core Vibrant Palette ---
val VibrantPurple = Color(0xFF703ACF) // Blue Iris - Primary candidate
val DeepNavy = Color(0xFF050A30)       // Dark Blue - Good for dark backgrounds/text
val BrightBlue = Color(0xFF0A84FF)     // Keeping a vibrant blue option (was PrimaryBlue)
val SkyBlue = Color(0xFF7EC8E3)        // Baby Blue - Secondary candidate
val VibrantPink = Color(0xFFE91E63)      // Tertiary / Accent
val SoftIvory = Color(0xFFFFF2FF)      // Light background/surface
val NearWhite = Color(0xFFFAFAFF)     // Slightly cooler white for surface

// --- Status & Feedback Colors ---
val SuccessGreen = Color(0xFF2ECC71)    // Emerald Green - More vibrant success
val ErrorRed = Color(0xFFFF453A)        // iOS Red - Keep vibrant error
val WarningOrange = Color(0xFFFF9F0A)   // iOS Orange - Good for warnings or tertiary
val HighlightYellow = Color(0xFFFFD60A) // iOS Gold/Yellow - For achievements

// --- Neutral Shades (Derived) ---
// Light Theme Neutrals
val TextColorLight = DeepNavy           // Dark text on light background
val SubtleGrayLight = Color(0xFFB0B8D4) // Lighter gray derived from blue tones
val OutlineLight = SubtleGrayLight.copy(alpha = 0.4f)
val TrackLight = SubtleGrayLight.copy(alpha = 0.2f)

// Dark Theme Neutrals
val TextColorDark = SoftIvory           // Light text on dark background
val SubtleGrayDark = Color(0xFF8A94BC)  // Mid-tone gray derived from blue/purple
val OutlineDark = SubtleGrayDark.copy(alpha = 0.3f)
val TrackDark = SubtleGrayDark.copy(alpha = 0.2f)

// --- Backgrounds/Surfaces ---
// Light Theme
val BackgroundLight = SoftIvory
val SurfaceLight = NearWhite

// Dark Theme
val BackgroundDark = DeepNavy // Use the deep navy instead of black
val SurfaceDark = Color(0xFF101848) // Slightly lighter navy/purple for cards/sheets

// --- OLD COLORS (Can be removed if no longer referenced elsewhere) ---
/*
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
val PrimaryBlue = Color(0xFF0A84FF)
val PrimaryVariantBlue = Color(0xFF005ecb)
val SecondaryGreen = Color(0xFF30D158)
val SecondaryVariantGreen = Color(0xFF249a41)
val TertiaryOrange = Color(0xFFFF9F0A)
val BackgroundLightOld = Color(0xFFF8F9FA)
val SurfaceLightOld = Color.White
val OnBackgroundLightOld = Color(0xFF1C1C1E)
val OnSurfaceLightOld = Color(0xFF1C1C1E)
val BackgroundDarkOld = Color(0xFF000000)
val SurfaceDarkOld = Color(0xFF1C1C1E)
val OnBackgroundDarkOld = Color(0xFFEBEBF5)
val OnSurfaceDarkOld = Color(0xFFEBEBF5)
val CorrectGreenOld = SecondaryGreen
val WrongRedOld = Color(0xFFFF453A)
val NeutralGrayOld = Color(0xFF8A8A8E)
val GoldYellowOld = Color(0xFFFFD60A)
val OutlineGrayOld = NeutralGrayOld.copy(alpha = 0.3f)
val TrackColorOld = NeutralGrayOld.copy(alpha = 0.2f)
*/

// --- New Vibrant Lists for Categories ---
// Use Primary, Secondary, Tertiary (or other defined vibrant colors)
val vibrantCardColors = listOf(
    VibrantPurple.copy(alpha = 0.18f), // Slightly more presence
    SkyBlue.copy(alpha = 0.20f),
    VibrantPink.copy(alpha = 0.18f),
    BrightBlue.copy(alpha = 0.18f) // Added another option
)

val vibrantIconColors = listOf(
    VibrantPurple,
    SkyBlue,
    VibrantPink,
    BrightBlue // Match the card colors
)