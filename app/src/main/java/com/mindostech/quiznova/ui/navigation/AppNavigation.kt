package com.mindostech.quiznova.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mindostech.quiznova.ui.screen.about.AboutScreen
import com.mindostech.quiznova.ui.screen.category.CategoryScreen
import com.mindostech.quiznova.ui.screen.privacypolicy.PrivacyPolicyScreen
import com.mindostech.quiznova.ui.screen.quiz.QuizScreen
import com.mindostech.quiznova.ui.screen.settings.SettingsScreen
import com.mindostech.quiznova.util.ThemePreference
import com.mindostech.quiznova.ui.screen.splash.SplashScreen
import timber.log.Timber

@Composable
fun AppNavigation(
    themePreference: ThemePreference
) {
    val navController = rememberNavController()
    val animationSpec = tween<IntOffset>(durationMillis = 400)
    val fadeSpec = tween<Float>(durationMillis = 400)

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = animationSpec) + fadeIn(fadeSpec) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 3 }, animationSpec = animationSpec) + fadeOut(fadeSpec) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = animationSpec) + fadeIn(fadeSpec) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it / 3 }, animationSpec = animationSpec) + fadeOut(fadeSpec) }
    ) {
        composable(
            route = Screen.Splash.route,
            exitTransition = { fadeOut(animationSpec = tween(500)) }
        ) {
            SplashScreen(navController = navController)
        }

        composable(Screen.Category.route) {
            CategoryScreen(
                navController = navController
            )
        }

        composable(
            route = Screen.Quiz.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.IntType },
                navArgument("categoryName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            Timber.tag("AppNavigation")
                .d("QuizScreen composable entered. Args: %s", backStackEntry.arguments)
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: "Quiz"

            QuizScreen(
                navController = navController,
                categoryName = categoryName,
                currentTheme = themePreference
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                navController = navController
            )
        }

        composable(Screen.About.route) {
            AboutScreen(navController = navController)
        }

        composable(Screen.PrivacyPolicy.route) {
            PrivacyPolicyScreen(navController = navController)
        }
    }
}