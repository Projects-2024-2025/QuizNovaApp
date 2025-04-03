package com.technovix.quiznova.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// --- New Dark Color Scheme ---
private val DarkColorScheme = darkColorScheme(
    primary = VibrantPurple,           // Main vibrant color
    onPrimary = TextColorDark,         // Text on primary (SoftIvory)
    primaryContainer = Color(0xFF4D2C9A), // Darker purple container
    onPrimaryContainer = SoftIvory.copy(alpha = 0.9f),
    secondary = SkyBlue,               // Secondary accent
    onSecondary = DeepNavy,            // Dark text on light blue
    secondaryContainer = Color(0xFF004A70), // Darker blue container
    onSecondaryContainer = SkyBlue.copy(alpha = 0.9f),
    tertiary = VibrantPink,            // Tertiary accent
    onTertiary = Color.White,          // White text on pink
    tertiaryContainer = Color(0xFFB0003A), // Darker pink container
    onTertiaryContainer = VibrantPink.copy(alpha = 0.9f),
    background = BackgroundDark,       // DeepNavy
    onBackground = TextColorDark,      // SoftIvory on DeepNavy
    surface = SurfaceDark,             // Slightly lighter navy/purple
    onSurface = TextColorDark,         // SoftIvory on SurfaceDark
    surfaceVariant = SurfaceDark.copy(alpha = 0.7f), // For cards, slightly different
    onSurfaceVariant = SubtleGrayDark, // Mid-tone gray text on surface variant
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRed.copy(alpha = 0.2f),
    onErrorContainer = ErrorRed.copy(alpha = 0.9f),
    outline = OutlineDark,             // Subtle gray outline
    outlineVariant = OutlineDark.copy(alpha = 0.5f) // Slightly stronger outline variant
    // inversePrimary, surfaceTint etc. can be left default or customized if needed
)

// --- New Light Color Scheme ---
private val LightColorScheme = lightColorScheme(
    primary = VibrantPurple,           // Main vibrant color
    onPrimary = Color.White,           // White text on purple
    primaryContainer = VibrantPurple.copy(alpha = 0.15f), // Light purple container
    onPrimaryContainer = VibrantPurple.copy(alpha = 0.8f), // Darker purple text
    secondary = SkyBlue,               // Secondary accent
    onSecondary = DeepNavy,            // Dark text on light blue
    secondaryContainer = SkyBlue.copy(alpha = 0.15f), // Light blue container
    onSecondaryContainer = Color(0xFF005A80), // Darker blue text
    tertiary = VibrantPink,            // Tertiary accent
    onTertiary = Color.White,          // White text on pink
    tertiaryContainer = VibrantPink.copy(alpha = 0.15f), // Light pink container
    onTertiaryContainer = Color(0xFF9A0036), // Darker pink text
    background = BackgroundLight,      // SoftIvory
    onBackground = TextColorLight,     // DeepNavy on SoftIvory
    surface = SurfaceLight,            // NearWhite
    onSurface = TextColorLight,        // DeepNavy on NearWhite
    surfaceVariant = SubtleGrayLight.copy(alpha = 0.2f), // Very subtle gray for cards
    onSurfaceVariant = TextColorLight.copy(alpha = 0.7f), // Slightly faded dark text
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRed.copy(alpha = 0.1f),
    onErrorContainer = ErrorRed.copy(alpha = 0.9f),
    outline = OutlineLight,            // Subtle light gray outline
    outlineVariant = OutlineLight.copy(alpha = 0.6f) // Slightly stronger outline variant
)

@Composable
fun QuizAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Dynamic color still optional
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Status bar matches background, use dark icons on light bg and vice-versa
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            // Navigation bar also matches background
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Assuming Typography.kt is defined
        shapes = Shapes,       // Assuming Shapes.kt is defined
        content = content
    )
}