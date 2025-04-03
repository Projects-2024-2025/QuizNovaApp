package com.technovix.quiznova.ui.navigation

// Rotaları tanımlayan sealed class (Değişiklik yok)
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Category : Screen("category")
    object Quiz : Screen("quiz/{categoryId}/{categoryName}") {
        fun createRoute(categoryId: Int, categoryName: String) = "quiz/$categoryId/$categoryName"
    }
    // Gelecekte eklenebilecek ekranlar için buraya ekleme yapabilirsin
    // object Settings : Screen("settings")
    // object About : Screen("about")
}