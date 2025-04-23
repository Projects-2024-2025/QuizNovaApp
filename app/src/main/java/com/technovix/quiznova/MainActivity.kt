package com.technovix.quiznova

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.technovix.quiznova.data.datastore.ThemeSettingsRepository
import com.technovix.quiznova.ui.navigation.AppNavigation
import com.technovix.quiznova.util.ThemePreference
import com.technovix.quiznova.ui.theme.QuizAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeSettingsRepository: ThemeSettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false) // Kenardan kenara UI için

        setContent {
            // --- Tema Tercihi State Yönetimi ---
            // ÖNEMLİ: Bu state kalıcı değil. DataStore ile değiştirin!
            val currentThemePreference by themeSettingsRepository.themePreferenceFlow
                .collectAsState(initial = ThemePreference.SYSTEM )
            QuizAppTheme(themePreference = currentThemePreference) {
                AppNavigation(
                    themePreference = currentThemePreference,
                    onThemeChange = { newPreference ->
                        lifecycleScope.launch {
                            themeSettingsRepository.saveThemePreference(newPreference)
                        }
                    }
                )
            }
        }
    }
}

