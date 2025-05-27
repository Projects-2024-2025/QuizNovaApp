package com.technovix.quiznova.ui.screen.category.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.technovix.quiznova.data.local.entity.CategoryEntity

@Composable
fun CategoryGridItem(
    modifier: Modifier = Modifier, // Dışarıdan gelen modifier'ı (aspectRatio içeren) alacak
    category: CategoryEntity,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    isCompact: Boolean
) {
    // ÖNEMLİ NOT: Aşağıdaki dimensionResource() kullanımları için
    // projenizde res/values/dimens.xml dosyasında ilgili boyutları tanımlamanız gerekir.
    // Örnek: <dimen name="card_grid_padding">12dp</dimen>
    // Eğer tanımlamak istemiyorsanız, doğrudan .dp değerlerini kullanın (örn: .padding(12.dp)).
    val cardPadding = if (isCompact) 8.dp else 12.dp
    val iconBackgroundSize = if (isCompact) 56.dp else 64.dp
    val iconSize = if (isCompact) 30.dp else 36.dp
    val spacerHeight = if (isCompact) 6.dp else 8.dp
    val textStyle = if (isCompact) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleMedium

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(), // AspectRatio modifier'ı dışarıdan uygulanacak
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        ),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
    ) {
        Box(modifier = Modifier
            .fillMaxSize() // Oranla belirlenen alana yayıl
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.03f),
                        Color.Transparent,
                        accentColor.copy(alpha = 0.03f)
                    )
                )
            )
        ){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(cardPadding), // İç padding
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center // İçeriği dikeyde ortala
            ) {
                Box(
                    modifier = Modifier
                        .size(iconBackgroundSize) // İkon arka plan boyutu
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = category.name, // Orijinal isim kalsın
                        modifier = Modifier.size(iconSize), // İkon boyutu
                        tint = accentColor
                    )
                }
                Spacer(modifier = Modifier.height(spacerHeight)) // İkon ve metin arası boşluk
                Text(
                    text = category.name.substringAfterLast(':', category.name).trim(), // Kısaltılmış metin
                    style = textStyle, // Stil ayarlanabilir
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2, // Kare kart için 2 satır genellikle yeterli
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}


@Composable
fun CategoryPageItem(
    category: CategoryEntity,
    icon: ImageVector,
    accentColor: Color,
    isCompactHeight: Boolean,
    isCompactWidth: Boolean,
    modifier: Modifier = Modifier
) {
    // Kartın yüksekliğini ve içindeki elemanların boyutlarını ayarla
    val cardFillMaxHeight = if (isCompactHeight) 0.9f else 0.85f // Pager içindeki kartın yüksekliği
    val cardInternalPadding = if (isCompactWidth || isCompactHeight) 24.dp else 32.dp
    val iconContainerSize = if (isCompactWidth || isCompactHeight) 80.dp else 96.dp
    val iconActualSize = if (isCompactWidth || isCompactHeight) 48.dp else 56.dp
    val spacerHeight = if (isCompactHeight) 16.dp else 24.dp
    val textStyle = if (isCompactWidth || isCompactHeight) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium

    Card(
        modifier = modifier.fillMaxHeight(cardFillMaxHeight),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            accentColor.copy(alpha = 0.5f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(cardInternalPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(iconContainerSize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = category.name, // Orijinal isim kalsın
                        modifier = Modifier.size(iconActualSize),
                        tint = accentColor
                    )
                }
                Spacer(modifier = Modifier.height(spacerHeight))
                Text(
                    text = category.name.substringAfterLast(':', category.name).trim(), // Kısaltılmış metin
                    style = textStyle,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    maxLines = 2, // Kısaltıldığı için 2 satır yeterli olabilir
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}