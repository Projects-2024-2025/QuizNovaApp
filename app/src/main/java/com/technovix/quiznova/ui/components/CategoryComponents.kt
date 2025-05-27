package com.technovix.quiznova.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// Kategori İkonlarını Eşleştirme Fonksiyonu
fun getIconForCategory(categoryName: String): ImageVector {
    val nameToCheck = categoryName // Veya gerekirse burada da kısaltma yapılabilir, ama genellikle gerekmez.
    return when {
        nameToCheck.contains("Knowledge", ignoreCase = true) -> Icons.Filled.AutoStories
        nameToCheck.contains("Books", ignoreCase = true) -> Icons.Filled.LibraryBooks
        nameToCheck.contains("Film", ignoreCase = true) -> Icons.Filled.Theaters
        nameToCheck.contains("Music", ignoreCase = true) -> Icons.Filled.MusicNote
        nameToCheck.contains("Musicals & Theatres", ignoreCase = true) -> Icons.Filled.TheaterComedy
        nameToCheck.contains("Television", ignoreCase = true) -> Icons.Filled.Tv
        nameToCheck.contains("Video Games", ignoreCase = true) -> Icons.Filled.VideogameAsset
        nameToCheck.contains("Board Games", ignoreCase = true) -> Icons.Filled.Casino
        nameToCheck.contains("Science & Nature", ignoreCase = true) -> Icons.Filled.Science
        nameToCheck.contains("Computers", ignoreCase = true) -> Icons.Filled.Computer
        nameToCheck.contains("Mathematics", ignoreCase = true) -> Icons.Filled.Calculate
        nameToCheck.contains("Mythology", ignoreCase = true) -> Icons.Filled.Fort
        nameToCheck.contains("Sports", ignoreCase = true) -> Icons.Filled.SportsBasketball
        nameToCheck.contains("Geography", ignoreCase = true) -> Icons.Filled.Public
        nameToCheck.contains("History", ignoreCase = true) -> Icons.Filled.Museum
        nameToCheck.contains("Politics", ignoreCase = true) -> Icons.Filled.Gavel
        nameToCheck.contains("Art", ignoreCase = true) -> Icons.Filled.Palette
        nameToCheck.contains("Celebrities", ignoreCase = true) -> Icons.Filled.Star
        nameToCheck.contains("Animals", ignoreCase = true) -> Icons.Filled.Pets
        nameToCheck.contains("Vehicles", ignoreCase = true) -> Icons.Filled.DirectionsCar
        nameToCheck.contains("Comics", ignoreCase = true) -> Icons.Filled.MenuBook
        nameToCheck.contains("Gadgets", ignoreCase = true) -> Icons.Filled.PhonelinkSetup
        nameToCheck.contains("Japanese Anime & Manga", ignoreCase = true) -> Icons.Filled.Face
        nameToCheck.contains("Cartoon & Animations", ignoreCase = true) -> Icons.Filled.Animation
        else -> Icons.Filled.Category // Varsayılan ikon
    }
}

// İkonlar ve Kart Arka Planları İçin Renk Paleti
val vibrantIconColors = listOf(
    Color(0xFFEF5350), Color(0xFFEC407A), Color(0xFFAB47BC), Color(0xFF7E57C2),
    Color(0xFF5C6BC0), Color(0xFF42A5F5), Color(0xFF29B6F6), Color(0xFF26C6DA),
    Color(0xFF26A69A), Color(0xFF66BB6A), Color(0xFF9CCC65), Color(0xFFD4E157),
    Color(0xFFFFEE58), Color(0xFFFFCA28), Color(0xFFFFA726), Color(0xFFFF7043)
)