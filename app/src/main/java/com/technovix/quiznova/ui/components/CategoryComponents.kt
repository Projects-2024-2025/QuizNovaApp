package com.technovix.quiznova.ui.components

import androidx.annotation.StringRes
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed as staggeredItemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.airbnb.lottie.compose.*
import com.technovix.quiznova.R
import com.technovix.quiznova.data.local.entity.CategoryEntity
import com.technovix.quiznova.ui.theme.* // Kendi tema importların
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
                // Seçili değilse ikon alanı kadar boşluk bırakarak hizayı koru
                Spacer(modifier = Modifier.size(24.dp))
            }
        }
    )
}

@Composable
fun LoadingAnimation(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_loading))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(16.dp).fillMaxSize() // Tüm alanı kapla ve padding uygula
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


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryCarousel(
    categories: List<CategoryEntity>,
    onCategorySelect: (CategoryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { categories.size })
    // Seçili kategoriyi animasyonlu geçiş için state'de tutalım
    val currentCategory by remember { derivedStateOf { categories.getOrNull(pagerState.currentPage) } }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp)) // Başlık için biraz daha boşluk

        // Kategori başlığı için animasyonlu geçiş
        AnimatedContent(
            targetState = currentCategory?.name ?: "",
            label = "CarouselTitleAnim",
            transitionSpec = {
                // Yukarı/aşağı kayma efekti
                (slideInVertically { height -> height / 4 } + fadeIn(tween(300, 50))) togetherWith
                        (slideOutVertically { height -> -height / 4 } + fadeOut(tween(250))) using
                        SizeTransform(clip = false)
            }
        ) { title ->
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall, // Biraz daha büyük başlık
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .heightIn(min = 56.dp), // İki satır için yeterli yükseklik
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground // Tema rengi
            )
        }
        Spacer(modifier = Modifier.height(20.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // Alanın çoğunu kaplasın
            contentPadding = PaddingValues(horizontal = 56.dp), // Kenarlarda daha fazla boşluk
            pageSpacing = 16.dp
        ) { pageIndex ->
            categories.getOrNull(pageIndex)?.let { category ->
                val icon = getIconForCategory(category.name)
                // Renkleri doğrudan listeden alıyoruz
                val accentColor = vibrantIconColors[pageIndex % vibrantIconColors.size]

                CategoryPageItem(
                    category = category,
                    icon = icon,
                    accentColor = accentColor,
                    modifier = Modifier
                        // Pager geçiş animasyonu (ölçek ve alfa)
                        .graphicsLayer {
                            val pageOffset = ((pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction).absoluteValue
                            // Daha belirgin ölçekleme ve alfa efekti
                            val scale = lerp(start = 0.80f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
                            scaleX = scale
                            scaleY = scale
                            alpha = lerp(start = 0.4f, stop = 1f, fraction = 1f - pageOffset.coerceIn(0f, 1f))
                        }
                        .clip(MaterialTheme.shapes.extraLarge) // Yeni eklenen şekil
                )
            } ?: Spacer(Modifier.fillMaxSize(0.8f)) // Boş sayfa durumunda yer tutucu
        }

        // İndikatör
        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier.padding(vertical = 24.dp), // Daha fazla dikey boşluk
            activeColor = MaterialTheme.colorScheme.primary,
            inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            indicatorWidth = 8.dp,
            indicatorHeight = 8.dp,
            spacing = 8.dp,
            indicatorShape = CircleShape,
            activeIndicatorWidthMultiplier = 2.5f // Aktif olan daha belirgin olsun
        )

        // Başlat Butonu
        val buttonEnabled = currentCategory != null
        Button(
            onClick = { currentCategory?.let { onCategorySelect(it) } },
            enabled = buttonEnabled,
            modifier = Modifier
                .fillMaxWidth(0.75f) // Biraz daha geniş buton
                .padding(bottom = 48.dp, top = 16.dp) // Alt boşluk artırıldı
                .height(56.dp), // Standart buton yüksekliği
            shape = MaterialTheme.shapes.medium, // Tema şekli
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = if (buttonEnabled) 6.dp else 0.dp, // Daha belirgin gölge
                pressedElevation = 2.dp,
                disabledElevation = 0.dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary, // Tema rengi
                contentColor = MaterialTheme.colorScheme.onPrimary // Tema rengi
            )
        ) {
            // Buton metni için animasyon
            AnimatedContent(
                targetState = currentCategory?.name ?: "",
                label = "ButtonTextAnim",
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) }
            ) { name ->
                Text(
                    text = if (name.isNotEmpty()) stringResource(R.string.start_quiz_button_short, name) // Daha kısa metin
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


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryGridStaggered(
    categories: List<CategoryEntity>,
    onItemClick: (CategoryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2), // 2 sütunlu yapı
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp), // Kenarlarda eşit boşluk
        horizontalArrangement = Arrangement.spacedBy(16.dp), // Öğeler arası yatay boşluk
        verticalItemSpacing = 16.dp, // Öğeler arası dikey boşluk
        state = rememberLazyStaggeredGridState() // Kaydırma pozisyonunu hatırlar
    ) {
        staggeredItemsIndexed(
            items = categories,
            key = { _, category -> category.id } // Performans için anahtar
        ) { index, category ->
            val accentColor = vibrantIconColors[index % vibrantIconColors.size]
            val icon = getIconForCategory(category.name)

            CategoryCardStaggered(
                category = category,
                icon = icon,
                accentColor = accentColor,
                onClick = { onItemClick(category) }
                // Modifier'ı burada tekrar vermeye gerek yok, zaten Card içinde kullanılıyor
            )
        }
    }
}

