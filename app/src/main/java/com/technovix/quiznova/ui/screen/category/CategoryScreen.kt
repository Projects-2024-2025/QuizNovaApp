package com.technovix.quiznova.ui.screen.category

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.technovix.quiznova.R
import com.technovix.quiznova.data.local.entity.CategoryEntity
import com.technovix.quiznova.ui.components.CategoryCarousel
import com.technovix.quiznova.ui.components.CategoryGridStaggered
import com.technovix.quiznova.ui.components.EmptyStateView
import com.technovix.quiznova.ui.components.ErrorView
import com.technovix.quiznova.ui.components.LoadingAnimation
import com.technovix.quiznova.ui.navigation.Screen
import com.technovix.quiznova.util.ThemePreference
import com.technovix.quiznova.ui.theme.*
import com.technovix.quiznova.ui.viewmodel.CategoryViewModel
import com.technovix.quiznova.util.Resource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CategoryScreen(
    navController: NavController,
    viewModel: CategoryViewModel = hiltViewModel(),
    currentTheme: ThemePreference
) {
    val categoriesState by viewModel.categories.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val privacyPolicyUrl = stringResource(R.string.url_privacy_policy)
    var isGridViewVisible by remember { mutableStateOf(true) }
    val zoomOutThreshold = 0.85f
    val zoomInThreshold = 1.15f

    val backgroundBrush = if (currentTheme == ThemePreference.DARK || (currentTheme == ThemePreference.SYSTEM && isSystemInDarkTheme())) {
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
            Log.e("CategoryNavigation", "Navigasyon başarısız: Rota='${route}'", e)
            // Kullanıcıya bir hata mesajı gösterilebilir (Snackbar vb.)
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
                    // Grid/Carousel Değiştirme Butonu
                    val actionIcon = if (isGridViewVisible) Icons.Default.ViewCarousel else Icons.Default.GridView
                    val actionDescResId = if (isGridViewVisible) R.string.cd_show_carousel else R.string.cd_show_grid
                    IconButton(onClick = { isGridViewVisible = !isGridViewVisible }) {
                        Icon(actionIcon, contentDescription = stringResource(actionDescResId))
                    }

                    // Diğer Seçenekler Menüsü Butonu
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
                                leadingIcon = { Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.primary) }
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_privacy_policy)) },
                                onClick = {
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl)))
                                    } catch (e: Exception) {
                                        Log.e("CategoryScreen", "URL Açılamadı: $privacyPolicyUrl", e)
                                    }
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Policy, null, tint = MaterialTheme.colorScheme.secondary) }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_about)) },
                                onClick = { /* TODO: Hakkında ekranına git */ showMenu = false },
                                leadingIcon = { Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.tertiary) }
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
                                if (zoom > zoomInThreshold) { isGridViewVisible = false }
                            } else {
                                // Sadece Carousel görünümündeyken zoom out ile Grid'e geç
                                if (zoom < zoomOutThreshold) { isGridViewVisible = true }
                            }
                        }
                    }
            ) {
                when (categoriesState) {
                    is Resource.Loading -> LoadingAnimation(modifier = Modifier.align(Alignment.Center)) // Component çağrısı
                    is Resource.Success -> {
                        val categories = (categoriesState as Resource.Success<List<CategoryEntity>>).data
                        if (categories.isNullOrEmpty()) {
                            EmptyStateView(modifier = Modifier.align(Alignment.Center).padding(32.dp)) // Component çağrısı
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
                                    CategoryGridStaggered( // Component çağrısı
                                        categories = categories,
                                        onItemClick = { category -> navigateToQuizLocal(category) } // Lambda ile geçiş
                                    )
                                } else {
                                    CategoryCarousel( // Component çağrısı
                                        categories = categories,
                                        onCategorySelect = { category -> navigateToQuizLocal(category) } // Lambda ile geçiş
                                    )
                                }
                            }
                        }
                    }
                    is Resource.Error -> ErrorView( // Component çağrısı
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        message = (categoriesState as Resource.Error<List<CategoryEntity>>).message ?: stringResource(R.string.error_unknown),
                        onRetry = { viewModel.refreshCategories() }
                    )
                } // end when
            } // end İçerik Box
        } // end Arka Plan Box
    } // end Scaffold
}