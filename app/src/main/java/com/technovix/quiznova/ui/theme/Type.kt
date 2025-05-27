package com.technovix.quiznova.ui.theme // Paket adınızı kontrol edin

import android.content.res.Configuration
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// 2. Responsive Tipografi Oluşturucu Fonksiyon
@Composable
fun getResponsiveTypography(
    configuration: Configuration = LocalConfiguration.current,
    // Varsayılan olarak yukarıda tanımladığımız AppTypography'yi kullanacak
    baseTypography: Typography = Typography
): Typography {
    val screenWidthDp = configuration.screenWidthDp.dp

    // Çok dar ekranlar için (Örn: Genişliği 360dp'den küçük telefonlar)
    if (screenWidthDp < 360.dp) {
        return baseTypography.copy(
            headlineLarge = baseTypography.headlineLarge.copy(fontSize = 28.sp, lineHeight = 34.sp), // 34 -> 28
            headlineMedium = baseTypography.headlineMedium.copy(fontSize = 24.sp, lineHeight = 30.sp), // 28 -> 24
            headlineSmall = baseTypography.headlineSmall.copy(fontSize = 20.sp, lineHeight = 26.sp),   // 24 -> 20
            titleLarge = baseTypography.titleLarge.copy(fontSize = 18.sp, lineHeight = 24.sp),     // 22 -> 18
            titleMedium = baseTypography.titleMedium.copy(fontSize = 15.sp, lineHeight = 20.sp),   // 18 -> 15
            titleSmall = baseTypography.titleSmall.copy(fontSize = 13.sp, lineHeight = 18.sp),     // 14 -> 13
            bodyLarge = baseTypography.bodyLarge.copy(fontSize = 14.sp, lineHeight = 20.sp),       // 17 -> 14
            bodyMedium = baseTypography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),     // 15 -> 13
            bodySmall = baseTypography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),       // 13 -> 11
            labelLarge = baseTypography.labelLarge.copy(fontSize = 12.sp, lineHeight = 18.sp),     // 14 -> 12
            labelMedium = baseTypography.labelMedium.copy(fontSize = 11.sp, lineHeight = 15.sp),   // 12 -> 11
            labelSmall = baseTypography.labelSmall.copy(fontSize = 10.sp, lineHeight = 14.sp)      // 11 -> 10
        )
    }

    // Daha geniş ekranlar / Tabletler (Örn: Genişliği 600dp ve üzeri)
    // Bu kesme noktası Materyal Tasarım'da "Medium" genişlik sınıfının başlangıcıdır.
    else if (screenWidthDp >= 600.dp) {
        return baseTypography.copy(
            headlineLarge = baseTypography.headlineLarge.copy(fontSize = 38.sp, lineHeight = 44.sp), // 34 -> 38
            headlineMedium = baseTypography.headlineMedium.copy(fontSize = 32.sp, lineHeight = 38.sp), // 28 -> 32
            headlineSmall = baseTypography.headlineSmall.copy(fontSize = 28.sp, lineHeight = 34.sp),   // 24 -> 28
            titleLarge = baseTypography.titleLarge.copy(fontSize = 26.sp, lineHeight = 32.sp),     // 22 -> 26
            titleMedium = baseTypography.titleMedium.copy(fontSize = 20.sp, lineHeight = 26.sp),   // 18 -> 20
            bodyLarge = baseTypography.bodyLarge.copy(fontSize = 18.sp, lineHeight = 26.sp),       // 17 -> 18
            bodyMedium = baseTypography.bodyMedium.copy(fontSize = 16.sp, lineHeight = 22.sp)      // 15 -> 16
            // Diğer stiller (bodySmall, label'lar) için de artışlar eklenebilir.
        )
    }

    return baseTypography
}