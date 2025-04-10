package com.technovix.quiznova

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import com.technovix.quiznova.ui.navigation.AppNavigation
import com.technovix.quiznova.util.ThemePreference
import com.technovix.quiznova.ui.theme.QuizAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false) // Kenardan kenara UI için

        setContent {
            // --- Tema Tercihi State Yönetimi ---
            // ÖNEMLİ: Bu state kalıcı değil. DataStore ile değiştirin!
            var currentThemePreference by remember { mutableStateOf(ThemePreference.SYSTEM) }
            QuizAppTheme(themePreference = currentThemePreference) {
                AppNavigation(
                    themePreference = currentThemePreference,
                    onThemeChange = { newPreference ->
                        currentThemePreference = newPreference
                        // TODO: Seçimi DataStore'a burada kaydedin!
                        // Örnek: settingsViewModel.saveThemePreference(newPreference)
                    }
                )
            }
        }
    }
}

