package com.technovix.quiznova.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test // Flow testi için Turbine
import com.google.common.truth.Truth.assertThat // Assertion için Truth
import com.technovix.quiznova.data.local.entity.QuestionEntity
import com.technovix.quiznova.data.repository.QuizRepository
import com.technovix.quiznova.util.HtmlDecoder
import com.technovix.quiznova.util.Resource
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher // JUnit 4 için TestWatcher import'u
import org.junit.runner.Description // JUnit 4 için Description import'u

@ExperimentalCoroutinesApi
class QuizViewModelTest {

    // Coroutine Dispatcher'larını test için yöneten kural
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher()) // StandardDispatcher kullanıyoruz

    // Mock nesneler
    @MockK private lateinit var repository: QuizRepository
    @MockK private lateinit var htmlDecoder: HtmlDecoder
    private lateinit var savedStateHandle: SavedStateHandle

    // Test edilecek ViewModel
    private lateinit var viewModel: QuizViewModel

    // Test verileri
    private val categoryId = 9
    private val categoryName = "General Knowledge"
    private val questionAmount = 10

    // --- Yardımcı Fonksiyonlar ---
    private fun createViewModel(initialState: Map<String, Any?> = mapOf()): QuizViewModel {
        savedStateHandle = SavedStateHandle(initialState)
        // Navigasyondan gelmesi beklenen argümanları handle'a ekle
        savedStateHandle[QuizViewModel.CATEGORY_ID_KEY] = categoryId
        savedStateHandle[QuizViewModel.CATEGORY_NAME_KEY] = categoryName
        // Testte HtmlDecoder'ın basitçe input'u döndürmesini sağla
        // Bu, ViewModel'deki decode fonksiyonunun hata vermesini engeller
        every { htmlDecoder.decode(any()) } answers { firstArg() }
        return QuizViewModel(repository, savedStateHandle, htmlDecoder)
    }

    private fun createFakeQuestionEntity(
        id: Int,
        question: String = "Question $id HTML?", // Ham HTML içerebilir
        correct: String = "Correct$id",
        incorrect: List<String> = listOf("WrongA$id", "WrongB$id"),
        all: List<String>? = null
    ): QuestionEntity {
        // Test verisi artık HAM HTML içerebilir, çünkü decode işlemi ViewModel'de
        val allAnswers = all ?: (incorrect + correct).shuffled()
        return QuestionEntity(
            id = id, category = categoryName, type = "multiple", difficulty = "easy",
            question = question, correctAnswer = correct, incorrectAnswers = incorrect,
            allAnswers = allAnswers, fetchedTimestamp = System.currentTimeMillis()
        )
    }
    // --- Yardımcı Fonksiyonlar Bitti ---

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        // Her test kendi mock davranışını tanımlayacağı için burada genel mock yok.
    }

    // --- init Testleri ---

    @Test
    fun `init - SavedStateHandle boşsa başlangıç state'i doğru ayarlanır ve loadQuestions çağrılır`() = runTest {
        // Arrange
        // ViewModel oluşturulduğunda init bloğu çalışır ve loadQuestions'ı çağırır.
        // loadQuestions'ın hata vermemesi için repository mock'unu ayarlayalım.
        val loadingFlow = flowOf(Resource.Loading<List<QuestionEntity>>())
        coEvery { repository.getQuestions(categoryId, categoryName, questionAmount) } returns loadingFlow

        // Act
        viewModel = createViewModel()
        // init içindeki launch işleminin tamamlanmasını bekle
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Assert - Başlangıç state'ini kontrol et
        val state = viewModel.uiState.value
        assertThat(state.currentQuestionIndex).isEqualTo(0)
        assertThat(state.score).isEqualTo(0)
        assertThat(state.isAnswerSubmitted).isFalse()
        assertThat(state.isQuizFinished).isFalse()
        assertThat(state.userAnswers).isEmpty()
        assertThat(state.questions).isInstanceOf(Resource.Loading::class.java) // init sonrası Loading

        // Assert - loadQuestions çağrıldığını doğrula
        coVerify(exactly = 1) { repository.getQuestions(categoryId, categoryName, questionAmount) }
    }

    @Test
    fun `init - SavedStateHandle doluysa state doğru yüklenir ve loadQuestions çağrılır`() = runTest {
        // Arrange
        val initialState = mapOf(
            QuizViewModel.CURRENT_INDEX_KEY to 3,
            QuizViewModel.SCORE_KEY to 5,
            QuizViewModel.IS_SUBMITTED_KEY to true,
            QuizViewModel.IS_FINISHED_KEY to false // Quiz bitmemiş
        )
        val loadingFlow = flowOf(Resource.Loading<List<QuestionEntity>>())
        coEvery { repository.getQuestions(categoryId, categoryName, questionAmount) } returns loadingFlow

        // Act
        viewModel = createViewModel(initialState)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Assert - Yüklenen state'i kontrol et
        val state = viewModel.uiState.value
        assertThat(state.currentQuestionIndex).isEqualTo(3)
        assertThat(state.score).isEqualTo(5)
        assertThat(state.isAnswerSubmitted).isTrue() // Yüklendi
        assertThat(state.isQuizFinished).isFalse()
        assertThat(state.questions).isInstanceOf(Resource.Loading::class.java) // loadQuestions çağrıldı

        // Assert - loadQuestions çağrıldığını doğrula
        coVerify(exactly = 1) { repository.getQuestions(categoryId, categoryName, questionAmount) }
    }

    @Test
    fun `init - Quiz bitmişse (isFinished=true) loadQuestions çağrılmaz`() = runTest {
        // Arrange
        val initialState = mapOf(QuizViewModel.IS_FINISHED_KEY to true)
        // loadQuestions çağrılmayacağı için repository mocklamasına gerek yok bu senaryoda
        clearMocks(repository) // Emin olmak için temizleyelim

        // Act
        viewModel = createViewModel(initialState)
        // init içindeki kontrol anlık olduğu için advanceUntilIdle gerekmeyebilir, ama kalsa da olur.
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Assert - loadQuestions çağrılmadı
        coVerify(exactly = 0) { repository.getQuestions(any(), any(), any()) }
        val state = viewModel.uiState.value
        assertThat(state.isQuizFinished).isTrue()
        // questions alanı başlangıç değeri olan Loading'de kalır
        assertThat(state.questions).isInstanceOf(Resource.Loading::class.java)
    }

    // --- loadQuestions Testleri ---

    @Test
    fun `loadQuestions - Repository Success döndürürse state Success ve veri olur`() = runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        val question1Html = "Q1 &?"
        val repoQuestions = listOf(createFakeQuestionEntity(1, question = question1Html))
        // Sadece Success emit eden bir flow hazırlayalım (Loading'i init'te zaten test ediyoruz)
        val successFlow = flowOf(Resource.Success(repoQuestions))
        coEvery { repository.getQuestions(categoryId, categoryName, questionAmount) } returns successFlow

        // Act
        viewModel = createViewModel() // init çalışır, loadQuestions tetiklenir

        // Assert - Başlangıç state'ini kontrol et
        assertThat(viewModel.uiState.value.questions).isInstanceOf(Resource.Loading::class.java)

        // loadQuestions içindeki coroutine'in çalışıp bitmesini sağla
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Assert - Son state'i kontrol et
        val finalState = viewModel.uiState.value
        assertThat(finalState.questions).isInstanceOf(Resource.Success::class.java)
        val dataInState = (finalState.questions as Resource.Success).data
        assertThat(dataInState).hasSize(1)
        assertThat(dataInState?.get(0)?.question).isEqualTo(question1Html) // Ham HTML kontrolü
    }

    @Test
    fun `loadQuestions - Repository Error döndürürse state Error olur`() = runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        val errorMessage = "Yükleme Hatası"
        // Sadece Error emit eden bir flow
        val errorFlow = flowOf(Resource.Error<List<QuestionEntity>>(errorMessage))
        coEvery { repository.getQuestions(categoryId, categoryName, questionAmount) } returns errorFlow

        // Act
        viewModel = createViewModel()
        assertThat(viewModel.uiState.value.questions).isInstanceOf(Resource.Loading::class.java) // Başlangıç kontrolü

        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle() // Coroutine bitsin

        // Assert - Son state
        val finalState = viewModel.uiState.value
        assertThat(finalState.questions).isInstanceOf(Resource.Error::class.java)
        assertThat((finalState.questions as Resource.Error).message).isEqualTo(errorMessage)
    }

    // --- Kullanıcı Etkileşim Testleri ---

    @Test
    fun `onAnswerSelected - Cevap seçildiğinde state güncellenir`() = runTest {
        // Arrange
        coEvery { repository.getQuestions(any(), any(), any()) } returns flowOf() // init'te hata vermesin diye boş flow
        viewModel = createViewModel()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle() // init bitsin

        val selectedAnswer = "Test Answer"

        // Act
        viewModel.onAnswerSelected(selectedAnswer)
        // State güncellemesi anlık olmalı, advanceUntilIdle'a gerek yok

        // Assert
        assertThat(viewModel.uiState.value.selectedAnswer).isEqualTo(selectedAnswer)
    }

    @Test
    fun `onAnswerSelected - Cevap gönderilmişse state güncellenmez`() = runTest {
        // Arrange
        val question1 = createFakeQuestionEntity(1)
        coEvery { repository.getQuestions(any(), any(), any()) } returns flowOf(Resource.Success(listOf(question1)))
        viewModel = createViewModel()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle() // init/loadQuestions bitsin

        // İlk cevabı seç ve GÖNDER
        val firstAnswer = "İlk Cevap"
        viewModel.onAnswerSelected(firstAnswer)
        viewModel.submitAnswer()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle() // submitAnswer bitsin

        // Act: Gönderilmişken yeni cevap seçmeye çalış
        viewModel.onAnswerSelected("Yeni Cevap")

        // Assert
        val state = viewModel.uiState.value
        assertThat(state.selectedAnswer).isEqualTo(firstAnswer) // Değişmemeli
        assertThat(state.isAnswerSubmitted).isTrue()
    }

    @Test
    fun `submitAnswer - Doğru cevapta skor artar, state güncellenir, SavedState kaydedilir`() = runTest {
        // Arrange
        val correctHtml = "Doğru Cevap HTML"
        val question1 = createFakeQuestionEntity(1, correct = correctHtml)
        coEvery { repository.getQuestions(any(), any(), any()) } returns flowOf(Resource.Success(listOf(question1)))
        viewModel = createViewModel()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Act
        viewModel.onAnswerSelected(correctHtml) // Ham HTML ile seç
        viewModel.submitAnswer()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertThat(state.score).isEqualTo(1)
        assertThat(state.isAnswerSubmitted).isTrue()
        assertThat(state.userAnswers).hasSize(1)
        assertThat(state.userAnswers[0].first.id).isEqualTo(question1.id)
        assertThat(state.userAnswers[0].second).isEqualTo(correctHtml)
        assertThat(savedStateHandle.get<Int>(QuizViewModel.SCORE_KEY)).isEqualTo(1)
        assertThat(savedStateHandle.get<Boolean>(QuizViewModel.IS_SUBMITTED_KEY)).isTrue()
    }

    @Test
    fun `submitAnswer - Yanlış cevapta skor artmaz, state güncellenir, SavedState kaydedilir`() = runTest {
        // Arrange
        val correctHtml = "Doğru Cevap HTML"
        val question1 = createFakeQuestionEntity(1, correct = correctHtml)
        coEvery { repository.getQuestions(any(), any(), any()) } returns flowOf(Resource.Success(listOf(question1)))
        viewModel = createViewModel()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Act
        val wrongAnswer = "Yanlış Cevap"
        viewModel.onAnswerSelected(wrongAnswer)
        viewModel.submitAnswer()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertThat(state.score).isEqualTo(0)
        assertThat(state.isAnswerSubmitted).isTrue()
        assertThat(state.userAnswers).hasSize(1)
        assertThat(state.userAnswers[0].second).isEqualTo(wrongAnswer)
        assertThat(savedStateHandle.get<Int>(QuizViewModel.SCORE_KEY)).isEqualTo(0)
        assertThat(savedStateHandle.get<Boolean>(QuizViewModel.IS_SUBMITTED_KEY)).isTrue()
    }

    @Test
    fun `nextQuestion - Son soruda değilse index artar, state sıfırlanır, SavedState kaydedilir`() = runTest {
        // Arrange
        val question1 = createFakeQuestionEntity(1)
        val question2 = createFakeQuestionEntity(2)
        coEvery { repository.getQuestions(any(), any(), any()) } returns flowOf(Resource.Success(listOf(question1, question2)))
        viewModel = createViewModel()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // İlk soruyu gönder
        viewModel.onAnswerSelected("Cevap")
        viewModel.submitAnswer()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Act
        viewModel.nextQuestion()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertThat(state.currentQuestionIndex).isEqualTo(1)
        assertThat(state.selectedAnswer).isNull()
        assertThat(state.isAnswerSubmitted).isFalse()
        assertThat(state.isQuizFinished).isFalse()
        assertThat(savedStateHandle.get<Int>(QuizViewModel.CURRENT_INDEX_KEY)).isEqualTo(1)
        assertThat(savedStateHandle.get<Boolean>(QuizViewModel.IS_SUBMITTED_KEY)).isFalse()
    }

    @Test
    fun `nextQuestion - Son sorudaysa quiz biter, SavedState kaydedilir`() = runTest {
        // Arrange
        val question1 = createFakeQuestionEntity(1)
        coEvery { repository.getQuestions(any(), any(), any()) } returns flowOf(Resource.Success(listOf(question1)))
        viewModel = createViewModel()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Soruyu gönder
        viewModel.onAnswerSelected("Cevap")
        viewModel.submitAnswer()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Act
        viewModel.nextQuestion()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertThat(state.isQuizFinished).isTrue()
        assertThat(state.currentQuestionIndex).isEqualTo(0) // Index değişmedi
        assertThat(state.isAnswerSubmitted).isFalse()
        assertThat(savedStateHandle.get<Boolean>(QuizViewModel.IS_FINISHED_KEY)).isTrue()
        assertThat(savedStateHandle.get<Boolean>(QuizViewModel.IS_SUBMITTED_KEY)).isFalse()
    }


    @Test
    fun `restartQuiz - State sıfırlanır, SavedState temizlenir, loadQuestions çağrılır`() = runTest {
        // Arrange
        val initialState = mapOf(
            QuizViewModel.CURRENT_INDEX_KEY to 5,
            QuizViewModel.SCORE_KEY to 3,
            QuizViewModel.IS_FINISHED_KEY to true
        )
        clearMocks(repository) // Mockları temizle

        viewModel = createViewModel(initialState) // init çalışır ama loadQuestions çağırmaz
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // restartQuiz içinde çağrılacak loadQuestions'ı mockla
        coEvery { repository.getQuestions(categoryId, categoryName, questionAmount) } returns flowOf(Resource.Loading())

        // Act
        viewModel.restartQuiz()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle() // restartQuiz bitsin

        // Assert - State sıfırlandı mı?
        val state = viewModel.uiState.value
        assertThat(state.currentQuestionIndex).isEqualTo(0)
        assertThat(state.score).isEqualTo(0)
        assertThat(state.isAnswerSubmitted).isFalse()
        assertThat(state.isQuizFinished).isFalse()
        assertThat(state.userAnswers).isEmpty()
        assertThat(state.questions).isInstanceOf(Resource.Loading::class.java)

        // Assert - SavedStateHandle temizlendi mi?
        assertThat(savedStateHandle.contains(QuizViewModel.CURRENT_INDEX_KEY)).isFalse()
        assertThat(savedStateHandle.contains(QuizViewModel.SCORE_KEY)).isFalse()
        assertThat(savedStateHandle.contains(QuizViewModel.IS_SUBMITTED_KEY)).isFalse()
        assertThat(savedStateHandle.contains(QuizViewModel.IS_FINISHED_KEY)).isFalse()

        // Assert - loadQuestions restartQuiz'da çağrıldı mı?
        coVerify(exactly = 1) { repository.getQuestions(categoryId, categoryName, questionAmount) }
    }

    @After
    fun tearDown() {
        // Dispatchers.resetMain() // MainDispatcherRule bunu zaten yapıyor
    }
}


// Test Dispatcher'larını yönetmek için yardımcı kural
@ExperimentalCoroutinesApi
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher() // StandardDispatcher kullanıyoruz
) : TestWatcher() {
    override fun starting(description: Description) { // JUnit 4 Description
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) { // JUnit 4 Description
        Dispatchers.resetMain()
    }
}