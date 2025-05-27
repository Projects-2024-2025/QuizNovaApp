package com.technovix.quiznova.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.technovix.quiznova.util.ThemePreference

// DarkColorScheme ve LightColorScheme tanımlarınız burada olmalı (önceki kodunuzdaki gibi)
private val DarkColorScheme = darkColorScheme(
    primary = VibrantPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4D2C9A),
    onPrimaryContainer = SoftIvory.copy(alpha = 0.9f),
    secondary = SkyBlue,
    onSecondary = DeepNavy,
    secondaryContainer = Color(0xFF004A70),
    onSecondaryContainer = SkyBlue.copy(alpha = 0.9f),
    tertiary = VibrantPink,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFB0003A),
    onTertiaryContainer = VibrantPink.copy(alpha = 0.9f),
    background = BackgroundDark,
    onBackground = TextColorDark,
    surface = SurfaceDark,
    onSurface = TextColorDark,
    surfaceVariant = SurfaceDark.copy(alpha = 0.7f),
    onSurfaceVariant = SubtleGrayDark,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRed.copy(alpha = 0.2f),
    onErrorContainer = ErrorRed.copy(alpha = 0.9f),
    outline = OutlineDark,
    outlineVariant = OutlineDark.copy(alpha = 0.5f),
    inverseSurface = SurfaceLight,
    inverseOnSurface = TextColorLight
)

private val LightColorScheme = lightColorScheme(
    primary = VibrantPurple,
    onPrimary = Color.White,
    primaryContainer = VibrantPurple.copy(alpha = 0.15f),
    onPrimaryContainer = VibrantPurple.copy(alpha = 0.8f),
    secondary = SkyBlue,
    onSecondary = DeepNavy,
    secondaryContainer = SkyBlue.copy(alpha = 0.15f),
    onSecondaryContainer = Color(0xFF005A80),
    tertiary = VibrantPink,
    onTertiary = Color.White,
    tertiaryContainer = VibrantPink.copy(alpha = 0.15f),
    onTertiaryContainer = Color(0xFF9A0036),
    background = BackgroundLight,
    onBackground = TextColorLight,
    surface = SurfaceLight,
    onSurface = TextColorLight,
    surfaceVariant = SubtleGrayLight.copy(alpha = 0.2f),
    onSurfaceVariant = TextColorLight.copy(alpha = 0.7f),
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRed.copy(alpha = 0.1f),
    onErrorContainer = ErrorRed.copy(alpha = 0.9f),
    outline = OutlineLight,
    outlineVariant = OutlineLight.copy(alpha = 0.6f),
    inverseSurface = SurfaceDark,
    inverseOnSurface = TextColorDark
)


// lightAppBackgroundGradient ve darkAppBackgroundGradient fonksiyonlarınız burada (değişiklik yok)
@Composable
fun lightAppBackgroundGradient(): Brush {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val background = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    return Brush.verticalGradient(
        colors = listOf(
            primary.copy(alpha = 0.4f),
            secondary.copy(alpha = 0.3f),
            surfaceVariant.copy(alpha = 0.6f),
            background.copy(alpha = 0.9f)
        ),
        startY = 0f,
    )
}

@Composable
fun darkAppBackgroundGradient(): Brush {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val surface = MaterialTheme.colorScheme.surface
    val background = MaterialTheme.colorScheme.background

    return Brush.verticalGradient(
        colors = listOf(
            primary.copy(alpha = 0.25f),
            secondary.copy(alpha = 0.15f),
            surface.copy(alpha = 0.4f),
            background
        )
    )
}


@Composable
fun QuizAppTheme(
    themePreference: ThemePreference = ThemePreference.LIGHT, // themePreference parametresi olarak alınıyor
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themePreference) {
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }

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
            val window = (view.context as? Activity)?.window ?: return@SideEffect // Güvenli cast
            window.statusBarColor = colorScheme.background.toArgb() // Veya istediğiniz başka bir renk
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            window.navigationBarColor = colorScheme.background.toArgb() // Veya istediğiniz başka bir renk
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    val responsiveTypography = getResponsiveTypography(baseTypography = Typography) // <<<--- DEĞİŞİKLİK

    MaterialTheme(
        colorScheme = colorScheme,
        typography = responsiveTypography,
        shapes = Shapes,
        content = content
    )
}