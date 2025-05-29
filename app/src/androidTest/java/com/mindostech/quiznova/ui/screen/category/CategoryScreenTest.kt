package com.mindostech.quiznova.ui.screen.category

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mindostech.quiznova.MainActivity
import com.mindostech.quiznova.R
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest // Hilt'i etkinleştirir ve TestAppModule'ü kullanır
@RunWith(AndroidJUnit4::class)
class CategoryScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    // Test modülünden mock repository'yi almak isterseniz (opsiyonel)
    // @Inject lateinit var repository: QuizRepository

    @Before
    fun setUp() {
        hiltRule.inject()
        // Thread.sleep KESİNLİKLE KALDIRILDI!
    }

    @Test
    fun categoryScreen_displaysAppBarTitle() {
        // Arrange: Başlığın görünmesini bekle
        val expectedTitle = composeTestRule.activity.getString(R.string.category_screen_title)
        composeTestRule.waitUntil(timeoutMillis = 5000) { // Daha kısa timeout yeterli olmalı
            composeTestRule.onAllNodesWithText(expectedTitle)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Act & Assert:
        composeTestRule.onNodeWithText(expectedTitle).assertIsDisplayed()
        val moreOptionsDesc = composeTestRule.activity.getString(R.string.cd_more_options)
        composeTestRule.onNodeWithContentDescription(moreOptionsDesc).assertIsDisplayed()
    }

    @Test
    fun categoryScreen_displaysCategories_whenRepoReturnsSuccess() {
        // Arrange: TestAppModule zaten başarılı veri döndürecek şekilde ayarlandı.
        // Sadece UI'ın çizilmesini bekleyelim.
        val categoryNameFromMock = "General Knowledge" // Mock'ta tanımladığımız isim
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText(categoryNameFromMock)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Act & Assert:
        composeTestRule.onNodeWithText(categoryNameFromMock).assertIsDisplayed()
        composeTestRule.onNodeWithText("Sports").assertIsDisplayed() // Diğer mock kategori
    }


    @Test
    fun clickingCategory_navigatesToQuizScreen() {
        // Arrange: Kategorilerin yüklenmesini bekle
        val categoryToClick = "General Knowledge" // Mock'ta var
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText(categoryToClick, substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Act: Kategoriye tıkla
        composeTestRule.onNodeWithText(categoryToClick, substring = true, ignoreCase = true)
            .performClick()

        // Assert: Quiz Ekranının başlığını bekle
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText(categoryToClick, substring = true, ignoreCase = true)
                .fetchSemanticsNodes().isNotEmpty() // Başlıkta kategori adı var mı?
        }
        composeTestRule.onNodeWithText(categoryToClick, substring = true, ignoreCase = true)
            .assertIsDisplayed()

        // Geri butonunu kontrol et
        val backButtonDesc = composeTestRule.activity.getString(R.string.cd_back)
        composeTestRule.onNodeWithContentDescription(backButtonDesc).assertIsDisplayed()
    }
}