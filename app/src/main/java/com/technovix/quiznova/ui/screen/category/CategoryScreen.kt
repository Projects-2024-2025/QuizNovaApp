package com.technovix.quiznova.ui.screen.category

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.technovix.quiznova.R
import com.technovix.quiznova.data.local.entity.CategoryEntity
import com.technovix.quiznova.ui.navigation.Screen
import com.technovix.quiznova.ui.theme.*
import com.technovix.quiznova.ui.viewmodel.CategoryViewModel
import com.technovix.quiznova.util.Resource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun CategoryScreen(
    navController: NavController,
    viewModel: CategoryViewModel = hiltViewModel()
) {
    val categoriesState by viewModel.categories.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val privacyPolicyUrl = stringResource(R.string.url_privacy_policy)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.category_screen_title), fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.cd_more_options))
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_settings), color = MaterialTheme.colorScheme.onSurface) },
                            onClick = { println("Ayarlar tıklandı!"); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_privacy_policy), color = MaterialTheme.colorScheme.onSurface) },
                            onClick = {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl)))
                                } catch (e: Exception) {
                                    Log.e("CategoryScreen", "URL Açılamadı: $privacyPolicyUrl", e)
                                }
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Policy, contentDescription = null, tint = MaterialTheme.colorScheme.secondary) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_about), color = MaterialTheme.colorScheme.onSurface) },
                            onClick = { println("Hakkında tıklandı!"); showMenu = false },
                            leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary) }
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        // LazyVerticalStaggeredGrid Kullanımı
        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Fixed(2), // 2 sütun
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), // AppBar altından başla
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp), // Kenar ve dikey boşluk
            horizontalArrangement = Arrangement.spacedBy(12.dp), // Sütunlar arası boşluk
            verticalItemSpacing = 12.dp, // Satırlar arası dikey boşluk
            state = rememberLazyStaggeredGridState() // Grid state'i
        ) {
            // Duruma göre içeriği göster (LazyStaggeredGridScope genişletmesiyle)
            handleContentStateStaggered(
                categoriesState = categoriesState,
                navController = navController,
                viewModel = viewModel
            )
        } // LazyVerticalStaggeredGrid sonu
    } // Scaffold sonu
}

// İçeriği yönetmek için StaggeredGrid'e özel fonksiyon
@OptIn(ExperimentalFoundationApi::class)
private fun LazyStaggeredGridScope.handleContentStateStaggered(
    categoriesState: Resource<List<CategoryEntity>>,
    navController: NavController,
    viewModel: CategoryViewModel
) {
    when (categoriesState) {
        is Resource.Loading -> {
            // Yükleme animasyonu tam genişlik kaplasın
            item(span = StaggeredGridItemSpan.FullLine) {
                LoadingAnimation(modifier = Modifier.fillMaxWidth().padding(vertical = 100.dp))
            }
        }
        is Resource.Success -> {
            val categories = categoriesState.data
            if (categories.isNullOrEmpty()) {
                // Boş durum görünümü tam genişlik kaplasın
                item(span = StaggeredGridItemSpan.FullLine) {
                    EmptyStateView(modifier = Modifier.fillMaxWidth().padding(vertical = 100.dp))
                }
            } else {
                // --- İsteğe Bağlı Başlık ---
                // item(span = StaggeredGridItemSpan.FullLine) {
                //     SectionHeader(stringResource(R.string.category_section_all))
                // }

                // --- Kategorileri Grid'de Göster ---
                itemsIndexed(
                    items = categories,
                    key = { _, category -> category.id }
                ) { index, category ->
                    val accentColor = vibrantIconColors[index % vibrantIconColors.size]
                    val icon = getIconForCategory(category.name)

                    // Yeni Staggered Kart Tasarımı
                    CategoryCardStaggered(
                        modifier = Modifier.animateItemPlacement( // Animasyon (Staggered için farklı olabilir)
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy, // Biraz daha zıplama
                                stiffness = Spring.StiffnessLow // Daha yumuşak
                            )
                        ),
                        category = category,
                        icon = icon,
                        accentColor = accentColor,
                        onClick = {
                            val route = Screen.Quiz.createRoute(category.id, category.name)
                            try { navController.navigate(route) }
                            catch (e: Exception) { Log.e("CategoryClick", "Navigation failed!", e) }
                        }
                    )
                }
            }
        }
        is Resource.Error -> {
            // Hata görünümü tam genişlik kaplasın
            item(span = StaggeredGridItemSpan.FullLine) {
                ErrorView(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 100.dp),
                    message = categoriesState.message ?: stringResource(R.string.error_unknown),
                    onRetry = { viewModel.refreshCategories() }
                )
            }
        }
    }
}

