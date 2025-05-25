package com.technovix.quiznova.ui.components // veya ui.screen.category.components

import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells // Düzenli Grid için
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid // Düzenli Grid için
import androidx.compose.foundation.lazy.grid.itemsIndexed // Düzenli Grid için
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.airbnb.lottie.compose.*
import com.technovix.quiznova.R // Kendi R dosyanızın importu
import com.technovix.quiznova.data.local.entity.CategoryEntity
import kotlin.math.absoluteValue


// Tema seçimi menü elemanı için yardımcı Composable
@Composable
fun ThemeMenuItem(
    @StringRes textRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(stringResource(textRes)) },
        onClick = onClick,
        leadingIcon = {
            if (isSelected) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = stringResource(R.string.cd_selected_theme),
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Spacer(modifier = Modifier.size(24.dp))
            }
        }
    )
}

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

// Kategori Carousel Görünümü (Üstü Boş)
@Composable
fun CategoryCarousel(
    categories: List<CategoryEntity>,
    onCategorySelect: (CategoryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (categories.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Kategoriler yükleniyor veya bulunamadı.")
        }
        return
    }
    val pagerState = rememberPagerState(pageCount = { categories.size })
    val currentCategory by remember { derivedStateOf { categories.getOrNull(pagerState.currentPage) } }

    val configuration = LocalConfiguration.current
    val screenWidthDp: Dp = configuration.screenWidthDp.dp
    val screenHeightDp: Dp = configuration.screenHeightDp.dp

    val isCompactWidth = screenWidthDp < 380.dp
    val isCompactHeight = screenHeightDp < 650.dp

    val topSpacerHeight = if (isCompactHeight) 12.dp else 20.dp

    val pagerContentHorizontalPadding = when {
        screenWidthDp < 360.dp -> 40.dp
        screenWidthDp < 420.dp -> 48.dp
        else -> 56.dp
    }
    val pagerPageSpacing = if (isCompactWidth) 12.dp else 16.dp

    val indicatorVerticalPadding = if (isCompactHeight) 16.dp else 24.dp
    val indicatorHorizontalPadding = if (isCompactWidth) 24.dp else 32.dp
    val indicatorDotSize = if (isCompactWidth) 6.dp else 8.dp // Hem width hem height için
    val indicatorSpacing = if (isCompactWidth) 6.dp else 8.dp

    val buttonFillMaxWidthFraction = if (isCompactWidth) 0.85f else 0.75f
    val buttonVerticalPaddingTop = if (isCompactHeight) 12.dp else 16.dp
    val buttonVerticalPaddingBottom = if (isCompactHeight) 32.dp else 48.dp
    val buttonHeight = if (isCompactHeight) 50.dp else 56.dp
    val buttonFontSize = if (isCompactWidth) 14.sp else 16.sp

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(topSpacerHeight)) // Pager'dan önceki boşluk.

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = pagerContentHorizontalPadding),
            pageSpacing = pagerPageSpacing
        ) { pageIndex ->
            categories.getOrNull(pageIndex)?.let { category ->
                val icon = getIconForCategory(category.name)
                val accentColor = vibrantIconColors[pageIndex % vibrantIconColors.size]

                CategoryPageItem( // Carousel içindeki kart (kısaltılmış metin içeriyor)
                    category = category,
                    icon = icon,
                    accentColor = accentColor,
                    isCompactWidth = isCompactWidth,
                    isCompactHeight = isCompactHeight,
                    modifier = Modifier
                        .graphicsLayer {
                            val pageOffset = ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction).absoluteValue
                            val scale = lerp(start = 0.80f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
                            scaleX = scale
                            scaleY = scale
                            alpha = lerp(start = 0.4f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
                        }
                        .clip(MaterialTheme.shapes.extraLarge)
                )
            } ?: Spacer(Modifier.fillMaxSize(0.8f))
        }

        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .padding(vertical = indicatorVerticalPadding)
                .padding(horizontal = indicatorHorizontalPadding),
            activeColor = MaterialTheme.colorScheme.primary,
            inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            indicatorWidth = indicatorDotSize,
            indicatorHeight = indicatorDotSize,
            spacing = indicatorSpacing,
            indicatorShape = CircleShape,
            activeIndicatorWidthMultiplier = 2.5f
        )

        val buttonEnabled = currentCategory != null
        Button(
            onClick = { currentCategory?.let { onCategorySelect(it) } },
            enabled = buttonEnabled,
            modifier = Modifier
                .fillMaxWidth(buttonFillMaxWidthFraction)
                .padding(top = buttonVerticalPaddingTop, bottom = buttonVerticalPaddingBottom)
                .height(buttonHeight),
            shape = MaterialTheme.shapes.medium,
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = if (buttonEnabled) 6.dp else 0.dp,
                pressedElevation = 2.dp,
                disabledElevation = 0.dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            AnimatedContent(
                targetState = currentCategory?.name ?: "",
                label = "ButtonTextAnim",
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) }
            ) { name ->
                // Buton metni için de kısaltılmış ismi kullanabiliriz (isteğe bağlı)
                val displayName = name.substringAfterLast(':', name).trim()
                Text(
                    text = if (displayName.isNotEmpty()) stringResource(R.string.start_quiz_button_short, displayName)
                    else stringResource(R.string.select_category_button),
                    fontWeight = FontWeight.Bold,
                    fontSize = buttonFontSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// Kategori Düzenli Grid Görünümü (AspectRatio ile)
@Composable
fun CategoryGridRegular(
    categories: List<CategoryEntity>,
    onItemClick: (CategoryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp: Dp = configuration.screenWidthDp.dp

    val numColumns = when {
        screenWidthDp < 360.dp -> 2
        screenWidthDp < 600.dp -> 2
        screenWidthDp < 840.dp -> 3
        else -> 4
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

// Grid İçindeki Kategori Kartı (AspectRatio ve Kısaltılmış Metin ile)
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

// Carousel İçindeki Kategori Kartı (Kısaltılmış Metin ile)
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


// Yatay Pager Göstergesi
@Composable
fun HorizontalPagerIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
    indicatorWidth: Dp = 8.dp,
    indicatorHeight: Dp = indicatorWidth,
    spacing: Dp = indicatorWidth,
    indicatorShape: Shape = CircleShape,
    activeIndicatorWidthMultiplier: Float = 2.0f
) {
    val activeIndicatorWidth = indicatorWidth * activeIndicatorWidthMultiplier
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val currentPage = pagerState.currentPage
        if (pagerState.pageCount > 0) { // Sayfa sayısı 0'dan büyükse göster
            repeat(pagerState.pageCount) { iteration ->
                val width by animateDpAsState(
                    targetValue = if (currentPage == iteration) activeIndicatorWidth else indicatorWidth,
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                    label = "IndicatorWidthAnim"
                )
                val color = if (currentPage == iteration) activeColor else inactiveColor
                Box(
                    modifier = Modifier
                        .clip(indicatorShape)
                        .background(color)
                        .height(indicatorHeight)
                        .width(width)
                )
            }
        }
    }
}

// Boş Durum Görünümü
@Composable
fun EmptyStateView(modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val isCompact = screenWidthDp < 380.dp

    val iconSize = if (isCompact) 70.dp else 80.dp
    val textStyle = if (isCompact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium
    Column(
        modifier = modifier.padding(horizontal = 32.dp).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            stringResource(R.string.category_empty_state),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = textStyle,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
    }
}

// Hata Durumu Görünümü
@Composable
fun ErrorView(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val isCompact = screenWidthDp < 380.dp

    val lottieSize = if (isCompact) 170.dp else 200.dp
    val titleStyle = if (isCompact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall
    val messageStyle = if (isCompact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge
    val buttonPaddingHorizontal = if (isCompact) 24.dp else 32.dp
    val buttonPaddingVertical = if (isCompact) 10.dp else 12.dp

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_error))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(32.dp).fillMaxSize()
    ) {
        LottieAnimation(
            composition = composition,
            modifier = Modifier.size(lottieSize)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.error_oops),
            style = titleStyle,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            style = messageStyle,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onRetry,
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            contentPadding = PaddingValues(horizontal = buttonPaddingHorizontal, vertical = buttonPaddingVertical)
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.retry), fontWeight = FontWeight.Bold)
        }
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