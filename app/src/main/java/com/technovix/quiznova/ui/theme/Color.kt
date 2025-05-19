package com.technovix.quiznova.ui.theme

import androidx.compose.ui.graphics.Color
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Quiz için özel renkler (Burada tanımlamak daha merkezi olur)
val PositiveGreen = Color(0xFF3DD598)
val SuccessGreen = Color(0xFF4CAF50) // Başarı rengi
val ErrorRed = Color(0xFFF44336)     // Hata/Yanlış rengi
val HighlightYellow = Color(0xFFFFC107) // Vurgu/Mükemmel rengi

// Gradientler için renkler (Zaten muhtemelen burada veya Theme.kt'dedir)
val LightStart = Color(0xFFE1F5FE) // Açık tema başlangıç
val LightEnd = Color(0xFFB3E5FC) // Açık tema bitiş
val DarkStart = Color(0xFF0D1B2A) // Koyu tema başlangıç
val DarkEnd = Color(0xFF1B263B) // Koyu tema bitiş
// --- Yeni Çekirdek Canlı Palet ---
val VibrantPurple = Color(0xFF703ACF) // Ana renk adayı (Mavi İris gibi)
val DeepNavy = Color(0xFF050A30)       // Koyu Lacivert - Koyu tema arka planları/metinler için iyi
val BrightBlue = Color(0xFF0A84FF)     // Canlı mavi seçeneği (eski PrimaryBlue)
val SkyBlue = Color(0xFF7EC8E3)        // Bebek Mavisi - İkincil renk adayı
val VibrantPink = Color(0xFFE91E63)      // Üçüncül / Vurgu rengi
val SoftIvory = Color(0xFFFFF2FF)      // Açık tema arka planı/yüzeyi (Hafif mor tonlu)
val NearWhite = Color(0xFFFAFAFF)      // Yüzey için biraz daha soğuk beyaz


// --- Nötr Tonlar (Türetilmiş) ---
// Açık Tema Nötrleri
val TextColorLight = DeepNavy           // Açık arka planda koyu metin
val SubtleGrayLight = Color(0xFFB0B8D4) // Mavi tonlarından türetilmiş daha açık gri
val OutlineLight = SubtleGrayLight.copy(alpha = 0.4f)
val TrackLight = SubtleGrayLight.copy(alpha = 0.2f)

// Koyu Tema Nötrleri
val TextColorDark = SoftIvory           // Koyu arka planda açık metin
val SubtleGrayDark = Color(0xFF8A94BC)  // Mavi/mor tonlarından türetilmiş orta ton gri
val OutlineDark = SubtleGrayDark.copy(alpha = 0.3f)
val TrackDark = SubtleGrayDark.copy(alpha = 0.2f)

// --- Arka Planlar/Yüzeyler ---
// Açık Tema
val BackgroundLight = SoftIvory
val SurfaceLight = NearWhite

// Koyu Tema
val BackgroundDark = DeepNavy // Siyah yerine koyu lacivert
val SurfaceDark = Color(0xFF101848) // Kartlar/sayfalar için biraz daha açık lacivert/mor

// --- Kategori Kartları/İkonları İçin Canlı Renk Listesi ---
// Bu liste çeşitlilik sağlamak için iyidir. Renklerin kendisi canlı ve uyumlu olmalı.
val vibrantIconColors = listOf(
    Color(0xFFF44336), // Kırmızı
    Color(0xFFE91E63), // Pembe
    Color(0xFF9C27B0), // Mor
    Color(0xFF673AB7), // Koyu Mor
    Color(0xFF3F51B5), // İndigo
    Color(0xFF2196F3), // Mavi
    Color(0xFF03A9F4), // Açık Mavi
    Color(0xFF00BCD4), // Camgöbeği
    Color(0xFF009688), // Teal
    Color(0xFF4CAF50), // Yeşil
    Color(0xFF8BC34A), // Açık Yeşil
    Color(0xFFCDDC39), // Lime
    Color(0xFFFFEB3B), // Sarı
    Color(0xFFFFC107), // Amber
    Color(0xFFFF9800), // Turuncu
    Color(0xFF795548), // Kahverengi
    Color(0xFF607D8B)  // Mavi Gri
)

