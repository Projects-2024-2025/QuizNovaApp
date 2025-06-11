package com.mindostech.quiznova.ui.screen.quiz

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mindostech.quiznova.R
import com.mindostech.quiznova.ui.components.common.ExitConfirmationDialog
import com.mindostech.quiznova.ui.components.common.GenericEmptyStateView
import com.mindostech.quiznova.ui.components.common.GenericErrorStateView
import com.mindostech.quiznova.ui.components.common.GenericLoadingStateView
import com.mindostech.quiznova.ui.screen.quiz.components.QuestionContent
import com.mindostech.quiznova.ui.screen.quiz.components.QuizResultContent
import com.mindostech.quiznova.ui.theme.*
import com.mindostech.quiznova.ui.viewmodel.QuizViewModel
import com.mindostech.quiznova.util.Resource
import com.mindostech.quiznova.util.ThemePreference
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
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
    val activity = remember(context) { context as? Activity }

    val configuration = LocalConfiguration.current
    val screenWidthDp: Dp = configuration.screenWidthDp.dp

    val horizontalMainPadding = when {
        screenWidthDp < 360.dp -> 8.dp
        screenWidthDp < 600.dp -> 16.dp
        else -> 24.dp
    }

    BackHandler(enabled = !uiState.isQuizFinished && !showExitDialog) {
        Timber.tag("QuizScreen").d("BackHandler: Quiz in progress. Showing exit dialog.")
        showExitDialog = true
    }

    BackHandler(enabled = uiState.isQuizFinished) {
        if (activity != null) {
            Timber.tag("QuizScreen").d("BackHandler: Quiz finished. Showing ad then popBackStack.")
            viewModel.showInterstitialAd(activity) {
                navController.popBackStack()
            }
        } else {
            Timber.tag("QuizScreen").w("BackHandler: Quiz finished, but activity is null. Popping back stack directly.")
            navController.popBackStack()
        }
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
                            if (activity != null) {
                                Timber.tag("QuizScreen").d("TopAppBar Back: Quiz finished. Showing ad then popBackStack.")
                                viewModel.showInterstitialAd(activity) {
                                    navController.popBackStack()
                                }
                            } else {
                                Timber.tag("QuizScreen").w("TopAppBar Back: Quiz finished, but activity is null. Popping back stack directly.")
                                navController.popBackStack()
                            }
                        } else {
                            Timber.tag("QuizScreen").d("TopAppBar Back: Quiz in progress. Showing exit dialog.")
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
                    .padding(horizontal = horizontalMainPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                AnimatedContent(
                    targetState = uiState.questions,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith
                                fadeOut(animationSpec = tween(300))
                    },
                    label = "QuizScreenContentSwitch"
                ) { questionsState ->
                    when (questionsState) {
                        is Resource.Loading -> {
                            GenericLoadingStateView(
                                loadingText = stringResource(R.string.loading_questions),
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        is Resource.Error -> {
                            GenericErrorStateView(
                                modifier = Modifier.align(Alignment.Center),
                                errorMessage = questionsState.message ?: stringResource(R.string.error_loading_questions),
                                onRetryClick = viewModel::retryLoadQuestions
                            )
                        }
                        is Resource.Success -> {
                            val questions = questionsState.data
                            if (questions.isNullOrEmpty()) {
                                GenericEmptyStateView(
                                    title = stringResource(R.string.quiz_no_questions_found),
                                    icon = Icons.Filled.CloudOff,
                                    actionButtonText = stringResource(R.string.back),
                                    onActionButtonClick = {
                                        Timber.tag("QuizScreen").d("EmptyQuestionsView: Navigating back.")
                                        navController.popBackStack()
                                    },
                                    modifier = Modifier.align(Alignment.Center)
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
                                                    Timber.tag("QuizScreen").d("QuizResultContent: Restarting quiz. Showing ad.")
                                                    viewModel.showInterstitialAd(activity) {
                                                        viewModel.restartQuiz()
                                                    }
                                                } else {
                                                    Timber.tag("QuizScreen").w("QuizResultContent: Restarting quiz, but activity is null.")
                                                    viewModel.restartQuiz()
                                                }
                                            },
                                            onBackToCategories = {
                                                if (activity != null) {
                                                    Timber.tag("QuizScreen").d("QuizResultContent: Going back to categories. Showing ad.")
                                                    viewModel.showInterstitialAd(activity) {
                                                        navController.popBackStack()
                                                    }
                                                } else {
                                                    Timber.tag("QuizScreen").w("QuizResultContent: Going back to categories, but activity is null.")
                                                    navController.popBackStack()
                                                }
                                            },
                                            modifier = Modifier.fillMaxSize()
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
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Timber.tag("QuizScreen").e("Error: Current question is null unexpectedly when quiz is not finished.")
                                            GenericErrorStateView(
                                                modifier = Modifier.align(Alignment.Center),
                                                errorMessage = stringResource(R.string.error_unexpected_question),
                                                onRetryClick = { viewModel.restartQuiz() }
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