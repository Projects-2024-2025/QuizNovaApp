package com.mindostech.quiznova.ui.screen.category

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mindostech.quiznova.R
import com.mindostech.quiznova.data.local.entity.CategoryEntity
import com.mindostech.quiznova.ui.components.common.GenericEmptyStateView
import com.mindostech.quiznova.ui.components.common.GenericErrorStateView
import com.mindostech.quiznova.ui.components.common.GenericLoadingStateView
import com.mindostech.quiznova.ui.navigation.Screen
import com.mindostech.quiznova.ui.screen.category.components.CategoryCarousel
import com.mindostech.quiznova.ui.screen.category.components.CategoryGridRegular
import com.mindostech.quiznova.util.ThemePreference
import com.mindostech.quiznova.ui.theme.*
import com.mindostech.quiznova.ui.viewmodel.CategoryViewModel
import com.mindostech.quiznova.ui.viewmodel.SettingsViewModel
import com.mindostech.quiznova.util.Resource
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    navController: NavController,
    viewModel: CategoryViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val currentTheme by settingsViewModel.currentTheme.collectAsState()
    val isOnline by settingsViewModel.isOnline.collectAsState()
    val categoriesState by viewModel.categories.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var isGridViewVisible by remember { mutableStateOf(true) }
    val zoomOutThreshold = 0.85f
    val zoomInThreshold = 1.15f

    val context = LocalContext.current
    val privacyPolicyUrl = "https://MindosTech.github.io/quiznova-privacy-policy/"



    val backgroundBrush = if (currentTheme == ThemePreference.DARK) {
        darkAppBackgroundGradient()
    } else {
        lightAppBackgroundGradient()
    }

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
                                    showMenu = false
                                    if (isOnline) {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(privacyPolicyUrl))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Timber.e("Could not open web privacy policy URL, falling back to internal.")
                                            navController.navigate(Screen.PrivacyPolicy.route)
                                        }
                                    } else {
                                        Timber.d("No internet (from ViewModel), opening internal privacy policy.")
                                        navController.navigate(Screen.PrivacyPolicy.route)
                                    }
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
                when (val currentState = categoriesState) {
                    is Resource.Loading -> {
                        GenericLoadingStateView(
                            loadingText = stringResource(R.string.loading_categories),
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is Resource.Success -> {
                        val categories = (categoriesState as Resource.Success<List<CategoryEntity>>).data
                        if (categories.isNullOrEmpty()) {
                            GenericEmptyStateView(
                                title = stringResource(R.string.category_empty_state),
                                icon = Icons.Filled.SentimentDissatisfied,
                                modifier = Modifier.align(Alignment.Center)
                            )
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
                                    CategoryGridRegular(
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
                    is Resource.Error -> {
                        GenericErrorStateView(
                            modifier = Modifier.align(Alignment.Center),
                            errorMessage = currentState.message ?: stringResource(R.string.error_unknown),
                            onRetryClick = { viewModel.refreshCategories() }
                        )
                    }
                }
            }
        }
    }
}