package com.technovix.quiznova.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.text.Html
import androidx.core.text.HtmlCompat
import com.technovix.quiznova.data.local.entity.QuestionEntity
import com.technovix.quiznova.data.repository.QuizRepository
import com.technovix.quiznova.util.HtmlDecoder
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
    private  val savedStateHandle: SavedStateHandle,
    private val htmlDecoder: HtmlDecoder
) : ViewModel() {

    companion object {
        const val CATEGORY_ID_KEY = "categoryId"
        const val CATEGORY_NAME_KEY = "categoryName"
        const val CURRENT_INDEX_KEY = "currentQuestionIndex"
        const val SCORE_KEY = "score"
        const val IS_SUBMITTED_KEY = "isAnswerSubmitted"
        const val IS_FINISHED_KEY = "isQuizFinished"
        // userAnswers'ı kaydetmek biraz daha karmaşık, şimdilik diğerlerini yapalım
        // private const val USER_ANSWERS_KEY = "userAnswers"
    }

    private val categoryId: Int = savedStateHandle.get<Int>(CATEGORY_ID_KEY) ?: 0
    private val categoryName: String = savedStateHandle.get<String>(CATEGORY_NAME_KEY) ?: "Unknown"
    private val questionAmount = 10

    //Durumu başlatma (SavedState'den geri yükleme)
    private val _uiState = MutableStateFlow(QuizUiState(
        currentQuestionIndex = savedStateHandle.get<Int>(CURRENT_INDEX_KEY) ?: 0,
        score = savedStateHandle.get<Int>(SCORE_KEY) ?: 0,
        // isAnswerSubmitted'ı direkt yüklememek daha iyi olabilir, çünkü soru değişince sıfırlanıyor.
        // Ama quiz bittiyse yükleyebiliriz.
        isAnswerSubmitted = savedStateHandle.get<Boolean>(IS_SUBMITTED_KEY) ?: false,
        isQuizFinished = savedStateHandle.get<Boolean>(IS_FINISHED_KEY) ?: false
        // userAnswers'ı şimdilik yüklemiyoruz
    ))
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    init {
        // Eğer quiz bitmemişse ve sorular henüz yüklenmemişse soruları yükle.
        // Eğer quiz bitmişse (isQuizFinished true ise) tekrar yüklemeye gerek yok.
        if (uiState.value.questions !is Resource.Success && !uiState.value.isQuizFinished) {
            loadQuestions()
        } else if (uiState.value.isQuizFinished) {
            // Quiz zaten bitmiş, belki sadece sonuçları göstermek yeterli
            // VEYA Kullanıcı geçmiş sonuçları görsün isteniyorsa soruları tekrar yükleyebiliriz
            // Şimdilik bitmişse bir şey yapmayalım, UI zaten sonuç ekranını gösterir.
            // Eğer `userAnswers`'ı da kaydedip yükleseydik, burada onu da yüklerdik.
            println("QuizViewModel: Quiz zaten bitmiş durumda, soru yüklenmiyor.")
        }

    }

    private fun loadQuestions() {
        viewModelScope.launch {
            _uiState.update { it.copy(questions = Resource.Loading()) }

            repository.getQuestions(categoryId, categoryName, questionAmount)
                .collect { result ->
                    // Gelen sonucu işle ve decode et
                    val processedResult = when (result) {
                        is Resource.Success -> {
                            val decodedQuestions = decodeQuestionEntities(result.data ?: emptyList())
                            Resource.Success(decodedQuestions)
                        }
                        is Resource.Error -> Resource.Error(result.message ?: "Bilinmeyen hata", decodeQuestionEntities(result.data ?: emptyList())) // Hata durumunda bile varsa veriyi decode et
                        is Resource.Loading -> Resource.Loading()
                    }
                    _uiState.update { currentState ->
                        currentState.copy(questions = processedResult)
                    }
                }
        }
    }

    // HTML decode işlemini yapan yardımcı fonksiyon
    private fun decodeQuestionEntities(entities: List<QuestionEntity>): List<QuestionEntity> {
        return entities.map { entity ->
            entity.copy( // copy() ile yeni bir nesne oluştur, orijinali değiştirme
                question = decodeHtml(entity.question),
                correctAnswer = decodeHtml(entity.correctAnswer),
                incorrectAnswers = entity.incorrectAnswers.map { decodeHtml(it) },
                // allAnswers'ı da decode etmeliyiz
                allAnswers = entity.allAnswers.map { decodeHtml(it) }
            )
        }
    }

    // Tek bir string'i decode eden fonksiyon
    private fun decodeHtml(html: String): String {
        // HtmlCompat daha modern ve güvenli bir alternatiftir
        return htmlDecoder.decode(html)
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
        val newScore = if (isCorrect) currentState.score + 1 else currentState.score

        _uiState.update {
            it.copy(
                score = if (isCorrect) it.score + 1 else it.score,
                isAnswerSubmitted = true,
                userAnswers = it.userAnswers + (currentQuestion to it.selectedAnswer)
            )
        }
        // --- Durumu SavedStateHandle'a Kaydet ---
        savedStateHandle[SCORE_KEY] = newScore
        savedStateHandle[IS_SUBMITTED_KEY] = true
    }

    fun nextQuestion() {
        val currentState = _uiState.value
        val questions = (currentState.questions as? Resource.Success)?.data ?: return
        val nextIndex = currentState.currentQuestionIndex + 1

        if (nextIndex < questions.size) {
            val newIndex = currentState.currentQuestionIndex + 1
            _uiState.update {
                it.copy(
                    currentQuestionIndex = it.currentQuestionIndex + 1,
                    selectedAnswer = null,
                    isAnswerSubmitted = false
                )
            }
            savedStateHandle[CURRENT_INDEX_KEY] = newIndex
            savedStateHandle[IS_SUBMITTED_KEY] = false
        } else {
            _uiState.update { it.copy(isQuizFinished = true, isAnswerSubmitted = false) }
            savedStateHandle[IS_FINISHED_KEY] = true
            savedStateHandle[IS_SUBMITTED_KEY] = false

        }
    }

    fun restartQuiz() {
        // ViewModel state'ini BAŞLANGIÇ durumuna sıfırla
        _uiState.value = QuizUiState() // Tüm değerleri sıfırla
        // SavedStateHandle'daki değerleri de temizle (önemli!)
        savedStateHandle.remove<Int>(CURRENT_INDEX_KEY)
        savedStateHandle.remove<Int>(SCORE_KEY)
        savedStateHandle.remove<Boolean>(IS_SUBMITTED_KEY)
        savedStateHandle.remove<Boolean>(IS_FINISHED_KEY)
        loadQuestions()
    }

    fun retryLoadQuestions() {
        loadQuestions()
    }

    // --- UserAnswers Kaydetme (İsteğe Bağlı - Daha Karmaşık) ---
    // `userAnswers: List<Pair<QuestionEntity, String?>>` listesini doğrudan SavedStateHandle'a
    // kaydetmek zordur çünkü QuestionEntity Parcelable veya Serializable değil ve büyük olabilir.
    // Olası Çözümler:
    // 1. Sadece Cevapları Kaydet: `List<String?>` olarak sadece kullanıcının verdiği cevapları
    //    (veya null) index sırasına göre kaydedebilirsiniz. Sonuç ekranında soruları tekrar
    //    yükleyip bu cevaplarla eşleştirirsiniz. Bu en basit yol.
    // 2. ID ve Cevapları Kaydet: `List<Pair<Int, String?>>` olarak soru ID'si ve cevabı
    //    kaydedebilirsiniz. Yine soruları yükleyip ID ile eşleştirirsiniz.
    // 3. Serileştirme: QuestionEntity'yi Serializable yapabilir veya GSON/Moshi ile JSON String'e
    //    çevirip kaydedebilirsiniz. Ama SavedStateHandle'ın boyut limiti olabilir ve performans
    //    etkilenebilir. Genellikle kaçınılır.
    // ŞİMDİLİK BU ADIMI ATLAYALIM. Kullanıcı uygulamayı kapatırsa sonuç özetini göremez
    // ama en azından kaldığı yerden devam edebilir veya skoru korunur.
}