package com.technovix.quiznova.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.technovix.quiznova.ui.screen.about.AboutScreen
import com.technovix.quiznova.ui.screen.category.CategoryScreen
import com.technovix.quiznova.ui.screen.quiz.QuizScreen
import com.technovix.quiznova.ui.screen.settings.SettingsScreen
import com.technovix.quiznova.util.ThemePreference
import com.technovix.quiznova.ui.screen.splash.SplashScreen
import timber.log.Timber

@Composable
fun AppNavigation(
    // Tema state'i ve değiştiricisi parametre olarak eklendi
    themePreference: ThemePreference,
    //onThemeChange: (ThemePreference) -> Unit
) {
    val navController = rememberNavController()
    // Animasyon ayarları (isteğe bağlı)
    val animationSpec = tween<IntOffset>(durationMillis = 400)
    val fadeSpec = tween<Float>(durationMillis = 400)

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        // Navigasyon animasyonları (isteğe bağlı)
        enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = animationSpec) + fadeIn(fadeSpec) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = animationSpec) + fadeOut(fadeSpec) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = animationSpec) + fadeIn(fadeSpec) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it / 3 }, animationSpec = animationSpec) + fadeOut(fadeSpec) }
    ) {
        // Splash Ekranı
        composable(
            route = Screen.Splash.route,
            exitTransition = { fadeOut(animationSpec = tween(500)) } // Splash'tan çıkarken sadece fade out
        ) {
            SplashScreen(navController = navController)
        }

        // Kategori Ekranı
        composable(Screen.Category.route) {
            // CategoryScreen'e tema parametreleri iletiliyor
            CategoryScreen(
                navController = navController,
                currentTheme = themePreference // Mevcut tema state'i
            )
        }

        // Quiz Ekranı
        composable(
            route = Screen.Quiz.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.IntType },
                navArgument("categoryName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            Timber.tag("AppNavigation")
                .d("QuizScreen composable entered. Args: %s", backStackEntry.arguments)
            // val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: 0 // Gerekirse kullan
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: "Quiz"

            // QuizScreen'e tema parametresi iletiliyor
            QuizScreen(
                navController = navController,
                categoryName = categoryName,
                currentTheme = themePreference // Mevcut tema state'i
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController
            )
        }

        // About Screen
        composable(Screen.About.route) {
            AboutScreen(navController = navController)
        }
    }
}