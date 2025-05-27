package com.technovix.quiznova.ui.screen.category.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.technovix.quiznova.data.local.entity.CategoryEntity
import com.technovix.quiznova.ui.components.getIconForCategory
import com.technovix.quiznova.ui.components.vibrantIconColors

@Composable
fun CategoryGridRegular(
    categories: List<CategoryEntity>,
    onItemClick: (CategoryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp: Dp = configuration.screenWidthDp.dp
    val orientation = configuration.orientation

    val numColumns = when {
        orientation == Configuration.ORIENTATION_LANDSCAPE -> {
            when { // Yatay mod için genişliğe göre sütun
                screenWidthDp < 600.dp -> 3 // Dar yatay (bazı telefonlar)
                screenWidthDp < 840.dp -> 4
                else -> 5
            }
        }
        else -> { // Dikey mod (mevcut mantığınız)
            when {
                screenWidthDp < 360.dp -> 2
                screenWidthDp < 600.dp -> 2
                screenWidthDp < 840.dp -> 3
                else -> 4
            }
        }
    }

    val contentPaddingAll = if (screenWidthDp < 360.dp) 12.dp else 16.dp
    val itemSpacing = if (screenWidthDp < 360.dp) 12.dp else 16.dp

    LazyVerticalGrid(
        columns = GridCells.Fixed(numColumns), // Sütun sayısı
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(contentPaddingAll),
        horizontalArrangement = Arrangement.spacedBy(itemSpacing), // Yatay boşluk
        verticalArrangement = Arrangement.spacedBy(itemSpacing)   // Dikey boşluk
    ) {
        itemsIndexed(
            items = categories,
            key = { _, category -> category.id }
        ) { index, category ->
            val accentColor = vibrantIconColors[index % vibrantIconColors.size]
            val icon = getIconForCategory(category.name)

            // Grid içindeki her bir kart (Kare görünüm ve kısaltılmış metin)
            CategoryGridItem(
                category = category,
                icon = icon,
                accentColor = accentColor,
                onClick = { onItemClick(category) },
                isCompact = screenWidthDp < 380.dp,
                modifier = Modifier.aspectRatio(1f) // Kare oranını uygula
            )
        }
    }
}