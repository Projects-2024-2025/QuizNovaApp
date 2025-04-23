package com.technovix.quiznova.ui.screen.quiz

import androidx.compose.ui.test.* // Temel Compose Test importları
import androidx.compose.ui.test.junit4.createAndroidComposeRule // Activity ile test kuralı
import androidx.test.ext.junit.runners.AndroidJUnit4 // Test Runner
import com.technovix.quiznova.MainActivity // Test edilecek Activity
import com.technovix.quiznova.R // String kaynakları
// Test modülü ve ayarlanabilir repo interface'i
import com.technovix.quiznova.di.ConfigurableMockQuizRepository
import com.technovix.quiznova.data.local.entity.QuestionEntity // Gerekli Entity
import com.technovix.quiznova.data.repository.QuizRepository // Inject edilecek tip
// import com.technovix.quiznova.util.Resource // Gerekirse
import dagger.hilt.android.testing.HiltAndroidRule // Hilt test kuralı
import dagger.hilt.android.testing.HiltAndroidTest // Hilt test anotasyonu
// import io.mockk.coEvery // Artık gerekli değil (mock repo implementasyonu var)
// import kotlinx.coroutines.Dispatchers // MainDispatcherRule için
// import kotlinx.coroutines.ExperimentalCoroutinesApi // MainDispatcherRule için
// import kotlinx.coroutines.flow.flowOf // Artık gerekli değil
// import kotlinx.coroutines.test.StandardTestDispatcher // MainDispatcherRule için
// import kotlinx.coroutines.test.TestDispatcher // MainDispatcherRule için
// import kotlinx.coroutines.test.resetMain // MainDispatcherRule için
// import kotlinx.coroutines.test.setMain // MainDispatcherRule için
import org.junit.After // @After için
import org.junit.Before
import org.junit.Rule
import org.junit.Test
// import org.junit.rules.TestWatcher // MainDispatcherRule için
// import org.junit.runner.Description // MainDispatcherRule için
import org.junit.runner.RunWith
import javax.inject.Inject // Hilt enjeksiyonu için
import androidx.test.espresso.Espresso

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class QuizScreenTest {

    // --- Kurallar ---
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    // Mock Repository'yi inject et
    @Inject
    lateinit var mockQuizRepository: QuizRepository

    // Mock repository'yi güvenli cast etmek için helper property
    private val configurableRepo: ConfigurableMockQuizRepository
        get() = mockQuizRepository as? ConfigurableMockQuizRepository
            ?: throw IllegalStateException("Injected repository is not ConfigurableMockQuizRepository")

    // --- Test Sabitleri ---
    private val categoryToTest = "General Knowledge"
    // Test verisi oluşturmak için yardımcı fonksiyon (TestAppModule'deki ile aynı)
    private fun createFakeQuestion(id: Int, category: String): QuestionEntity {
        val correctAnswer = "Correct $id"
        val incorrectAnswers = listOf("Wrong A$id", "Wrong B$id")
        val allAnswers = listOf(correctAnswer) + incorrectAnswers // Sabit sıra
        return QuestionEntity(
            id = id, category = category, type = "multiple", difficulty = "easy",
            question = "Test Question $id for $category?",
            correctAnswer = correctAnswer,
            incorrectAnswers = incorrectAnswers,
            allAnswers = allAnswers
        )
    }
    // Kolay erişim için soru nesneleri
    private val question1 = createFakeQuestion(1, categoryToTest)
    private val question2 = createFakeQuestion(2, categoryToTest)

    // --- String Kaynakları (Daha okunaklı testler için) ---
    private lateinit var submitButtonText: String
    private lateinit var nextButtonText: String
    private lateinit var finishButtonText: String
    private lateinit var playAgainButtonText: String
    private lateinit var backToCategoriesButtonText: String
    private lateinit var progressTextTemplate: String // "Soru %1$d / %2$d"
    private lateinit var errorViewTitle: String // Örn: R.string.error_oops
    private lateinit var retryButtonText: String // Örn: R.string.retry
    private lateinit var emptyViewText: String // Örn: R.string.quiz_no_questions_found
    private lateinit var backButtonText: String // Örn: R.string.back


    // --- Kurulum ve Temizlik ---
    @Before
    fun setUp() {
        hiltRule.inject() // Önce inject et

        // String kaynaklarını al
        submitButtonText = composeTestRule.activity.getString(R.string.quiz_submit_answer)
        nextButtonText = composeTestRule.activity.getString(R.string.quiz_next_question)
        finishButtonText = composeTestRule.activity.getString(R.string.quiz_finish)
        playAgainButtonText = composeTestRule.activity.getString(R.string.quiz_result_play_again)
        backToCategoriesButtonText = composeTestRule.activity.getString(R.string.quiz_result_back_to_categories)
        progressTextTemplate = composeTestRule.activity.getString(R.string.quiz_question_progress) // Format string'i al

        errorViewTitle = composeTestRule.activity.getString(R.string.error_oops) // Kendi ID'nizle değiştirin
        retryButtonText = composeTestRule.activity.getString(R.string.retry) // Kendi ID'nizle değiştirin
        emptyViewText = composeTestRule.activity.getString(R.string.quiz_no_questions_found) // Kendi ID'nizle değiştirin
        backButtonText = composeTestRule.activity.getString(R.string.back) // Kendi ID'nizle değiştirin

        // Her testten önce repo'yu varsayılan durumuna sıfırla (güvenlik için)
        configurableRepo.reset()
        // NOT: Her test KENDİ ihtiyacına göre repo'yu ayarlayacak.
    }

    @After
    fun tearDown() {
        // İsteğe bağlı: Test sonrası sıfırlama (genellikle @Before'daki yeterli)
        // configurableRepo.reset()
    }

    // --- Yardımcı Fonksiyonlar ---

    // Quiz ekranına gitmek için yardımcı fonksiyon (Repo state'i DIŞARIDAN ayarlanmalı)
    private fun navigateToQuizScreen(category: String = categoryToTest, expectedFirstQuestion: QuestionEntity) {
        // Kategori ekranının yüklenmesini ve tıklanacak kategorinin görünmesini bekle
        println("TEST: Waiting for category '$category'...")
        composeTestRule.waitUntil(timeoutMillis = 15000) { // Timeout artırıldı
            composeTestRule.onAllNodesWithText(category, substring = true, ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
        }
        println("TEST: Category node found. Performing click...")
        composeTestRule.onNodeWithText(category, substring = true, ignoreCase = true)
            .performClick()
        println("TEST: Clicked category '$category'.")

        // Quiz ekranının yüklenmesini ve İLK BEKLENEN sorunun görünmesini bekle
        println("TEST: Waiting for first question text: ${expectedFirstQuestion.question}")
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            composeTestRule.onAllNodesWithText(expectedFirstQuestion.question).fetchSemanticsNodes().isNotEmpty()
        }
        println("TEST: First question found.")
        composeTestRule.waitForIdle()
        println("TEST: UI is idle. Ready for Quiz Screen assertions.")
    }

    // Sonuç ekranına gitmek için yardımcı fonksiyon (Repo'yu 1 soruya ayarlar)
    private fun goToResultScreen(category: String = categoryToTest, question: QuestionEntity, answerCorrectly: Boolean = true) {
        // 1. Mock Repository'yi 1 soruya ayarla
        println("goToResultScreen: Setting mock repo to 1 question for $category")
        configurableRepo.setQuestionsForCategory(category, listOf(question)) // Sadece verilen soruyu ayarla

        // 2. Quiz ekranına git ve ilk (tek) sorunun yüklenmesini bekle
        navigateToQuizScreen(category, question)

        // 3. Tek soruyu cevapla
        val answerToClick = if (answerCorrectly) question.correctAnswer else question.incorrectAnswers.first()
        println("TEST: Clicking answer: $answerToClick")
        composeTestRule.onNodeWithText(answerToClick).performClick()
        composeTestRule.waitForIdle() // Seçimin işlenmesini bekle

        // 4. Cevabı gönder
        println("TEST: Submitting the only answer...")
        composeTestRule.onNodeWithText(submitButtonText).assertIsEnabled().performClick()
        println("TEST: Submitted answer.")

        // 5. "Bitir" butonunun görünmesini bekle ve tıkla
        println("TEST: Waiting for Finish button...")
        composeTestRule.waitUntil(timeoutMillis = 10000) { // Bitir butonunu bekle
            composeTestRule.onAllNodesWithText(finishButtonText).fetchSemanticsNodes().isNotEmpty()
        }
        println("TEST: Finish button found. Clicking...")
        composeTestRule.onNodeWithText(finishButtonText).assertIsDisplayed().performClick()
        println("TEST: Clicked Finish button. Waiting for result screen...")

        // 6. Sonuç ekranının yüklendiğini doğrula ("Tekrar Oyna" butonu ile)
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText(playAgainButtonText).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.waitForIdle() // Sonuç ekranının tam oturmasını bekle
        println("TEST: Result screen reached.")
    }

    // --- TEST FONKSİYONLARI ---

    @Test
    fun quizScreen_displaysFirstQuestionAndAnswers_andInitialButtonState() {
        // Arrange: Bu test için 2 soru ayarlayalım (tipik senaryo)
        configurableRepo.setQuestionsForCategory(categoryToTest, listOf(question1, question2))
        navigateToQuizScreen(categoryToTest, question1) // İlk soru question1 olmalı

        // Assert
        // Kategori başlığı varsa kontrol edilebilir:
        // composeTestRule.onNodeWithTag("quiz_category_title").assertTextEquals(categoryToTest)
        composeTestRule.onNodeWithText(question1.question).assertIsDisplayed()
        composeTestRule.onNodeWithText(question1.correctAnswer).assertIsDisplayed()
        composeTestRule.onNodeWithText(question1.incorrectAnswers.first()).assertIsDisplayed() // Bir yanlış cevap yeterli
        composeTestRule.onNodeWithText(submitButtonText)
            .assertIsDisplayed()
            .assertIsNotEnabled() // Başlangıçta pasif
        // İlerleme göstergesini kontrol et
        composeTestRule.onNodeWithText(progressTextTemplate.format(1, 2)).assertIsDisplayed() // 1/2
    }

    @Test
    fun selectingCorrectAnswer_enablesSubmitButton_andSubmitting_showsNextButton() {
        // Arrange: Bu test 2 soru gerektirir
        configurableRepo.setQuestionsForCategory(categoryToTest, listOf(question1, question2))
        navigateToQuizScreen(categoryToTest, question1)
        composeTestRule.waitForIdle()

        // Act 1: Doğru cevabı seç (question1 için)
        composeTestRule.onNodeWithText(question1.correctAnswer).performClick()
        composeTestRule.waitForIdle()

        // Assert 1: Gönder butonu etkin
        composeTestRule.onNodeWithText(submitButtonText).assertIsEnabled()

        // Act 2: Cevabı gönder
        composeTestRule.onNodeWithText(submitButtonText).performClick()
        composeTestRule.waitForIdle() // State güncellemesini ve animasyonu bekle

        // Assert 2: "Sonraki Soru" butonu görünür ve etkin
        println("TEST: Waiting for next button after correct answer...")
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText(nextButtonText).fetchSemanticsNodes().isNotEmpty()
        }
        println("TEST: Next button found. Asserting...")
        val nextButtonNode = composeTestRule.onNodeWithText(nextButtonText)
        nextButtonNode.assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithText(submitButtonText).assertDoesNotExist() // Gönder butonu kaybolmalı
        println("TEST: Next button assertions passed.")
    }

    @Test
    fun selectingIncorrectAnswer_enablesSubmitButton_andSubmitting_showsNextButton() {
        // Arrange: Bu test 2 soru gerektirir
        configurableRepo.setQuestionsForCategory(categoryToTest, listOf(question1, question2))
        navigateToQuizScreen(categoryToTest, question1)
        composeTestRule.waitForIdle()

        // Act 1: Yanlış cevabı seç (question1 için)
        composeTestRule.onNodeWithText(question1.incorrectAnswers.first()).performClick()
        composeTestRule.waitForIdle()

        // Assert 1: Gönder butonu etkin
        composeTestRule.onNodeWithText(submitButtonText).assertIsEnabled()

        // Act 2: Cevabı gönder
        composeTestRule.onNodeWithText(submitButtonText).performClick()
        composeTestRule.waitForIdle() // State güncellemesini ve animasyonu bekle

        // Assert 2: "Sonraki Soru" butonu görünür ve etkin
        println("TEST: Waiting for next button after incorrect answer...")
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText(nextButtonText).fetchSemanticsNodes().isNotEmpty()
        }
        println("TEST: Next button found. Asserting...")
        val nextButtonNode = composeTestRule.onNodeWithText(nextButtonText)
        nextButtonNode.assertIsDisplayed().assertIsEnabled()
        composeTestRule.onNodeWithText(submitButtonText).assertDoesNotExist()
        println("TEST: Next button assertions passed.")
    }

    @Test
    fun clickingNextQuestion_loadsNextQuestion_andResetsState() {
        // Arrange: Bu test 2 soru gerektirir
        configurableRepo.setQuestionsForCategory(categoryToTest, listOf(question1, question2))
        navigateToQuizScreen(categoryToTest, question1)
        composeTestRule.waitForIdle()

        // İlk soruyu cevapla ve gönder (örn: doğru cevap)
        composeTestRule.onNodeWithText(question1.correctAnswer).performClick()
        composeTestRule.onNodeWithText(submitButtonText).performClick()
        composeTestRule.waitForIdle() // State güncellemesini bekle

        // "Sonraki Soru" butonunun görünmesini bekle
        println("TEST: Waiting for next button before clicking next...")
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText(nextButtonText).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(nextButtonText).assertIsDisplayed()
        println("TEST: Next button found.")

        // Act: "Sonraki Soru" butonuna tıkla
        composeTestRule.onNodeWithText(nextButtonText).performClick()
        composeTestRule.waitForIdle() // Yeni soru yüklenmesini bekle

        // Assert: İkinci sorunun yüklendiğini ve durumun sıfırlandığını doğrula
        println("TEST: Waiting for question 2 text...")
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText(question2.question).fetchSemanticsNodes().isNotEmpty()
        }
        println("TEST: Question 2 text found. Asserting state...")
        composeTestRule.onNodeWithText(question2.question).assertIsDisplayed()
        composeTestRule.onNodeWithText(question1.question).assertDoesNotExist() // İlk soru gitmeli
        // İkinci sorunun cevapları görünmeli
        composeTestRule.onNodeWithText(question2.correctAnswer).assertIsDisplayed()
        composeTestRule.onNodeWithText(question2.incorrectAnswers.first()).assertIsDisplayed()
        // Gönder butonu tekrar görünür ve pasif olmalı
        composeTestRule.onNodeWithText(submitButtonText)
            .assertIsDisplayed()
            .assertIsNotEnabled()
        // Sonraki Soru butonu kaybolmalı
        composeTestRule.onNodeWithText(nextButtonText).assertDoesNotExist()
        // İlerleme göstergesini kontrol et
        composeTestRule.onNodeWithText(progressTextTemplate.format(2, 2)).assertIsDisplayed() // 2/2
        println("TEST: Question 2 assertions passed.")
    }

    @Test
    fun answeringLastQuestion_andClickingFinish_displaysResultScreen_withCorrectScore() {
        // Arrange: Bu test 1 soru gerektirir. goToResultScreen bunu halleder.
        val score = 1
        val total = 1
        // expectedScoreText = "$score/$total" // Artık bu şekilde tek bir string'e ihtiyacımız yok

        // Act: Sonuç ekranına git (doğru cevap vererek)
        goToResultScreen(categoryToTest, question1, answerCorrectly = true)

        // Assert: Sonuç Ekranı öğeleri görünür ve skor doğru
        println("TEST: Asserting result screen elements...")
        composeTestRule.onNodeWithText(playAgainButtonText).assertIsDisplayed()
        composeTestRule.onNodeWithText(backToCategoriesButtonText).assertIsDisplayed()

        // --- DÜZELTİLMİŞ SKOR KONTROLÜ ---
        // Skor değerini ("1") ayrı olarak kontrol et
        composeTestRule.onNodeWithText("$score", useUnmergedTree = true).assertIsDisplayed()
        // Toplam soru sayısını ("/1") ayrı olarak kontrol et
        composeTestRule.onNodeWithText("/$total", useUnmergedTree = true).assertIsDisplayed()
        // ------------------------------------

        // Özet başlığını kontrol et (opsiyonel)
        val summaryTitle = composeTestRule.activity.getString(R.string.quiz_result_summary)
        composeTestRule.onNodeWithText(summaryTitle).assertIsDisplayed()
        println("TEST: Result screen score and elements assertions passed.")
    }

    // --- YENİ EKLENEN SONUÇ EKRANI TESTLERİ ---

    @Test
    fun resultScreen_clickPlayAgain_restartsQuiz() {
        // Arrange: Sonuç ekranına git (1 soru ile)
        goToResultScreen(categoryToTest, question1, answerCorrectly = true)
        println("TEST: On result screen. Clicking 'Play Again'...")

        // Act: "Tekrar Oyna" butonuna tıkla
        composeTestRule.onNodeWithText(playAgainButtonText).performClick()
        // ÖNEMLİ: waitForIdle() burada genellikle YETMEZ.
        // State'in güncellenip UI'ın yeniden çizilmesini beklememiz lazım.
        // En iyi yol, YENİ state'in bir göstergesini beklemektir.
        println("TEST: Clicked 'Play Again'. Waiting for quiz restart (question text)...")

        // --- YENİ BEKLEME ADIMI ---
        // Quiz'in yeniden başladığını doğrulamak için ilk sorunun metninin
        // tekrar görünmesini bekle.
        try {
            composeTestRule.waitUntil(timeoutMillis = 10000) { // Timeout artırılabilir
                composeTestRule.onAllNodesWithText(question1.question).fetchSemanticsNodes().isNotEmpty()
            }
            println("TEST: Question text reappeared. Asserting quiz restart...")
        } catch (e: ComposeTimeoutException) {
            println("TEST ERROR: Question text did not reappear after clicking Play Again!")
            composeTestRule.onRoot().printToLog("PlayAgainFail") // UI hiyerarşisini yazdır
            throw e // Testi başarısız yap
        }
        // Şimdi UI'ın yeniden yüklendiğinden daha eminiz.

        // Assert: Quiz ekranının sıfırlandığını doğrula
        composeTestRule.onNodeWithText(question1.question).assertIsDisplayed() // Zaten beklemiştik ama tekrar assert edelim.
        // Soru sayacının sıfırlandığını kontrol et (1/1)
        composeTestRule.onNodeWithText(progressTextTemplate.format(1, 1)).assertIsDisplayed()
        // Gönder butonunun tekrar görünür ve pasif olduğunu kontrol et
        composeTestRule.onNodeWithText(submitButtonText)
            .assertIsDisplayed()
            .assertIsNotEnabled()
        // Sonraki/Bitir butonunun görünmediğini kontrol et
        composeTestRule.onNodeWithText(finishButtonText).assertDoesNotExist()
        composeTestRule.onNodeWithText(nextButtonText).assertDoesNotExist()

        // --- DÜZELTİLMİŞ SKOR YOKLUĞU KONTROLÜ ---
        // Önceki skorun (ayrı ayrı) artık GÖRÜNMEMESİ gerektiğini kontrol et.
        composeTestRule.onNodeWithText("1", useUnmergedTree = true).assertDoesNotExist()
        composeTestRule.onNodeWithText("/1", useUnmergedTree = true).assertDoesNotExist()
        // Tek node'u kontrol etmek yerine ayrı ayrı kontrol etmek daha güvenli.
        // val scoreText = "1/1" // Bu satırı kaldırabiliriz.
        // composeTestRule.onNodeWithText(scoreText).assertDoesNotExist() // Bu satırı kaldırabiliriz.
        // --------------------------------------------

        // Sonuç ekranı butonlarının kaybolduğunu kontrol et
        composeTestRule.onNodeWithText(playAgainButtonText).assertDoesNotExist()
        composeTestRule.onNodeWithText(backToCategoriesButtonText).assertDoesNotExist()
        println("TEST: Quiz restart assertions passed.")
    }

    @Test
    fun resultScreen_clickBackToCategories_navigatesToCategoryScreen() {
        // Arrange: Sonuç ekranına git (1 soru ile)
        goToResultScreen(categoryToTest, question1, answerCorrectly = true)
        println("TEST: On result screen. Clicking 'Back to Categories'...")

        // Act: "Kategorilere Dön" butonuna tıkla
        composeTestRule.onNodeWithText(backToCategoriesButtonText).performClick()
        println("TEST: Clicked 'Back to Categories'. Waiting for Category Screen...")

        // Kategori ekranına dönüldüğünü doğrulamak için bekle
        try {
            println("TEST: Waiting for category '$categoryToTest' to reappear...")
            composeTestRule.waitUntil(timeoutMillis = 10000) {
                composeTestRule.onAllNodesWithText(categoryToTest, substring = true, ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
            }
            println("TEST: Category screen loaded. Asserting navigation results...")
        } catch (e: ComposeTimeoutException) {
            println("TEST ERROR: Category text did not reappear after clicking Back to Categories!")
            composeTestRule.onRoot().printToLog("BackToCatFail")
            throw e
        }

        // Assert: Kategori ekranına dönüldüğünü ve ÖNCEKİ ekran öğelerinin kaybolduğunu doğrula

        // 1. Kategori ekranındaki öğenin göründüğünü tekrar doğrula
        composeTestRule.onNodeWithText(categoryToTest, substring = true, ignoreCase = true).assertIsDisplayed()

        // --- DÜZELTİLMİŞ YOKLUK KONTROLLERİ ---
        // 2. Quiz/Sonuç ekranına ÖZGÜ öğelerin ARTIK GÖRÜNMEDİĞİNİ kontrol et:
        composeTestRule.onNodeWithText(question1.question).assertDoesNotExist() // Quiz sorusu
        composeTestRule.onNodeWithText(submitButtonText).assertDoesNotExist() // Quiz gönder butonu
        composeTestRule.onNodeWithText(playAgainButtonText).assertDoesNotExist() // Sonuç ekranı "Tekrar Oyna" butonu
        // Skor metinleri
        composeTestRule.onNodeWithText("1", useUnmergedTree = true).assertDoesNotExist()
        composeTestRule.onNodeWithText("/1", useUnmergedTree = true).assertDoesNotExist()

        // **KALDIRILDI:** Aşağıdaki satır, Kategori ekranı başlığı ile çakıştığı için kaldırıldı.
        // composeTestRule.onNodeWithText(backToCategoriesButtonText).assertDoesNotExist()
        // -----------------------------------------

        println("TEST: Category screen navigation assertions passed.")
    }

    @Test
    fun pressingBackButton_showsExitConfirmationDialog() {
        // Arrange: Quiz Ekranına git (örn: 1 soru yeterli)
        configurableRepo.setQuestionsForCategory(categoryToTest, listOf(question1))
        navigateToQuizScreen(categoryToTest, question1) // İlk soru question1 olmalı
        println("TEST: On quiz screen. Pressing back button...")

        // Act: Sistem geri tuşuna bas
        Espresso.pressBack()
        composeTestRule.waitForIdle() // Dialogun görünmesi için bekle

        // Assert: Dialog görünür ve doğru metinleri içerir
        val dialogTitle = composeTestRule.activity.getString(R.string.quiz_exit_dialog_title)
        val exitButtonText = composeTestRule.activity.getString(R.string.exit)
        val cancelButtonText = composeTestRule.activity.getString(R.string.cancel)

        println("TEST: Asserting dialog is displayed...")
        composeTestRule.onNodeWithText(dialogTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(exitButtonText).assertIsDisplayed()
        composeTestRule.onNodeWithText(cancelButtonText).assertIsDisplayed()
        println("TEST: Dialog display assertions passed.")
    }

    @Test
    fun exitDialog_clickingCancel_dismissesDialogAndStaysOnQuiz() {
        // Arrange: Quiz Ekranına git ve geri tuşuna basarak dialogu göster
        configurableRepo.setQuestionsForCategory(categoryToTest, listOf(question1))
        navigateToQuizScreen(categoryToTest, question1)
        Espresso.pressBack()
        composeTestRule.waitForIdle()
        val dialogTitle = composeTestRule.activity.getString(R.string.quiz_exit_dialog_title)
        val cancelButtonText = composeTestRule.activity.getString(R.string.cancel)
        composeTestRule.onNodeWithText(dialogTitle).assertIsDisplayed() // Dialogun başta göründüğünden emin ol
        println("TEST: Dialog shown. Clicking cancel...")

        // Act: "İptal" butonuna tıkla
        composeTestRule.onNodeWithText(cancelButtonText).performClick()
        composeTestRule.waitForIdle() // Dialogun kaybolması için bekle
        println("TEST: Clicked cancel. Asserting dialog dismissed...")

        // Assert: Dialog kayboldu ve Quiz Ekranı hala görünür
        composeTestRule.onNodeWithText(dialogTitle).assertDoesNotExist()
        // Quiz ekranı öğelerinin hala var olduğunu kontrol et
        composeTestRule.onNodeWithText(question1.question).assertIsDisplayed()
        composeTestRule.onNodeWithText(submitButtonText).assertIsDisplayed() // Gönder butonu hala olmalı
        println("TEST: Cancel button assertions passed.")
    }

    @Test
    fun exitDialog_clickingExit_navigatesBackToCategoryScreen() {
        // Arrange: Quiz Ekranına git ve geri tuşuna basarak dialogu göster
        configurableRepo.setQuestionsForCategory(categoryToTest, listOf(question1))
        navigateToQuizScreen(categoryToTest, question1)
        Espresso.pressBack()
        composeTestRule.waitForIdle()
        val dialogTitle = composeTestRule.activity.getString(R.string.quiz_exit_dialog_title)
        val exitButtonText = composeTestRule.activity.getString(R.string.exit)
        composeTestRule.onNodeWithText(dialogTitle).assertIsDisplayed() // Dialogun başta göründüğünden emin ol
        println("TEST: Dialog shown. Clicking exit...")

        // Act: "Çıkış" butonuna tıkla
        composeTestRule.onNodeWithText(exitButtonText).performClick()
        composeTestRule.waitForIdle() // Navigasyon için bekle
        println("TEST: Clicked exit. Waiting for Category Screen...")

        // Assert: Dialog kayboldu ve Kategori Ekranına dönüldü

        // 1. Kategori ekranındaki bir öğenin görünmesini bekle
        try {
            println("TEST: Waiting for category '$categoryToTest' to reappear after exit...")
            composeTestRule.waitUntil(timeoutMillis = 10000) {
                composeTestRule.onAllNodesWithText(categoryToTest, substring = true, ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
            }
            println("TEST: Category screen reappeared.")
        } catch (e: ComposeTimeoutException) {
            println("TEST ERROR: Category text did not reappear after clicking Exit!")
            composeTestRule.onRoot().printToLog("ExitBackFail") // UI hiyerarşisini yazdır
            throw e
        }

        // 2. Kategori ekranı öğesini doğrula
        composeTestRule.onNodeWithText(categoryToTest, substring = true, ignoreCase = true).assertIsDisplayed()

        // 3. Dialog ve Quiz ekranı öğelerinin kaybolduğunu doğrula
        composeTestRule.onNodeWithText(dialogTitle).assertDoesNotExist()
        composeTestRule.onNodeWithText(question1.question).assertDoesNotExist()
        composeTestRule.onNodeWithText(submitButtonText).assertDoesNotExist()
        println("TEST: Exit button assertions passed.")
    }


    @Test
    fun errorFetchingQuestions_displaysErrorView_withCorrectMessage() {
        // Arrange: Repo'nun soruları getirirken hata vermesini sağla
        val specificErrorMessage = "Network connection failed!"
        configurableRepo.setQuestionsError(categoryToTest, Exception(specificErrorMessage))
        println("TEST: Configured repository to return error: $specificErrorMessage")

        // Act: Kategoriye tıkla ve Quiz Ekranına gitmeye çalış
        // navigateToQuizScreen burada çalışmaz çünkü soru beklemez. Manuel yapalım:
        println("TEST: Waiting for category '$categoryToTest'...")
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            composeTestRule.onAllNodesWithText(categoryToTest, substring = true, ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
        }
        println("TEST: Category node found. Performing click...")
        composeTestRule.onNodeWithText(categoryToTest, substring = true, ignoreCase = true)
            .performClick()
        println("TEST: Clicked category '$categoryToTest'. Waiting for error view...")

        // Hata mesajının görünmesini bekle (bu, ErrorView'ın yüklendiğini gösterir)
        try {
            composeTestRule.waitUntil(timeoutMillis = 10000) {
                composeTestRule.onAllNodesWithText(specificErrorMessage).fetchSemanticsNodes().isNotEmpty()
            }
            println("TEST: Error message found. Asserting error view...")
        } catch (e: ComposeTimeoutException) {
            println("TEST ERROR: Specific error message '$specificErrorMessage' not found!")
            composeTestRule.onRoot().printToLog("ErrorViewFail")
            throw e
        }

        // Assert: Hata görünümü ve doğru mesaj gösterilir, quiz içeriği gösterilmez
        composeTestRule.onNodeWithText(errorViewTitle).assertIsDisplayed() // Hata başlığı
        composeTestRule.onNodeWithText(specificErrorMessage).assertIsDisplayed() // Spesifik mesaj
        composeTestRule.onNodeWithText(retryButtonText).assertIsDisplayed() // Tekrar Dene butonu
        // Normal quiz içeriğinin OLMADIĞINI kontrol et
        composeTestRule.onNodeWithText(question1.question).assertDoesNotExist()
        composeTestRule.onNodeWithText(submitButtonText).assertDoesNotExist()
        println("TEST: Error view assertions passed.")
    }

    @Test
    fun emptyQuestionsReturned_displaysEmptyView() {
        // Arrange: Repo'nun boş soru listesi döndürmesini sağla
        configurableRepo.setQuestionsForCategory(categoryToTest, emptyList())
        println("TEST: Configured repository to return empty questions list.")

        // Act: Kategoriye tıkla ve Quiz Ekranına gitmeye çalış
        println("TEST: Waiting for category '$categoryToTest'...")
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            composeTestRule.onAllNodesWithText(categoryToTest, substring = true, ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
        }
        println("TEST: Category node found. Performing click...")
        composeTestRule.onNodeWithText(categoryToTest, substring = true, ignoreCase = true)
            .performClick()
        println("TEST: Clicked category '$categoryToTest'. Waiting for empty view...")

        // Boş liste metninin görünmesini bekle
        try {
            composeTestRule.waitUntil(timeoutMillis = 10000) {
                composeTestRule.onAllNodesWithText(emptyViewText).fetchSemanticsNodes().isNotEmpty()
            }
            println("TEST: Empty view text found. Asserting empty view...")
        } catch (e: ComposeTimeoutException) {
            println("TEST ERROR: Empty view text '$emptyViewText' not found!")
            composeTestRule.onRoot().printToLog("EmptyViewFail")
            throw e
        }

        // Assert: Boş liste görünümü gösterilir, quiz içeriği gösterilmez
        composeTestRule.onNodeWithText(emptyViewText).assertIsDisplayed()
        composeTestRule.onNodeWithText(backButtonText).assertIsDisplayed() // Geri butonu (EmptyView'da olmalı)
        // Normal quiz içeriğinin OLMADIĞINI kontrol et
        composeTestRule.onNodeWithText(question1.question).assertDoesNotExist()
        composeTestRule.onNodeWithText(submitButtonText).assertDoesNotExist()
        println("TEST: Empty view assertions passed.")
    }

    @Test
    fun errorView_clickingRetry_loadsQuestionsSuccessfully() {
        // Arrange 1: Repo'nun önce hata vermesini sağla
        val specificErrorMessage = "Initial network error!"
        configurableRepo.setQuestionsError(categoryToTest, Exception(specificErrorMessage))
        println("TEST: Configured repository to return error initially.")

        // Act 1: Hata ekranını göster
        println("TEST: Navigating to trigger error view...")
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            composeTestRule.onAllNodesWithText(categoryToTest, substring = true, ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(categoryToTest, substring = true, ignoreCase = true).performClick()
        composeTestRule.waitUntil(timeoutMillis = 10000) { // Hata mesajını bekle
            composeTestRule.onAllNodesWithText(specificErrorMessage).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(retryButtonText).assertIsDisplayed() // Retry butonu görünüyor mu?
        println("TEST: Error view displayed. Configuring repo for success...")

        // Arrange 2: Repo'yu şimdi BAŞARILI soru döndürecek şekilde ayarla
        configurableRepo.setQuestionsForCategory(categoryToTest, listOf(question1))
        println("TEST: Configured repository to return success now.")

        // Act 2: "Tekrar Dene" butonuna tıkla
        composeTestRule.onNodeWithText(retryButtonText).performClick()
        println("TEST: Clicked Retry. Waiting for question...")

        // Assert: Hata görünümü kaybolur ve ilk soru yüklenir
        try {
            composeTestRule.waitUntil(timeoutMillis = 10000) { // İlk soruyu bekle
                composeTestRule.onAllNodesWithText(question1.question).fetchSemanticsNodes().isNotEmpty()
            }
            println("TEST: Question loaded after retry. Asserting...")
        } catch (e: ComposeTimeoutException) {
            println("TEST ERROR: Question did not load after clicking Retry!")
            composeTestRule.onRoot().printToLog("RetryFail")
            throw e
        }

        composeTestRule.onNodeWithText(question1.question).assertIsDisplayed()
        composeTestRule.onNodeWithText(submitButtonText).assertIsDisplayed().assertIsNotEnabled() // Quiz başladı
        // Hata görünümünün kaybolduğunu kontrol et
        composeTestRule.onNodeWithText(errorViewTitle).assertDoesNotExist()
        composeTestRule.onNodeWithText(specificErrorMessage).assertDoesNotExist()
        composeTestRule.onNodeWithText(retryButtonText).assertDoesNotExist()
        println("TEST: Retry button assertions passed.")
    }

    @Test
    fun emptyView_clickingBack_navigatesBackToCategoryScreen() {
        // Arrange: Repo'nun boş soru listesi döndürmesini sağla ve boş ekranı göster
        configurableRepo.setQuestionsForCategory(categoryToTest, emptyList())
        println("TEST: Configured repository to return empty questions list.")
        println("TEST: Navigating to trigger empty view...")
        composeTestRule.waitUntil(timeoutMillis = 15000) {
            composeTestRule.onAllNodesWithText(categoryToTest, substring = true, ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(categoryToTest, substring = true, ignoreCase = true).performClick()
        composeTestRule.waitUntil(timeoutMillis = 10000) { // Boş metni bekle
            composeTestRule.onAllNodesWithText(emptyViewText).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(backButtonText).assertIsDisplayed() // Geri butonu görünüyor mu?
        println("TEST: Empty view displayed. Clicking Back button...")

        // Act: "Geri" butonuna tıkla (EmptyView içindeki)
        composeTestRule.onNodeWithText(backButtonText).performClick()
        println("TEST: Clicked Back. Waiting for Category Screen...")

        // Assert: Boş liste görünümü kaybolur ve Kategori Ekranına dönülür
        try {
            println("TEST: Waiting for category '$categoryToTest' to reappear after back...")
            composeTestRule.waitUntil(timeoutMillis = 10000) {
                composeTestRule.onAllNodesWithText(categoryToTest, substring = true, ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
            }
            println("TEST: Category screen reappeared.")
        } catch (e: ComposeTimeoutException) {
            println("TEST ERROR: Category text did not reappear after clicking Back from empty view!")
            composeTestRule.onRoot().printToLog("EmptyBackFail")
            throw e
        }

        // Kategori ekranı öğesini doğrula
        composeTestRule.onNodeWithText(categoryToTest, substring = true, ignoreCase = true).assertIsDisplayed()

        // Boş liste görünümünün kaybolduğunu doğrula
        composeTestRule.onNodeWithText(emptyViewText).assertDoesNotExist()
        composeTestRule.onNodeWithText(backButtonText).assertDoesNotExist() // EmptyView'daki buton gitti
        println("TEST: Empty view Back button assertions passed.")
    }
}

// MainDispatcherRule sınıfı buraya eklenebilir (gerekirse)
// @ExperimentalCoroutinesApi
// class MainDispatcherRule(...) { ... }