@Composable
fun CategoryPageItem(
    category: CategoryEntity,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxHeight(0.95f), // Biraz daha yüksek
        shape = MaterialTheme.shapes.extraLarge, // Daha yuvarlak köşeler
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp), // Daha belirgin gölge
        colors = CardDefaults.cardColors(containerColor = Color.Transparent) // Arka planı Box'ta yöneteceğiz
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Canlı ve modern gradient arka plan
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.8f), // Üstte daha baskın renk
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), // Ortada geçiş
                            accentColor.copy(alpha = 0.5f)  // Altta tekrar hafif renk
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp), // İç boşluk artırıldı
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center // İçerik ortalansın
            ) {
                // İkon için arka planı olan daire
                Box(
                    modifier = Modifier
                        .size(96.dp) // Daha büyük ikon alanı
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)), // Hafif şeffaf yüzey rengi
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null, // Dekoratif ikon
                        modifier = Modifier.size(56.dp), // İkon boyutu da büyüdü
                        tint = accentColor // Ana renk ile aynı
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.headlineMedium, // Daha büyük ve kalın başlık
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface, // Yüzey rengi üzerinde daha iyi okunur
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    maxLines = 3, // Gerekirse 3 satıra kadar
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CategoryCardStaggered(
    modifier: Modifier = Modifier, // Dışarıdan modifier alabilir
    category: CategoryEntity,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(), // Genişliği doldur
        shape = MaterialTheme.shapes.large, // Yuvarlak köşeler (16dp)
        colors = CardDefaults.cardColors(
            // Yüzey rengini hafif yükseklikle kullanmak daha iyi bir görünüm sağlar
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp, // Hafif gölge
            pressedElevation = 8.dp  // Tıklanınca artan gölge
        ),
        // Kenarlık rengini accentColor'dan türetelim
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f))
    ) {
        // Çok hafif bir gradient efekti (isteğe bağlı)
        Box(modifier = Modifier
            .fillMaxSize()
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
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 16.dp), // Dikey boşluk artırıldı
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // İkon arka planı
                Box(
                    modifier = Modifier
                        .size(80.dp) // Daha büyük ikon alanı
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)), // Daha belirgin ama yine de hafif
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = category.name, // Erişilebilirlik için
                        modifier = Modifier.size(44.dp), // İkon da büyüdü
                        tint = accentColor // Ana renk
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium, // Kartlar için uygun başlık stili
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 3, // Uzun isimler için
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface // Yüzeydeki metin rengi
                )
                // Alt kısımda ek boşluk (isteğe bağlı)
                // Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}


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
    // Aktif indikatörün genişlik çarpanı
    activeIndicatorWidthMultiplier: Float = 2.0f
) {
    // Aktif indikatörün animasyonlu genişliği
    val activeIndicatorWidth = indicatorWidth * activeIndicatorWidthMultiplier

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val currentPage = pagerState.currentPage
        repeat(pagerState.pageCount) { iteration ->
            // Genişlik animasyonu
            val width by animateDpAsState(
                targetValue = if (currentPage == iteration) activeIndicatorWidth else indicatorWidth,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing), // Yumuşak geçiş
                label = "IndicatorWidthAnim"
            )
            // Renk animasyonu (isteğe bağlı, ama genellikle gerekmez)
            val color = if (currentPage == iteration) activeColor else inactiveColor
            Box(
                modifier = Modifier
                    .clip(indicatorShape)
                    .background(color)
                    .height(indicatorHeight)
                    .width(width) // Animasyonlu genişlik
            )
        }
    }
}


