package com.technovix.quiznova.ui.navigation

import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.technovix.quiznova.ui.screen.category.CategoryScreen
import com.technovix.quiznova.ui.screen.quiz.QuizScreen
import com.technovix.quiznova.ui.screen.splash.SplashScreen
import androidx.compose.animation.*
import androidx.compose.ui.unit.IntOffset


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Animasyon süre ve offset ayarları
    val animationSpec = tween<IntOffset>(durationMillis = 400)
    val fadeSpec = tween<Float>(durationMillis = 400)

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        // Daha yumuşak ve modern geçiş animasyonları
        enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = animationSpec) + fadeIn(fadeSpec) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = animationSpec) + fadeOut(fadeSpec) }, // Çıkarken daha az kaysın
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = animationSpec) + fadeIn(fadeSpec) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it / 3 }, animationSpec = animationSpec) + fadeOut(fadeSpec) } // Geri gelirken daha az kaysın
    ) {
        composable(
            route = Screen.Splash.route,
            // Splash'tan çıkarken sadece fade out olsun
            exitTransition = { fadeOut(animationSpec = tween(500)) }
        ) {
            SplashScreen(navController = navController)
        }
        composable(Screen.Category.route) {
            CategoryScreen(navController = navController)
        }
        composable(
            route = Screen.Quiz.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.IntType },
                navArgument("categoryName") { type = NavType.StringType }
            )
            // Quiz ekranı için özel geçişler gerekirse buraya eklenebilir
            // enterTransition = { ... }
        ) { backStackEntry ->
            Log.d("AppNavigation", "QuizScreen composable entered. Args: ${backStackEntry.arguments}")
            val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: 0
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: "Unknown"
            QuizScreen(
                navController = navController,
                categoryId = categoryId,
                categoryName = categoryName
            )
        }
        // Gelecekteki ekranlar için composable'lar buraya eklenecek
        // composable(Screen.Settings.route) { SettingsScreen(...) }
        // composable(Screen.About.route) { AboutScreen(...) }
    }
}