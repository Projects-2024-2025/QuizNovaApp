package com.technovix.quiznova

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import com.technovix.quiznova.data.datastore.ThemeSettingsRepository
import com.technovix.quiznova.ui.navigation.AppNavigation
import com.technovix.quiznova.util.ThemePreference
import com.technovix.quiznova.ui.theme.QuizAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeSettingsRepository: ThemeSettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false) // For edge-to-edge UI.

        setContent {
            // Observe theme preference from DataStore, defaulting to LIGHT.
            val currentThemePreference by themeSettingsRepository.themePreferenceFlow
                .collectAsState(initial = ThemePreference.LIGHT)

            QuizAppTheme(themePreference = currentThemePreference) {
                AppNavigation(
                    themePreference = currentThemePreference
                )
            }
        }
    }
}