package com.technovix.quiznova.ui.screen.quiz

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.technovix.quiznova.R
import com.technovix.quiznova.ui.components.EmptyQuestionsView
import com.technovix.quiznova.ui.components.ErrorViewQuiz
import com.technovix.quiznova.ui.components.ExitConfirmationDialog
import com.technovix.quiznova.ui.components.LoadingAnimationQuiz
import com.technovix.quiznova.ui.components.QuestionContent
import com.technovix.quiznova.ui.components.QuizResultContent
import com.technovix.quiznova.ui.theme.*
import com.technovix.quiznova.ui.viewmodel.QuizViewModel
import com.technovix.quiznova.util.Resource
import com.technovix.quiznova.util.ThemePreference
import com.technovix.quiznova.ui.viewmodel.QuizUiState // QuizUiState'i import etmeyi unutma

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun QuizScreen(
    navController: NavController,
    categoryName: String,
    viewModel: QuizViewModel = hiltViewModel(),
    currentTheme: ThemePreference
) {
    val uiState by viewModel.uiState.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity // Activity'yi güvenli bir şekilde al

    BackHandler(enabled = !uiState.isQuizFinished && !showExitDialog) {
        showExitDialog = true
    }

    if (showExitDialog) {
        ExitConfirmationDialog(
            onConfirm = {
                showExitDialog = false
                navController.popBackStack()
            },
            onDismiss = { showExitDialog = false }
        )
    }

    val backgroundBrush = if (currentTheme == ThemePreference.DARK) {
        darkAppBackgroundGradient()
    } else {
        lightAppBackgroundGradient()
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = categoryName,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.isQuizFinished) {
                            // Quiz bittiyse ve geri gidiyorsa reklam gösterme
                            // Ya da istersen burada da reklam gösterebilirsin, ama genelde sonuç ekranındaki butonlar için istenir.
                            navController.popBackStack()
                        } else {
                            showExitDialog = true
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .testTag("quiz_screen_container")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp), // Yatay padding'i burada ver
                contentAlignment = Alignment.TopCenter // İçeriği üstte ortala
            ) {
                AnimatedContent(
                    targetState = uiState.questions,
                    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                    label = "QuizScreenContentSwitch"
                ) { questionsState ->
                    when (questionsState) {
                        is Resource.Loading -> LoadingAnimationQuiz(modifier = Modifier.align(Alignment.Center))
                        is Resource.Error -> ErrorViewQuiz(
                            modifier = Modifier.align(Alignment.Center).padding(32.dp),
                            message = questionsState.message ?: stringResource(R.string.error_loading_questions),
                            onRetry = viewModel::retryLoadQuestions
                        )
                        is Resource.Success -> {
                            val questions = questionsState.data
                            if (questions.isNullOrEmpty()) {
                                EmptyQuestionsView(
                                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                                    onBack = { navController.popBackStack() }
                                )
                            } else {
                                AnimatedContent(
                                    targetState = uiState.isQuizFinished,
                                    transitionSpec = {
                                        val enter = fadeIn(animationSpec = tween(450, delayMillis = 100)) +
                                                scaleIn(initialScale = 0.90f, animationSpec = tween(450, delayMillis = 100))
                                        val exit = fadeOut(animationSpec = tween(250)) +
                                                scaleOut(targetScale = 1.1f, animationSpec = tween(300))
                                        (enter togetherWith exit).using(SizeTransform(clip = false))
                                    },
                                    label = "QuizFinishSwitch"
                                ) { isFinished ->
                                    if (isFinished) {
                                        QuizResultContent(
                                            score = uiState.score,
                                            totalQuestions = questions.size,
                                            userAnswers = uiState.userAnswers,
                                            onRestart = {
                                                if (activity != null) {
                                                    viewModel.showInterstitialAd(activity) {
                                                        viewModel.restartQuiz()
                                                    }
                                                } else {
                                                    // Activity null ise (genellikle olmaz ama önlem)
                                                    viewModel.restartQuiz()
                                                }
                                            },
                                            onBackToCategories = {
                                                if (activity != null) {
                                                    viewModel.showInterstitialAd(activity) {
                                                        navController.popBackStack()
                                                    }
                                                } else {
                                                    // Activity null ise
                                                    navController.popBackStack()
                                                }
                                            },
                                            // QuizResultContent'in kendi padding'i varsa,
                                            // Modifier.padding(horizontal = 16.dp) buraya da eklenebilir
                                            // ama QuizResultContent içindeki Column'a padding vermek daha iyi olabilir.
                                            // Şimdilik QuizResultContent'in kendi padding'ini yönettiğini varsayıyoruz.
                                            modifier = Modifier.fillMaxSize() // Sonuç ekranının tüm alanı kaplaması için
                                        )
                                    } else {
                                        val currentQuestion = questions.getOrNull(uiState.currentQuestionIndex)
                                        if (currentQuestion != null) {
                                            QuestionContent(
                                                uiState = uiState,
                                                question = currentQuestion,
                                                totalQuestions = questions.size,
                                                onAnswerSelected = viewModel::onAnswerSelected,
                                                onSubmitAnswer = viewModel::submitAnswer,
                                                onNextQuestion = viewModel::nextQuestion,
                                                // QuestionContent'in kendi padding'i varsa,
                                                // Modifier.padding(horizontal = 16.dp) buraya da eklenebilir.
                                                // Ama bu padding Box'tan geliyor.
                                                modifier = Modifier.fillMaxSize() // Soru içeriğinin tüm alanı kaplaması için
                                            )
                                        } else {
                                            // Bu durum normalde olmamalı, question listesi boş değilse ve index sınırlar içindeyse.
                                            ErrorViewQuiz(
                                                modifier = Modifier.align(Alignment.Center).padding(32.dp),
                                                message = stringResource(R.string.error_unexpected_question),
                                                onRetry = { viewModel.restartQuiz() }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}