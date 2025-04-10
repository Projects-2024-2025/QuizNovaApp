package com.technovix.quiznova.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(8.dp),      // Küçük elemanlar, butonlar için
    medium = RoundedCornerShape(12.dp),    // Kartlar, dialoglar için
    large = RoundedCornerShape(16.dp),     // Daha büyük kartlar, bottom sheet'ler için
    extraLarge = RoundedCornerShape(28.dp) // Carousel item gibi çok büyük elemanlar için
)