// Bölüm Başlığı (İsteğe bağlı, Staggered Grid'de tam satır kaplar)
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, top = 16.dp, bottom = 8.dp) // Padding ayarlandı
    )
}


// --- YENİ STAGGERED KART TASARIMI ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryCardStaggered(
    modifier: Modifier = Modifier,
    category: CategoryEntity,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    // Kartın yüksekliği içeriğe göre otomatik ayarlanacak (Staggered Grid'in avantajı)
    // Genişlik Grid tarafından belirlenir.

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(), // Sütun genişliğini doldur
        shape = MaterialTheme.shapes.large, // Biraz daha yuvarlak köşeler
        colors = CardDefaults.cardColors(
            // Arka plana çok hafif bir gradient ekleyelim
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp) // Hafif yüksek yüzey
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp, pressedElevation = 8.dp), // Daha belirgin gölge
        border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.6f)) // Daha belirgin vurgu kenarlığı
    ) {
        // Kartın arka planına hafif gradient overlay eklemek için Box kullanalım
        Box(modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent, // Üstte şeffaf başla
                        accentColor.copy(alpha = 0.05f), // Ortada çok hafif vurgu rengi
                        Color.Transparent // Altta tekrar şeffaf bitir
                    )
                )
            )
        ){
            Column(
                modifier = Modifier
                    .fillMaxWidth() // Genişliği doldur
                    .padding(16.dp), // İç boşluk artırıldı
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // İkon alanı
                Box(
                    modifier = Modifier
                        .size(72.dp) // Biraz daha büyük ikon alanı
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.20f)), // Daha belirgin ikon arka planı
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = category.name,
                        modifier = Modifier.size(40.dp), // İkon da biraz büyüdü
                        tint = accentColor // Ana vurgu rengi
                    )
                }

                Spacer(modifier = Modifier.height(16.dp)) // Boşluk artırıldı

                // Kategori Adı
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleSmall, // Stil belki biraz daha büyük
                    fontWeight = FontWeight.Bold, // Daha kalın
                    textAlign = TextAlign.Center,
                    maxLines = 3, // Daha fazla satıra izin ver (Staggered için)
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface // Standart metin rengi
                )
                Spacer(modifier = Modifier.height(4.dp)) // Alt boşluk (kart yüksekliğini etkiler)
            }
        }
    }
}

// Boş Durum Görünümü (Aynı kalabilir)
@Composable
fun EmptyStateView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.SentimentVeryDissatisfied, contentDescription = null,
            modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.category_empty_state), color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
    }
}

// LoadingAnimation (Aynı kalabilir)
@Composable
fun LoadingAnimation(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_loading))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(16.dp)
    ) {
        LottieAnimation(composition = composition, iterations = LottieConstants.IterateForever, modifier = Modifier.size(150.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(R.string.loading_categories), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
}

// ErrorView (Aynı kalabilir)
@Composable
fun ErrorView(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_error))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        LottieAnimation(composition = composition, modifier = Modifier.size(180.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = stringResource(R.string.error_oops), style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = message, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRetry,
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.retry), fontWeight = FontWeight.SemiBold)
        }
    }
}

// getIconForCategory (Aynı kalabilir)
fun getIconForCategory(categoryName: String): ImageVector {
    return when {
        // ... (case'ler aynı)
        categoryName.contains("Knowledge", ignoreCase = true) -> Icons.Filled.AutoStories
        categoryName.contains("Books", ignoreCase = true) -> Icons.Filled.LibraryBooks
        categoryName.contains("Film", ignoreCase = true) -> Icons.Filled.Theaters
        categoryName.contains("Music", ignoreCase = true) -> Icons.Filled.MusicVideo
        categoryName.contains("Computers", ignoreCase = true) -> Icons.Filled.Computer
        categoryName.contains("Sports", ignoreCase = true) -> Icons.Filled.SportsBasketball
        categoryName.contains("Geography", ignoreCase = true) -> Icons.Filled.TravelExplore
        categoryName.contains("History", ignoreCase = true) -> Icons.Filled.Museum
        categoryName.contains("Animals", ignoreCase = true) -> Icons.Filled.Pets
        categoryName.contains("Science", ignoreCase = true) -> Icons.Filled.Science
        categoryName.contains("Mythology", ignoreCase = true) -> Icons.Filled.Fort
        categoryName.contains("Vehicles", ignoreCase = true) -> Icons.Filled.DirectionsCar
        else -> Icons.Filled.Category
    }
}