package com.technovix.quiznova.ui.screen.category

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.technovix.quiznova.R
import com.technovix.quiznova.data.local.entity.CategoryEntity
// --- Component importları güncellendi ---
import com.technovix.quiznova.ui.components.CategoryCarousel // veya kendi component paketiniz
import com.technovix.quiznova.ui.components.CategoryGridRegular // <<<--- CategoryGridStaggered yerine
import com.technovix.quiznova.ui.components.EmptyStateView
import com.technovix.quiznova.ui.components.ErrorView
import com.technovix.quiznova.ui.components.LoadingAnimation
// --- Diğer importlar ---
import com.technovix.quiznova.ui.navigation.Screen
import com.technovix.quiznova.util.ThemePreference
import com.technovix.quiznova.ui.theme.*
import com.technovix.quiznova.ui.viewmodel.CategoryViewModel
import com.technovix.quiznova.util.Resource
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CategoryScreen(
    navController: NavController,
    viewModel: CategoryViewModel = hiltViewModel(),
    currentTheme: ThemePreference
) {
    val categoriesState by viewModel.categories.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var isGridViewVisible by remember { mutableStateOf(true) } // Grid ile başla
    val zoomOutThreshold = 0.85f
    val zoomInThreshold = 1.15f

    val configuration = LocalConfiguration.current // EKLENDİ
    val screenWidthDp = configuration.screenWidthDp.dp // EKLENDİ

    val screenContentPadding = if (screenWidthDp < 360.dp) 8.dp else 0.dp

    val backgroundBrush = if (currentTheme == ThemePreference.DARK) {
        darkAppBackgroundGradient()
    } else {
        lightAppBackgroundGradient()
    }

    // Quiz ekranına yönlendirme fonksiyonu
    fun navigateToQuizLocal(category: CategoryEntity) {
        val route = Screen.Quiz.createRoute(category.id, category.name)
        try {
            navController.navigate(route)
        } catch (e: Exception) {
            Timber.tag("CategoryNavigation").e(e, "Navigasyon başarısız: Rota='" + route + "'")
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.category_screen_title),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    val actionIcon = if (isGridViewVisible) Icons.Default.ViewCarousel else Icons.Default.GridView
                    val actionDescResId = if (isGridViewVisible) R.string.cd_show_carousel else R.string.cd_show_grid
                    IconButton(onClick = { isGridViewVisible = !isGridViewVisible }) {
                        Icon(actionIcon, contentDescription = stringResource(actionDescResId))
                    }
                    Box {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.cd_more_options))
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_settings_actual)) },
                                onClick = {
                                    navController.navigate(Screen.Settings.route)
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.primary) },
                                modifier = Modifier.testTag("settings_menu_item")
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_about)) },
                                onClick = { navController.navigate(Screen.About.route)
                                    showMenu = false },
                                leadingIcon = { Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.tertiary) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_privacy_policy)) },
                                onClick = {
                                    navController.navigate(Screen.PrivacyPolicy.route) // <<<--- DEĞİŞTİRİLDİ
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Policy, null, tint = MaterialTheme.colorScheme.secondary) }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, _ ->
                            if (isGridViewVisible) {
                                if (zoom > zoomInThreshold) {
                                    isGridViewVisible = false
                                }
                            } else {
                                if (zoom < zoomOutThreshold) {
                                    isGridViewVisible = true
                                }
                            }
                        }
                    }
            ) {
                when (categoriesState) {
                    is Resource.Loading -> LoadingAnimation(modifier = Modifier.align(Alignment.Center))
                    is Resource.Success -> {
                        val categories = (categoriesState as Resource.Success<List<CategoryEntity>>).data
                        if (categories.isNullOrEmpty()) {
                            EmptyStateView(modifier = Modifier.align(Alignment.Center)) // Padding'i component içine taşıdık
                        } else {
                            AnimatedContent(
                                targetState = isGridViewVisible,
                                label = "LayoutSwitchAnimation",
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(400, 50)) + scaleIn(initialScale = 0.92f, animationSpec = tween(400, 50)) togetherWith
                                            fadeOut(animationSpec = tween(350)) + scaleOut(targetScale = 0.92f, animationSpec = tween(350)) using
                                            SizeTransform(clip = false, sizeAnimationSpec = { _, _ -> tween(400) })
                                }
                            ) { showGrid ->
                                if (showGrid) {
                                    // --- DEĞİŞİKLİK: Doğru grid fonksiyonu çağrılıyor ---
                                    CategoryGridRegular( // <<<--- CategoryGridStaggered yerine
                                        categories = categories,
                                        onItemClick = { category -> navigateToQuizLocal(category) }
                                    )
                                } else {
                                    CategoryCarousel(
                                        categories = categories,
                                        onCategorySelect = { category -> navigateToQuizLocal(category) }
                                    )
                                }
                            }
                        }
                    }
                    is Resource.Error -> ErrorView(
                        modifier = Modifier.align(Alignment.Center), // Padding'i component içine taşıdık
                        message = (categoriesState as Resource.Error<List<CategoryEntity>>).message ?: stringResource(R.string.error_unknown),
                        onRetry = { viewModel.refreshCategories() }
                    )
                } // end when
            } // end İçerik Box
        } // end Arka Plan Box
    } // end Scaffold
}