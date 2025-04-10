package com.technovix.quiznova.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.technovix.quiznova.data.local.entity.QuestionEntity
import com.technovix.quiznova.data.repository.QuizRepository
import com.technovix.quiznova.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizUiState(
    val questions: Resource<List<QuestionEntity>> = Resource.Loading(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswer: String? = null,
    val score: Int = 0,
    val isAnswerSubmitted: Boolean = false,
    val isQuizFinished: Boolean = false,
    val userAnswers: List<Pair<QuestionEntity, String?>> = emptyList()
)

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: QuizRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val categoryId: Int = savedStateHandle.get<Int>("categoryId") ?: 0
    private val categoryName: String = savedStateHandle.get<String>("categoryName") ?: "Unknown"
    private val questionAmount = 10

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            _uiState.update { it.copy(questions = Resource.Loading()) }

            repository.getQuestions(categoryId, categoryName, questionAmount)
                .collect { result ->
                    _uiState.update { currentState ->
                        currentState.copy(questions = result)
                    }
                }
        }
    }

    fun onAnswerSelected(answer: String) {
        if (!_uiState.value.isAnswerSubmitted) {
            _uiState.update { it.copy(selectedAnswer = answer) }
        }
    }

    fun submitAnswer() {
        val currentState = _uiState.value
        val questions = (currentState.questions as? Resource.Success)?.data ?: return
        if (currentState.selectedAnswer == null || currentState.isAnswerSubmitted) return

        val currentQuestion = questions.getOrNull(currentState.currentQuestionIndex) ?: return
        val isCorrect = currentState.selectedAnswer == currentQuestion.correctAnswer

        _uiState.update {
            it.copy(
                score = if (isCorrect) it.score + 1 else it.score,
                isAnswerSubmitted = true,
                userAnswers = it.userAnswers + (currentQuestion to it.selectedAnswer)
            )
        }
    }

    fun nextQuestion() {
        val currentState = _uiState.value
        val questions = (currentState.questions as? Resource.Success)?.data ?: return

        if (currentState.currentQuestionIndex < questions.size - 1) {
            _uiState.update {
                it.copy(
                    currentQuestionIndex = it.currentQuestionIndex + 1,
                    selectedAnswer = null,
                    isAnswerSubmitted = false
                )
            }
        } else {
            _uiState.update { it.copy(isQuizFinished = true) }
        }
    }

    fun restartQuiz() {
        _uiState.value = QuizUiState()
        loadQuestions()
    }

    fun retryLoadQuestions() {
        loadQuestions()
    }
}