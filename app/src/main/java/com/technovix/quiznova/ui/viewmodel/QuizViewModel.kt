package com.technovix.quiznova.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.technovix.quiznova.data.local.entity.QuestionEntity
import com.technovix.quiznova.data.repository.QuizRepository
import com.technovix.quiznova.util.HtmlDecoder
import com.technovix.quiznova.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber // Eğer Timber kullanıyorsanız
// import android.util.Log // Eğer Timber kullanmıyorsanız
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
    private val savedStateHandle: SavedStateHandle,
    val htmlDecoder: HtmlDecoder, // PrivacyPolicyScreen için public bırakılabilir veya ayrı çözüm
    private val application: Application // Context için
) : ViewModel() {

    companion object {
        const val CATEGORY_ID_KEY = "categoryId"
        const val CATEGORY_NAME_KEY = "categoryName"
        const val CURRENT_INDEX_KEY = "currentQuestionIndex"
        const val SCORE_KEY = "score"
        const val IS_SUBMITTED_KEY = "isAnswerSubmitted"
        const val IS_FINISHED_KEY = "isQuizFinished"
        // private const val USER_ANSWERS_KEY = "userAnswers" // Daha karmaşık
    }

    private val categoryId: Int = savedStateHandle.get<Int>(CATEGORY_ID_KEY) ?: 0
    private val categoryName: String = savedStateHandle.get<String>(CATEGORY_NAME_KEY) ?: "Unknown"
    private val questionAmount = 10 // Kaç soru çekileceği

    private val _uiState = MutableStateFlow(QuizUiState(
        currentQuestionIndex = savedStateHandle.get<Int>(CURRENT_INDEX_KEY) ?: 0,
        score = savedStateHandle.get<Int>(SCORE_KEY) ?: 0,
        isAnswerSubmitted = savedStateHandle.get<Boolean>(IS_SUBMITTED_KEY) ?: false,
        isQuizFinished = savedStateHandle.get<Boolean>(IS_FINISHED_KEY) ?: false
    ))
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    // --- REKLAM İÇİN EKLENENLER ---
    private var mInterstitialAd: InterstitialAd? = null
    // TEST İÇİN BU ID KULLANILIR. Play Store'a yüklerken KENDİ AD UNIT ID'NİZİ KULLANIN!
    private val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    private fun loadInterstitialAd() {
        if (mInterstitialAd != null) {
            Timber.d("Interstitial Ad is already loaded or currently loading.")
            // Log.d("QuizViewModel", "Interstitial Ad is already loaded or currently loading.")
            return
        }
        Timber.d("Loading Interstitial Ad...")
        // Log.d("QuizViewModel", "Loading Interstitial Ad...")
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            application.applicationContext, // Application context
            AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Timber.d("Interstitial Ad was loaded.")
                     Log.d("QuizViewModel", "Interstitial Ad was loaded.")
                    mInterstitialAd = interstitialAd
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Timber.d("Interstitial Ad failed to load: ${loadAdError.message}")
                     Log.d("QuizViewModel", "Interstitial Ad failed to load: ${loadAdError.message}")
                    mInterstitialAd = null
                }
            }
        )
    }

    fun showInterstitialAd(activity: Activity, onAdDismissed: () -> Unit) {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Timber.d("Ad was dismissed.")
                    // Log.d("QuizViewModel", "Ad was dismissed.")
                    mInterstitialAd = null // Reklamı bir kere gösterdikten sonra null yap
                    onAdDismissed()      // Asıl navigasyon işlemini yap
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Timber.d("Ad failed to show: ${adError.message}")
                    // Log.d("QuizViewModel", "Ad failed to show: ${adError.message}")
                    mInterstitialAd = null
                    onAdDismissed()      // Hata olsa bile navigasyonu yap
                }

                override fun onAdShowedFullScreenContent() {
                    Timber.d("Ad showed fullscreen content.")
                     Log.d("QuizViewModel", "Ad showed fullscreen content.")
                    // İsteğe bağlı: Reklam gösterildikten sonra hemen yenisini yüklemeye başla
                    // loadInterstitialAd()
                }
            }
            Timber.d("Showing Interstitial Ad.")
             Log.d("QuizViewModel", "Showing Interstitial Ad.")
            mInterstitialAd?.show(activity)
        } else {
            Timber.d("Interstitial ad not ready. Proceeding with navigation.")
            Log.d("QuizViewModel", "Interstitial ad not ready. Proceeding with navigation.")
            onAdDismissed() // Reklam hazır değilse direkt navigasyonu yap
        }
    }
    // --- REKLAM İÇİN EKLENENLER BİTTİ ---

    init {
        if (uiState.value.questions !is Resource.Success && !uiState.value.isQuizFinished) {
            loadQuestions()
        } else if (uiState.value.isQuizFinished) {
            Timber.d("QuizViewModel: Quiz was already finished. Loading ad for potential restart/new nav.")
            // Log.d("QuizViewModel", "QuizViewModel: Quiz was already finished. Loading ad for potential restart/new nav.")
            loadInterstitialAd() // Eğer ViewModel yeniden oluştuğunda quiz bitmişse reklam yükle
        }
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            _uiState.update { it.copy(questions = Resource.Loading()) }
            repository.getQuestions(categoryId, categoryName, questionAmount)
                .collect { result ->
                    val processedResult = when (result) {
                        is Resource.Success -> {
                            val decodedQuestions = decodeQuestionEntities(result.data ?: emptyList())
                            Resource.Success(decodedQuestions)
                        }
                        is Resource.Error -> Resource.Error(
                            result.message ?: "Bilinmeyen hata",
                            decodeQuestionEntities(result.data ?: emptyList())
                        )
                        is Resource.Loading -> Resource.Loading()
                    }
                    _uiState.update { currentState ->
                        currentState.copy(questions = processedResult)
                    }
                }
        }
    }

    private fun decodeQuestionEntities(entities: List<QuestionEntity>): List<QuestionEntity> {
        return entities.map { entity ->
            entity.copy(
                question = htmlDecoder.decode(entity.question),
                correctAnswer = htmlDecoder.decode(entity.correctAnswer),
                incorrectAnswers = entity.incorrectAnswers.map { htmlDecoder.decode(it) },
                allAnswers = entity.allAnswers.map { htmlDecoder.decode(it) }
            )
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
        val newScore = if (isCorrect) currentState.score + 1 else currentState.score

        _uiState.update {
            it.copy(
                score = newScore,
                isAnswerSubmitted = true,
                userAnswers = it.userAnswers + (currentQuestion to it.selectedAnswer)
            )
        }
        savedStateHandle[SCORE_KEY] = newScore
        savedStateHandle[IS_SUBMITTED_KEY] = true
    }

    fun nextQuestion() {
        val currentState = _uiState.value
        val questions = (currentState.questions as? Resource.Success)?.data ?: return
        val nextIndex = currentState.currentQuestionIndex + 1

        if (nextIndex < questions.size) {
            _uiState.update {
                it.copy(
                    currentQuestionIndex = nextIndex,
                    selectedAnswer = null,
                    isAnswerSubmitted = false
                )
            }
            savedStateHandle[CURRENT_INDEX_KEY] = nextIndex
            savedStateHandle[IS_SUBMITTED_KEY] = false
        } else {
            // Quiz bitti
            _uiState.update { it.copy(isQuizFinished = true, isAnswerSubmitted = false) }
            savedStateHandle[IS_FINISHED_KEY] = true
            savedStateHandle[IS_SUBMITTED_KEY] = false
            loadInterstitialAd() // QUIZ BİTTİĞİNDE REKLAMI YÜKLE
        }
    }

    fun restartQuiz() {
        _uiState.value = QuizUiState() // Tüm değerleri sıfırla
        savedStateHandle.remove<Int>(CURRENT_INDEX_KEY)
        savedStateHandle.remove<Int>(SCORE_KEY)
        savedStateHandle.remove<Boolean>(IS_SUBMITTED_KEY)
        savedStateHandle.remove<Boolean>(IS_FINISHED_KEY)
        // userAnswers'ı da temizlemek isterseniz:
        // _uiState.update { it.copy(userAnswers = emptyList()) }
        // Veya QuizUiState() zaten boş liste ile başlıyor.

        loadQuestions() // Soruları yeniden yükle
        // "Tekrar Oyna"ya basıldığında, bir sonraki quiz bittiğinde gösterilecek reklamı yüklemeye başla.
        loadInterstitialAd()
    }

    fun retryLoadQuestions() {
        loadQuestions()
    }
}