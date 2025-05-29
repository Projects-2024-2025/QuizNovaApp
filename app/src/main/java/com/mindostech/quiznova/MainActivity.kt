package com.mindostech.quiznova

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.google.android.gms.ads.MobileAds
import com.mindostech.quiznova.data.datastore.ThemeSettingsRepository
import com.mindostech.quiznova.ui.navigation.AppNavigation
import com.mindostech.quiznova.util.ThemePreference
import com.mindostech.quiznova.ui.theme.QuizAppTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeSettingsRepository: ThemeSettingsRepository

    private var isAppContentReady by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        // 2. Sistem splash ekranının ne zaman gizleneceğini belirleyin
        // Kendi Composable içeriğimiz (AppNavigation -> SplashScreen) hazır olana kadar
        // sistem splash'ını ekranda tut.
        splashScreen.setKeepOnScreenCondition { !isAppContentReady } // EKLENDİ

        // MobileAds SDK başlatma
        // Application sınıfında olması daha iyi ama burada da kalabilir.
        // Uygulama bağlamı her zaman non-null olacağı için null kontrolüne gerek yok.
        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(applicationContext) {}
        }

        WindowCompat.setDecorFitsSystemWindows(window, false) // Edge-to-edge UI

        setContent {
            val currentThemePreference by themeSettingsRepository.themePreferenceFlow
                .collectAsState(initial = ThemePreference.LIGHT)

            // Composable ağacı ilk kez oluşturulduğunda veya belirli bir koşul sağlandığında
            // isAppContentReady'yi true yaparak sistem splash'ının kalkmasını sağlayın.
            // Bu, AppNavigation veya SplashScreen'in kendisi yüklendiğinde olabilir.
            // SideEffect, kompozisyon her başarıyla tamamlandığında çalışır.
            // Sadece bir kez çalıştırmak için bir kontrol ekliyoruz.
            SideEffect { // EKLENDİ
                if (!isAppContentReady) {
                    isAppContentReady = true
                    Timber.d("MainActivity: App content is now considered ready, system splash should dismiss.")
                }
            }

            QuizAppTheme(themePreference = currentThemePreference) {
                AppNavigation(
                    themePreference = currentThemePreference
                )
            }
        }
    }
}