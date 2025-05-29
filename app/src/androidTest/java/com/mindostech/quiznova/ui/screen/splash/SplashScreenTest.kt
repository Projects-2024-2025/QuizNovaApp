package com.mindostech.quiznova.ui.screen.splash

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.mindostech.quiznova.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest // Eğer Hilt kullanıyorsan
class SplashScreenTest {

    // Hilt kurallarını başta tanımla
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    // Activity'yi başlatmak için kural
    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    // Bekleme süresi (waitUntil için)
    private val KATEGORI_EKRANI_BEKLEME_SURESI_MS = 5000L // Splash delay + makul bir yüklenme süresi

    @Before
    fun setUp() {
        // Test öncesi Hilt enjeksiyonunu yap (eğer Hilt kullanıyorsan)
        hiltRule.inject()
        // Navigasyon veya başka başlangıç ayarları gerekiyorsa burada yapılabilir.
        // Ancak MainActivity'yi kullandığımız için genellikle gerekmez.
    }

    @Test
    fun splashScreen_displaysAndNavigatesToCategoryScreen() {
        // 1. Başlangıçta Splash Screen öğelerinin göründüğünü (isteğe bağlı ama iyi pratik) doğrula
        // Lottie animasyonunu içeren container'a bir testTag eklemek faydalı olabilir
        // Örnek: SplashScreen içindeki Box'a Modifier.testTag("SplashScreenContainer") ekle
        // composeTestRule.onNodeWithTag("SplashScreenContainer").assertIsDisplayed()
        composeTestRule.onNodeWithText("QuizNova").assertIsDisplayed()

        // 2. Kategori Ekranının belirli bir süre içinde göründüğünü doğrula (navigasyonun çalıştığını gösterir)
        // Kategori Ekranındaki sabit bir başlık veya listenin testTag'i kullanılabilir.
        // Örneğin CategoryScreen'deki başlığın "Kategoriler" olduğunu varsayalım:
        composeTestRule.waitUntil(timeoutMillis = KATEGORI_EKRANI_BEKLEME_SURESI_MS) {
            // Kategori ekranındaki bir düğümü bulmaya çalış
            composeTestRule
                .onAllNodesWithText("Kategoriler") // CategoryScreen başlığı varsayımı
                .fetchSemanticsNodes().size == 1
        }

        // 3. Kategori Ekranı göründükten sonra Splash Screen öğelerinin artık görünmediğini doğrula
        // (popUpTo çalıştığı için)
        composeTestRule.onNodeWithText("QuizNova").assertDoesNotExist()
        // composeTestRule.onNodeWithTag("SplashScreenContainer").assertDoesNotExist() // Eğer tag kullandıysan
    }
}