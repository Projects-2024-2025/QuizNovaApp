package com.mindostech.quiznova.ui.viewmodel

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.mindostech.quiznova.data.local.entity.QuestionEntity
import com.mindostech.quiznova.data.repository.QuizRepository
import com.mindostech.quiznova.util.HtmlDecoder
import com.mindostech.quiznova.util.Resource
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@ExperimentalCoroutinesApi
class QuizViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    @RelaxedMockK
    lateinit var repository: QuizRepository

    @RelaxedMockK
    lateinit var htmlDecoder: HtmlDecoder

    @RelaxedMockK
    lateinit var mockApplication: Application
    private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var viewModel: QuizViewModel

    private val categoryId = 9
    private val categoryName = "General Knowledge"
    private val questionAmount = 10
    private val testInterstitialAdUnitId = "test_ad_unit_id"

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
    }

    private fun createViewModel(initialState: Map<String, Any?> = mapOf()): QuizViewModel {
        savedStateHandle = SavedStateHandle(initialState)
        savedStateHandle[QuizViewModel.CATEGORY_ID_KEY] = categoryId
        savedStateHandle[QuizViewModel.CATEGORY_NAME_KEY] = categoryName

        every { htmlDecoder.decode(any()) } answers { firstArg() }

        return QuizViewModel(
            repository = repository,
            savedStateHandle = savedStateHandle,
            htmlDecoder = htmlDecoder,
            application = mockApplication,
            interstitialAdUnitId = testInterstitialAdUnitId
        )
    }

    private fun createFakeQuestionEntity(
        id: Int,
        question: String = "Question $id HTML?",
        correct: String = "Correct$id",
        incorrect: List<String> = listOf("WrongA$id", "WrongB$id"),
        all: List<String>? = null
    ): QuestionEntity {
        val allAnswers = all ?: (incorrect + correct).shuffled()
        return QuestionEntity(
            id = id, category = categoryName, type = "multiple", difficulty = "easy",
            question = question, correctAnswer = correct, incorrectAnswers = incorrect,
            allAnswers = allAnswers, fetchedTimestamp = System.currentTimeMillis()
        )
    }

    @Test
    fun `init - SavedStateHandle boşsa başlangıç state'i doğru ayarlanır ve loadQuestions çağrılır`() = runTest {
        // Arrange
        val loadingFlow = flowOf(Resource.Loading<List<QuestionEntity>>())
        coEvery { repository.getQuestions(categoryId, categoryName, questionAmount) } returns loadingFlow

        // Act
        viewModel = createViewModel()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertThat(state.currentQuestionIndex).isEqualTo(0)
        assertThat(state.score).isEqualTo(0)
        assertThat(state.isAnswerSubmitted).isFalse()
        assertThat(state.isQuizFinished).isFalse()
        assertThat(state.userAnswers).isEmpty()
        assertThat(state.questions).isInstanceOf(Resource.Loading::class.java)

        // Assert
        coVerify(exactly = 1) { repository.getQuestions(categoryId, categoryName, questionAmount) }
    }

    @Test
    fun `init - SavedStateHandle doluysa state doğru yüklenir ve loadQuestions çağrılır`() = runTest {
        // Arrange
        val initialState = mapOf(
            QuizViewModel.CURRENT_INDEX_KEY to 3,
            QuizViewModel.SCORE_KEY to 5,
            QuizViewModel.IS_SUBMITTED_KEY to true,
            QuizViewModel.IS_FINISHED_KEY to false
        )
        val loadingFlow = flowOf(Resource.Loading<List<QuestionEntity>>())
        coEvery { repository.getQuestions(categoryId, categoryName, questionAmount) } returns loadingFlow

        // Act
        viewModel = createViewModel(initialState)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertThat(state.currentQuestionIndex).isEqualTo(3)
        assertThat(state.score).isEqualTo(5)
        assertThat(state.isAnswerSubmitted).isTrue()
        assertThat(state.isQuizFinished).isFalse()
        assertThat(state.questions).isInstanceOf(Resource.Loading::class.java)

        // Assert
        coVerify(exactly = 1) { repository.getQuestions(categoryId, categoryName, questionAmount) }
    }

    @Test
    fun `init - Quiz bitmişse (isFinished=true) loadQuestions çağrılmaz`() = runTest {
        // Arrange
        val initialState = mapOf(QuizViewModel.IS_FINISHED_KEY to true)
        clearMocks(repository)

        // Act
        viewModel = createViewModel(initialState)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        coVerify(exactly = 0) { repository.getQuestions(any(), any(), any()) }
        val state = viewModel.uiState.value
        assertThat(state.isQuizFinished).isTrue()
        assertThat(state.questions).isInstanceOf(Resource.Loading::class.java)
    }


    @Test
    fun `loadQuestions - Repository Success döndürürse state Success ve veri olur`() = runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        val question1Html = "Q1 &?"
        val repoQuestions = listOf(createFakeQuestionEntity(1, question = question1Html))
        val successFlow = flowOf(Resource.Success(repoQuestions))
        coEvery { repository.getQuestions(categoryId, categoryName, questionAmount) } returns successFlow

        // Act
        viewModel = createViewModel()

        // Assert
        assertThat(viewModel.uiState.value.questions).isInstanceOf(Resource.Loading::class.java)

        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val finalState = viewModel.uiState.value
        assertThat(finalState.questions).isInstanceOf(Resource.Success::class.java)
        val dataInState = (finalState.questions as Resource.Success).data
        assertThat(dataInState).hasSize(1)
        assertThat(dataInState?.get(0)?.question).isEqualTo(question1Html)
    }

    @Test
    fun `loadQuestions - Repository Error döndürürse state Error olur`() = runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        val errorMessage = "Yükleme Hatası"
        val errorFlow = flowOf(Resource.Error<List<QuestionEntity>>(errorMessage))
        coEvery { repository.getQuestions(categoryId, categoryName, questionAmount) } returns errorFlow

        // Act
        viewModel = createViewModel()
        assertThat(viewModel.uiState.value.questions).isInstanceOf(Resource.Loading::class.java)

        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val finalState = viewModel.uiState.value
        assertThat(finalState.questions).isInstanceOf(Resource.Error::class.java)
        assertThat((finalState.questions as Resource.Error).message).isEqualTo(errorMessage)
    }


    @Test
    fun `onAnswerSelected - Cevap seçildiğinde state güncellenir`() = runTest {
        // Arrange
        coEvery { repository.getQuestions(any(), any(), any()) } returns flowOf()
        viewModel = createViewModel()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val selectedAnswer = "Test Answer"

        // Act
        viewModel.onAnswerSelected(selectedAnswer)

        // Assert
        assertThat(viewModel.uiState.value.selectedAnswer).isEqualTo(selectedAnswer)
    }

    @Test
    fun `onAnswerSelected - Cevap gönderilmişse state güncellenmez`() = runTest {
        // Arrange
        val question1 = createFakeQuestionEntity(1)
        coEvery { repository.getQuestions(any(), any(), any()) } returns flowOf(Resource.Success(listOf(question1)))
        viewModel = createViewModel()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        val firstAnswer = "İlk Cevap"
        viewModel.onAnswerSelected(firstAnswer)
        viewModel.submitAnswer()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Act
        viewModel.onAnswerSelected("Yeni Cevap")

        // Assert
        val state = viewModel.uiState.value
        assertThat(state.selectedAnswer).isEqualTo(firstAnswer)
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
        viewModel.onAnswerSelected(correctHtml)
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

        viewModel.onAnswerSelected("Cevap")
        viewModel.submitAnswer()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Act
        viewModel.nextQuestion()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertThat(state.isQuizFinished).isTrue()
        assertThat(state.currentQuestionIndex).isEqualTo(0)
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
        clearMocks(repository)

        viewModel = createViewModel(initialState)
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        coEvery { repository.getQuestions(categoryId, categoryName, questionAmount) } returns flowOf(Resource.Loading())

        // Act
        viewModel.restartQuiz()
        mainDispatcherRule.testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertThat(state.currentQuestionIndex).isEqualTo(0)
        assertThat(state.score).isEqualTo(0)
        assertThat(state.isAnswerSubmitted).isFalse()
        assertThat(state.isQuizFinished).isFalse()
        assertThat(state.userAnswers).isEmpty()
        assertThat(state.questions).isInstanceOf(Resource.Loading::class.java)

        // Assert
        assertThat(savedStateHandle.contains(QuizViewModel.CURRENT_INDEX_KEY)).isFalse()
        assertThat(savedStateHandle.contains(QuizViewModel.SCORE_KEY)).isFalse()
        assertThat(savedStateHandle.contains(QuizViewModel.IS_SUBMITTED_KEY)).isFalse()
        assertThat(savedStateHandle.contains(QuizViewModel.IS_FINISHED_KEY)).isFalse()

        // Assert
        coVerify(exactly = 1) { repository.getQuestions(categoryId, categoryName, questionAmount) }
    }
}


@ExperimentalCoroutinesApi
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}