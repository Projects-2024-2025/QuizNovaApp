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

// Senior Dev: Quiz ekranının durumunu (state) yönetmek için bir data class.
// Veri arttıkça bunu kullanmak daha organize olur.
data class QuizUiState(
    val questions: Resource<List<QuestionEntity>> = Resource.Loading(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswer: String? = null, // Kullanıcının seçtiği cevap
    val score: Int = 0,
    val isAnswerSubmitted: Boolean = false, // Cevap gönderildi mi? (Doğru/yanlışı göstermek için)
    val isQuizFinished: Boolean = false,
    val userAnswers: List<Pair<QuestionEntity, String?>> = emptyList() // Kullanıcının tüm cevapları (sonuç ekranı için)
)

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: QuizRepository,
    savedStateHandle: SavedStateHandle // Navigation argümanlarını almak için
) : ViewModel() {

    // Senior Dev: Navigasyondan gelen argümanları SavedStateHandle ile alıyoruz.
    // Hilt bunu otomatik olarak enjekte eder.
    private val categoryId: Int = savedStateHandle.get<Int>("categoryId") ?: 0
    private val categoryName: String = savedStateHandle.get<String>("categoryName") ?: "Unknown"
    private val questionAmount = 10 // Kaç soru çekileceği

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    init {
        loadQuestions()
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            // Başlangıç durumunu Loading yap
            _uiState.update { it.copy(questions = Resource.Loading()) }

            repository.getQuestions(categoryId, categoryName, questionAmount)
                .collect { result ->
                    _uiState.update { currentState ->
                        currentState.copy(questions = result)
                    }
                }
        }
    }

    // Kullanıcı bir cevap seçtiğinde çağrılır
    fun onAnswerSelected(answer: String) {
        if (!_uiState.value.isAnswerSubmitted) { // Eğer cevap henüz gönderilmediyse seçime izin ver
            _uiState.update { it.copy(selectedAnswer = answer) }
        }
    }

    // Kullanıcı cevabını onayladığında/gönderdiğinde çağrılır
    fun submitAnswer() {
        val currentState = _uiState.value
        val questions = (currentState.questions as? Resource.Success)?.data ?: return // Sorular yoksa çık
        if (currentState.selectedAnswer == null || currentState.isAnswerSubmitted) return // Seçim yoksa veya zaten gönderilmişse çık

        val currentQuestion = questions.getOrNull(currentState.currentQuestionIndex) ?: return
        val isCorrect = currentState.selectedAnswer == currentQuestion.correctAnswer

        // Skoru güncelle ve cevap gönderildi durumunu işaretle
        _uiState.update {
            it.copy(
                score = if (isCorrect) it.score + 1 else it.score,
                isAnswerSubmitted = true,
                // Cevabı ve soruyu kaydet
                userAnswers = it.userAnswers + (currentQuestion to it.selectedAnswer)
            )
        }
    }

    // Sonraki soruya geçmek için
    fun nextQuestion() {
        val currentState = _uiState.value
        val questions = (currentState.questions as? Resource.Success)?.data ?: return

        if (currentState.currentQuestionIndex < questions.size - 1) {
            // Sonraki soruya geç, seçimi ve gönderildi durumunu sıfırla
            _uiState.update {
                it.copy(
                    currentQuestionIndex = it.currentQuestionIndex + 1,
                    selectedAnswer = null,
                    isAnswerSubmitted = false
                )
            }
        } else {
            // Quiz bitti
            _uiState.update { it.copy(isQuizFinished = true) }
        }
    }

    fun restartQuiz() {
        _uiState.value = QuizUiState() // State'i sıfırla
        loadQuestions() // Soruları tekrar yükle
    }

    fun retryLoadQuestions() {
        loadQuestions()
    }
}