@Composable
fun EmptyStateView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp), // Kenarlardan boşluk
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            // Daha uygun bir ikon (örn. boş kutu veya arama bulunamadı)
            Icons.Filled.SearchOff, // veya SentimentVeryDissatisfied
            contentDescription = null,
            modifier = Modifier.size(80.dp), // Daha büyük ikon
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) // Hafif soluk
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            stringResource(R.string.category_empty_state),
            color = MaterialTheme.colorScheme.onSurfaceVariant, // Tema rengi
            style = MaterialTheme.typography.titleMedium, // Daha uygun stil
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    // Hata için Lottie animasyonu
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_error)) // Kendi Lottie dosyanız
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(32.dp) // Daha fazla iç boşluk
    ) {
        LottieAnimation(
            composition = composition,
            modifier = Modifier.size(200.dp) // Daha büyük animasyon
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.error_oops), // Başlık
            style = MaterialTheme.typography.headlineSmall, // Başlık stili
            color = MaterialTheme.colorScheme.error, // Hata rengi
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message, // Detaylı hata mesajı
            style = MaterialTheme.typography.bodyLarge, // Daha okunaklı gövde metni
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant // Yardımcı metin rengi
        )
        Spacer(modifier = Modifier.height(40.dp)) // Buton için daha fazla boşluk
        Button(
            onClick = onRetry,
            shape = MaterialTheme.shapes.medium, // Standart şekil
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary, // Ana renk
                contentColor = MaterialTheme.colorScheme.onPrimary // Üzerindeki metin rengi
            ),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp) // Daha geniş buton
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null) // Yenileme ikonu
            Spacer(Modifier.size(ButtonDefaults.IconSpacing)) // İkon ve metin arası boşluk
            Text(stringResource(R.string.retry), fontWeight = FontWeight.Bold) // Kalın metin
        }
    }
}


// Bu fonksiyon kategori isimlerine göre ikon eşleştirmesi yapar.
// Yeni kategoriler eklenirse buraya da eklenmelidir.
// (Bu fonksiyonu renk listesiyle birlikte aynı dosyada tutmak mantıklı)
val vibrantIconColors = listOf( // Bu listeyi de buraya alalım
    Color(0xFFEF5350), // Kırmızı
    Color(0xFFEC407A), // Pembe
    Color(0xFFAB47BC), // Mor
    Color(0xFF7E57C2), // Derin Mor
    Color(0xFF5C6BC0), // İndigo
    Color(0xFF42A5F5), // Mavi
    Color(0xFF29B6F6), // Açık Mavi
    Color(0xFF26C6DA), // Camgöbeği
    Color(0xFF26A69A), // Turkuaz
    Color(0xFF66BB6A), // Yeşil
    Color(0xFF9CCC65), // Açık Yeşil
    Color(0xFFD4E157), // Limon Yeşili
    Color(0xFFFFEE58), // Sarı
    Color(0xFFFFCA28), // Kehribar
    Color(0xFFFFA726), // Turuncu
    Color(0xFFFF7043)  // Derin Turuncu
)

fun getIconForCategory(categoryName: String): ImageVector {
    return when {
        categoryName.contains("Knowledge", ignoreCase = true) -> Icons.Filled.AutoStories
        categoryName.contains("Books", ignoreCase = true) -> Icons.Filled.LibraryBooks
        categoryName.contains("Film", ignoreCase = true) -> Icons.Filled.Theaters
        categoryName.contains("Music", ignoreCase = true) -> Icons.Filled.MusicNote
        categoryName.contains("Musicals & Theatres", ignoreCase = true) -> Icons.Filled.TheaterComedy
        categoryName.contains("Television", ignoreCase = true) -> Icons.Filled.Tv
        categoryName.contains("Video Games", ignoreCase = true) -> Icons.Filled.VideogameAsset
        categoryName.contains("Board Games", ignoreCase = true) -> Icons.Filled.Casino
        categoryName.contains("Science & Nature", ignoreCase = true) -> Icons.Filled.Science
        categoryName.contains("Computers", ignoreCase = true) -> Icons.Filled.Computer
        categoryName.contains("Mathematics", ignoreCase = true) -> Icons.Filled.Calculate
        categoryName.contains("Mythology", ignoreCase = true) -> Icons.Filled.Fort
        categoryName.contains("Sports", ignoreCase = true) -> Icons.Filled.SportsBasketball
        categoryName.contains("Geography", ignoreCase = true) -> Icons.Filled.Public
        categoryName.contains("History", ignoreCase = true) -> Icons.Filled.Museum
        categoryName.contains("Politics", ignoreCase = true) -> Icons.Filled.Gavel
        categoryName.contains("Art", ignoreCase = true) -> Icons.Filled.Palette
        categoryName.contains("Celebrities", ignoreCase = true) -> Icons.Filled.Star
        categoryName.contains("Animals", ignoreCase = true) -> Icons.Filled.Pets
        categoryName.contains("Vehicles", ignoreCase = true) -> Icons.Filled.DirectionsCar
        categoryName.contains("Comics", ignoreCase = true) -> Icons.Filled.MenuBook
        categoryName.contains("Gadgets", ignoreCase = true) -> Icons.Filled.PhonelinkSetup
        categoryName.contains("Japanese Anime & Manga", ignoreCase = true) -> Icons.Filled.Face
        categoryName.contains("Cartoon & Animations", ignoreCase = true) -> Icons.Filled.Animation
        else -> Icons.Filled.Category
    }
}