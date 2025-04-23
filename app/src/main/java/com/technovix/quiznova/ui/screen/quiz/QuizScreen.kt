package com.technovix.quiznova.ui.screen.quiz

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    // Geri tuşu basıldığında Quiz bitmemişse ve dialog açık değilse onay sor
    BackHandler(enabled = !uiState.isQuizFinished && !showExitDialog) {
        showExitDialog = true
    }

    // Quiz'den Çıkış Onay Dialogu
    if (showExitDialog) {
        ExitConfirmationDialog( // Ayrılmış componenti kullan
            onConfirm = {
                showExitDialog = false
                navController.popBackStack()
            },
            onDismiss = { showExitDialog = false }
        )
    }

    val backgroundBrush = if (currentTheme == ThemePreference.DARK || (currentTheme == ThemePreference.SYSTEM && isSystemInDarkTheme())) {
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
                        if (uiState.isQuizFinished) navController.popBackStack() else showExitDialog = true
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
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                AnimatedContent(
                    targetState = uiState.questions,
                    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                    label = "QuizScreenContentSwitch"
                ) { questionsState ->
                    when (questionsState) {
                        is Resource.Loading -> LoadingAnimationQuiz(modifier = Modifier.align(Alignment.Center)) // Component çağrısı
                        is Resource.Error -> ErrorViewQuiz( // Component çağrısı
                            modifier = Modifier.align(Alignment.Center).padding(32.dp),
                            message = questionsState.message ?: stringResource(R.string.error_loading_questions),
                            onRetry = viewModel::retryLoadQuestions
                        )
                        is Resource.Success -> {
                            val questions = questionsState.data
                            if (questions.isNullOrEmpty()) {
                                EmptyQuestionsView( // Component çağrısı
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
                                        QuizResultContent( // Component çağrısı (İsmi değiştirdik)
                                            score = uiState.score,
                                            totalQuestions = questions.size,
                                            userAnswers = uiState.userAnswers,
                                            onRestart = viewModel::restartQuiz,
                                            onBackToCategories = { navController.popBackStack() }
                                        )
                                    } else {
                                        val currentQuestion = questions.getOrNull(uiState.currentQuestionIndex)
                                        if (currentQuestion != null) {
                                            QuestionContent( // Component çağrısı
                                                uiState = uiState,
                                                question = currentQuestion,
                                                totalQuestions = questions.size,
                                                onAnswerSelected = viewModel::onAnswerSelected,
                                                onSubmitAnswer = viewModel::submitAnswer,
                                                onNextQuestion = viewModel::nextQuestion
                                            )
                                        } else {
                                            ErrorViewQuiz( // Component çağrısı
                                                modifier = Modifier.align(Alignment.Center).padding(32.dp),
                                                message = stringResource(R.string.error_unexpected_question),
                                                onRetry = { viewModel.restartQuiz() }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } // end when
                } // end AnimatedContent
            } // end İçerik Box
        } // end Arka Plan Box
    } // end Scaffold
}