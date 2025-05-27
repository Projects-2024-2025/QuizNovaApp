package com.technovix.quiznova.ui.components // veya ui.screen.category.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.technovix.quiznova.R // Kendi R dosyanızın importu



// Yükleme animasyonu
@Composable
fun LoadingAnimation(modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val screenWidthDp: Dp = configuration.screenWidthDp.dp
    val isCompact = screenWidthDp < 380.dp

    val lottieSize = if (isCompact) 150.dp else 180.dp
    val textStyle = if (isCompact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_loading))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(16.dp).fillMaxSize()
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(lottieSize)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.loading_categories),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = textStyle
        )
    }
}

// Kategori İkonlarını Eşleştirme Fonksiyonu
fun getIconForCategory(categoryName: String): ImageVector {
    // İkon eşleştirmesi yapılırken orijinal, tam ismi kullanmak daha güvenli olabilir.
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