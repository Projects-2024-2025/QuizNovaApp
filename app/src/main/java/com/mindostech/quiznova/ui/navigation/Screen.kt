package com.mindostech.quiznova.ui.navigation

// Defines application's navigation routes.
sealed class Screen(val route: String) {
    object Splash : Screen("splash") // Splash screen route
    object Category : Screen("category") // Category selection screen route
    object Quiz : Screen("quiz/{categoryId}/{categoryName}") { // Quiz screen route with arguments
        /**
         * Creates the route for the Quiz screen with specific category ID and name.
         * @param categoryId The ID of the selected category.
         * @param categoryName The name of the selected category.
         * @return The-generated route string.
         */
        fun createRoute(categoryId: Int, categoryName: String) = "quiz/$categoryId/$categoryName"
    }
    object Settings : Screen("settings") // Settings screen route
    object About : Screen("about") // About screen route
    object PrivacyPolicy: Screen("privacy_policy") // Privacy Policy screen route
}