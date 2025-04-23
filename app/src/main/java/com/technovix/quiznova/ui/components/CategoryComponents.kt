package com.technovix.quiznova.ui.components // veya ui.screen.category.components

import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.res.dimensionResource // Boyut kaynakları için
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
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_loading))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(16.dp).fillMaxSize()
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(180.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.loading_categories),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// Kategori Carousel Görünümü (Üstü Boş)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryCarousel(
    categories: List<CategoryEntity>,
    onCategorySelect: (CategoryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { categories.size })
    val currentCategory by remember { derivedStateOf { categories.getOrNull(pagerState.currentPage) } }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp)) // Pager'dan önceki boşluk.

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 56.dp),
            pageSpacing = 16.dp
        ) { pageIndex ->
            categories.getOrNull(pageIndex)?.let { category ->
                val icon = getIconForCategory(category.name)
                val accentColor = vibrantIconColors[pageIndex % vibrantIconColors.size]

                CategoryPageItem( // Carousel içindeki kart (kısaltılmış metin içeriyor)
                    category = category,
                    icon = icon,
                    accentColor = accentColor,
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
            modifier = Modifier.padding(vertical = 24.dp),
            activeColor = MaterialTheme.colorScheme.primary,
            inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            indicatorWidth = 8.dp,
            indicatorHeight = 8.dp,
            spacing = 8.dp,
            indicatorShape = CircleShape,
            activeIndicatorWidthMultiplier = 2.5f
        )

        val buttonEnabled = currentCategory != null
        Button(
            onClick = { currentCategory?.let { onCategorySelect(it) } },
            enabled = buttonEnabled,
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .padding(bottom = 48.dp, top = 16.dp)
                .height(56.dp),
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
                    fontSize = 16.sp,
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
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // Sütun sayısı
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp), // Yatay boşluk
        verticalArrangement = Arrangement.spacedBy(16.dp)   // Dikey boşluk
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
    onClick: () -> Unit
) {
    // ÖNEMLİ NOT: Aşağıdaki dimensionResource() kullanımları için
    // projenizde res/values/dimens.xml dosyasında ilgili boyutları tanımlamanız gerekir.
    // Örnek: <dimen name="card_grid_padding">12dp</dimen>
    // Eğer tanımlamak istemiyorsanız, doğrudan .dp değerlerini kullanın (örn: .padding(12.dp)).

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
                    .padding(dimensionResource(id = R.dimen.card_grid_padding)), // İç padding
                // veya .padding(12.dp)
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center // İçeriği dikeyde ortala
            ) {
                Box(
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.card_grid_icon_background_size)) // İkon arka plan boyutu
                        // veya .size(64.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = category.name, // Orijinal isim kalsın
                        modifier = Modifier.size(dimensionResource(id = R.dimen.card_grid_icon_size)), // İkon boyutu
                        // veya .size(36.dp)
                        tint = accentColor
                    )
                }
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.card_grid_spacer_height))) // İkon ve metin arası boşluk
                // veya .height(8.dp)
                Text(
                    text = category.name.substringAfterLast(':', category.name).trim(), // Kısaltılmış metin
                    style = MaterialTheme.typography.titleMedium, // Stil ayarlanabilir
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
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxHeight(0.95f),
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
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = category.name, // Orijinal isim kalsın
                        modifier = Modifier.size(56.dp),
                        tint = accentColor
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = category.name.substringAfterLast(':', category.name).trim(), // Kısaltılmış metin
                    style = MaterialTheme.typography.headlineMedium,
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
@OptIn(ExperimentalFoundationApi::class)
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

// Boş Durum Görünümü
@Composable
fun EmptyStateView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            stringResource(R.string.category_empty_state),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
    }
}

// Hata Durumu Görünümü
@Composable
fun ErrorView(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_error))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(32.dp).fillMaxSize()
    ) {
        LottieAnimation(
            composition = composition,
            modifier = Modifier.size(200.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.error_oops),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
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
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
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