package com.mindostech.quiznova.ui.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.mindostech.quiznova.data.local.entity.QuestionEntity
import com.mindostech.quiznova.data.repository.QuizRepository
import com.mindostech.quiznova.util.HtmlDecoder
import com.mindostech.quiznova.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

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
    val htmlDecoder: HtmlDecoder,
    private val application: Application,
    @Named("InterstitialAdUnitId") private val interstitialAdUnitId: String
) : ViewModel() {

    companion object {
        const val CATEGORY_ID_KEY = "categoryId"
        const val CATEGORY_NAME_KEY = "categoryName"
        const val CURRENT_INDEX_KEY = "currentQuestionIndex"
        const val SCORE_KEY = "score"
        const val IS_SUBMITTED_KEY = "isAnswerSubmitted"
        const val IS_FINISHED_KEY = "isQuizFinished"
    }

    private val categoryId: Int = savedStateHandle.get<Int>(CATEGORY_ID_KEY) ?: 0
    private val categoryName: String = savedStateHandle.get<String>(CATEGORY_NAME_KEY) ?: "Unknown"
    private val questionAmount = 15

    private val _uiState = MutableStateFlow(QuizUiState(
        currentQuestionIndex = savedStateHandle.get<Int>(CURRENT_INDEX_KEY) ?: 0,
        score = savedStateHandle.get<Int>(SCORE_KEY) ?: 0,
        isAnswerSubmitted = savedStateHandle.get<Boolean>(IS_SUBMITTED_KEY) ?: false,
        isQuizFinished = savedStateHandle.get<Boolean>(IS_FINISHED_KEY) ?: false
    ))
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var mInterstitialAd: InterstitialAd? = null
    private var isAdLoading: Boolean = false

    private fun loadInterstitialAd() {
        if (mInterstitialAd != null) {
            Timber.tag("QuizAd").d("Interstitial Ad is already loaded.")
            return
        }
        if (isAdLoading) {
            Timber.tag("QuizAd").d("Interstitial Ad is already being loaded.")
            return
        }
        isAdLoading = true
        Timber.tag("QuizAd").i("Requesting new Interstitial Ad from ID: $interstitialAdUnitId")
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            application.applicationContext,
            interstitialAdUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    Timber.tag("QuizAd").i("Interstitial Ad was loaded successfully.")
                    mInterstitialAd = interstitialAd
                    isAdLoading = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Timber.tag("QuizAd").e("Interstitial Ad failed to load: ${loadAdError.message} (Code: ${loadAdError.code})")
                    mInterstitialAd = null
                    isAdLoading = false
                }
            }
        )
    }

    fun showInterstitialAd(activity: Activity, onAdDismissedOrFailed: () -> Unit) {
        if (mInterstitialAd != null) {
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Timber.tag("QuizAd").d("Ad was dismissed.")
                    mInterstitialAd = null
                    loadInterstitialAd()
                    onAdDismissedOrFailed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Timber.tag("QuizAd").e("Ad failed to show: ${adError.message} (Code: ${adError.code})")
                    mInterstitialAd = null
                    loadInterstitialAd()
                    onAdDismissedOrFailed()
                }

                override fun onAdShowedFullScreenContent() {
                    Timber.tag("QuizAd").d("Ad showed fullscreen content.")
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    Timber.tag("QuizAd").d("Ad impression recorded.")
                }
            }
            Timber.tag("QuizAd").d("Attempting to show Interstitial Ad.")
            mInterstitialAd?.show(activity)
        } else {
            Timber.tag("QuizAd").w("Interstitial ad not ready. Proceeding with action and attempting to load ad for next time.")
            onAdDismissedOrFailed()
            if (!isAdLoading) {
                loadInterstitialAd()
            }
        }
    }

    init {
        Timber.tag("QuizVM").d("Init. Category ID: $categoryId, Name: $categoryName. isQuizFinished: ${uiState.value.isQuizFinished}")
        if (uiState.value.questions !is Resource.Success && !uiState.value.isQuizFinished) {
            loadQuestions()
        } else if (uiState.value.isQuizFinished) {
            Timber.tag("QuizVM").d("Quiz was already finished. Loading ad for potential action.")
            loadInterstitialAd()
        }
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            _uiState.update { it.copy(questions = Resource.Loading()) }
            repository.getQuestions(categoryId, categoryName, questionAmount)
                .catch { e ->
                    Timber.tag("QuizVM").e(e, "Error loading questions from repository.")
                    _uiState.update { it.copy(questions = Resource.Error("Soru yüklenirken bir hata oluştu: ${e.localizedMessage}")) }
                }
                .collect { result ->
                    Timber.tag("QuizVM").d("Received questions result: $result")
                    val processedResult = when (result) {
                        is Resource.Success -> {
                            val decodedQuestions = decodeQuestionEntities(result.data ?: emptyList())
                            Resource.Success(decodedQuestions)
                        }
                        is Resource.Error -> Resource.Error(
                            result.message ?: "Bilinmeyen bir hata oluştu.",
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
        val questionsData = (currentState.questions as? Resource.Success)?.data
            ?: (currentState.questions as? Resource.Error)?.data?.takeIf { it.isNotEmpty() }
            ?: run {
                Timber.tag("QuizVM").e("SubmitAnswer: Questions data is not available or empty.")
                return
            }

        if (currentState.selectedAnswer == null || currentState.isAnswerSubmitted) {
            Timber.tag("QuizVM").w("SubmitAnswer: No answer selected or already submitted.")
            return
        }

        val currentQuestion = questionsData.getOrNull(currentState.currentQuestionIndex) ?: run {
            Timber.tag("QuizVM").e("SubmitAnswer: Current question not found at index ${currentState.currentQuestionIndex}")
            return
        }

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
        Timber.tag("QuizVM").d("Answer submitted. Correct: $isCorrect, New Score: $newScore")
    }

    fun nextQuestion() {
        val currentState = _uiState.value
        val questionsData = (currentState.questions as? Resource.Success)?.data
            ?: (currentState.questions as? Resource.Error)?.data?.takeIf { it.isNotEmpty() }
            ?: run {
                Timber.tag("QuizVM").e("NextQuestion: Questions data is not available or empty.")
                return
            }

        val nextIndex = currentState.currentQuestionIndex + 1

        if (nextIndex < questionsData.size) {
            _uiState.update {
                it.copy(
                    currentQuestionIndex = nextIndex,
                    selectedAnswer = null,
                    isAnswerSubmitted = false
                )
            }
            savedStateHandle[CURRENT_INDEX_KEY] = nextIndex
            savedStateHandle[IS_SUBMITTED_KEY] = false
            Timber.tag("QuizVM").d("Moved to next question. Index: $nextIndex")
        } else {
            _uiState.update { it.copy(isQuizFinished = true, isAnswerSubmitted = false) }
            savedStateHandle[IS_FINISHED_KEY] = true
            savedStateHandle[IS_SUBMITTED_KEY] = false
            Timber.tag("QuizVM").i("Quiz finished. Loading ad.")
            loadInterstitialAd()
        }
    }

    fun restartQuiz() {
        Timber.tag("QuizVM").i("Restarting quiz...")
        _uiState.value = QuizUiState(questions = Resource.Loading())
        savedStateHandle.keys().forEach { key -> savedStateHandle.remove<Any>(key) }

        loadQuestions()
        Timber.tag("QuizVM").d("Quiz restarted. Loading ad for next finish.")
        loadInterstitialAd()
    }

    fun retryLoadQuestions() {
        Timber.tag("QuizVM").d("Retrying to load questions.")
        loadQuestions()
    }
}