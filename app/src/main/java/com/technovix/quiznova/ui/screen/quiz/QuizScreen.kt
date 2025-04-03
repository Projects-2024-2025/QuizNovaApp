
package com.technovix.quiznova.ui.screen.quiz // Replace with your actual package

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.technovix.quiznova.R // Replace with your actual R file import
import com.technovix.quiznova.data.local.entity.QuestionEntity // Replace with your actual entity import
import com.technovix.quiznova.ui.theme.* // Import your theme package
import com.technovix.quiznova.ui.viewmodel.QuizUiState // Replace with your actual state import
import com.technovix.quiznova.ui.viewmodel.QuizViewModel // Replace with your actual viewmodel import
import com.technovix.quiznova.util.Resource // Replace with your actual resource import
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun QuizScreen(
    navController: NavController,
    categoryId: Int, // Keep if needed by ViewModel init, otherwise remove if solely fetched internally
    categoryName: String, // Needed for AppBar title
    viewModel: QuizViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showExitDialog by remember { mutableStateOf(false) }

    // Back handler for exit confirmation
    BackHandler(enabled = !uiState.isQuizFinished && !showExitDialog) {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(stringResource(id = R.string.quiz_exit_dialog_title)) },
            text = { Text(stringResource(id = R.string.quiz_exit_dialog_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        navController.popBackStack()
                    },
                    // Use error color from theme for the exit button
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(id = R.string.exit)) }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text(stringResource(id = R.string.cancel)) }
            }
            // Uses default theme colors for background, text etc.
        )
    }

    Scaffold(
        // Scaffold container color will be theme's background
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = categoryName,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                        // Title color defaults well, or use MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!uiState.isQuizFinished) showExitDialog = true else navController.popBackStack()
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back)) }
                    // Icon color defaults well, or use MaterialTheme.colorScheme.onSurface
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    // Transparent background, surface color on scroll - adapts to theme
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding first
                .background( // Then apply gradient background using theme colors
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background, // e.g., SoftIvory or DeepNavy
                            MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp).copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .padding(horizontal = 16.dp), // Inner horizontal padding
            contentAlignment = Alignment.TopCenter
        ) {
            AnimatedContent(
                targetState = uiState.questions,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                label = "QuizScreenContentSwitch"
            ) { questionsState ->
                when (questionsState) {
                    is Resource.Loading -> LoadingAnimation(modifier = Modifier.align(Alignment.Center)) // Centered
                    is Resource.Error -> ErrorView( // Uses theme colors now
                        modifier = Modifier.align(Alignment.Center), // Centered
                        message = questionsState.message ?: stringResource(R.string.error_unknown),
                        onRetry = { viewModel.retryLoadQuestions() }
                    )
                    is Resource.Success -> {
                        val questions = questionsState.data
                        if (questions.isNullOrEmpty()) {
                            // Empty questions state
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp)
                            ) {
                                Icon(Icons.Filled.CloudOff, contentDescription = null,
                                    modifier = Modifier.size(60.dp),
                                    // Use theme color
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = stringResource(R.string.quiz_no_questions_found),
                                    // Use theme color
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        } else {
                            // Switch between Quiz and Result screens
                            AnimatedContent(
                                targetState = uiState.isQuizFinished,
                                transitionSpec = {
                                    val enter = fadeIn(animationSpec = tween(400, delayMillis = 150)) +
                                            scaleIn(initialScale = if (targetState) 0.90f else 1.1f, animationSpec = tween(400, delayMillis = 150))
                                    val exit = fadeOut(animationSpec = tween(150)) +
                                            scaleOut(targetScale = if (targetState) 1.1f else 0.90f, animationSpec = tween(200))
                                    (enter togetherWith exit).using(SizeTransform(clip = false))
                                },
                                label = "QuizFinishSwitch"
                            ) { isFinished ->
                                if (isFinished) {
                                    QuizResultScreen( // Uses theme colors now
                                        score = uiState.score,
                                        totalQuestions = questions.size,
                                        userAnswers = uiState.userAnswers,
                                        onRestart = viewModel::restartQuiz,
                                        onBackToCategories = { navController.popBackStack() }
                                    )
                                } else {
                                    val currentQuestion = questions.getOrNull(uiState.currentQuestionIndex)
                                    if (currentQuestion != null) {
                                        QuestionContent( // Uses theme colors now
                                            uiState = uiState,
                                            question = currentQuestion,
                                            onAnswerSelected = viewModel::onAnswerSelected,
                                            onSubmitAnswer = viewModel::submitAnswer,
                                            onNextQuestion = viewModel::nextQuestion
                                        )
                                    } else {
                                        // Error for specific question load issue
                                        ErrorView(
                                            modifier = Modifier.align(Alignment.Center),
                                            message = "Soru yüklenirken bir sorun oluştu.",
                                            onRetry = { viewModel.restartQuiz() }
                                        )
                                    }
                                }
                            }
                        }
                    }
                } // end when
            } // end AnimatedContent (questionsState)
        } // end Box
    } // end Scaffold
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QuestionContent(
    uiState: QuizUiState,
    question: QuestionEntity,
    onAnswerSelected: (String) -> Unit,
    onSubmitAnswer: () -> Unit,
    onNextQuestion: () -> Unit
) {
    val questions = (uiState.questions as? Resource.Success)?.data ?: emptyList()

    val progress by animateFloatAsState(
        targetValue = if (questions.isNotEmpty()) (uiState.currentQuestionIndex + 1).toFloat() / questions.size.toFloat() else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "QuizProgressAnimation"
    )

    // Lottie compositions (using your R file IDs)
    val correctComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_correct))
    val wrongComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_error)) // Using error for wrong

    var showAnswerFeedback by remember(uiState.currentQuestionIndex) { mutableStateOf(false) }
    LaunchedEffect(uiState.isAnswerSubmitted) {
        if (uiState.isAnswerSubmitted) {
            showAnswerFeedback = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress Bar and Counter
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth()
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                // Use primary color from theme
                color = MaterialTheme.colorScheme.primary,
                // Use dedicated track color or surfaceVariant from theme
                trackColor = MaterialTheme.colorScheme.inverseSurface // Or surfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${uiState.currentQuestionIndex + 1}/${questions.size}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                // Use primary color from theme
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Question and Answers Area
        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            AnimatedContent(
                targetState = question,
                transitionSpec = {
                    val enter = fadeIn(animationSpec = tween(300, delayMillis = 150)) +
                            slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(400, delayMillis = 150))
                    val exit = fadeOut(animationSpec = tween(150)) +
                            slideOutVertically(targetOffsetY = { -it / 2 }, animationSpec = tween(200))
                    (enter togetherWith exit).using(SizeTransform(clip = false))
                },
                label = "QuestionContentSwitch"
            ){ currentQuestion ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentQuestion.question,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp, start = 8.dp, end = 8.dp)
                        // Uses default onSurface/onBackground color from theme
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Answer options
                    currentQuestion.allAnswers.forEach { answer ->
                        AnswerOption( // Uses theme colors now
                            text = answer,
                            isSelected = uiState.selectedAnswer == answer,
                            isCorrect = answer == currentQuestion.correctAnswer,
                            isSubmitted = uiState.isAnswerSubmitted,
                            enabled = !uiState.isAnswerSubmitted,
                            onSelect = { onAnswerSelected(answer) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            // Lottie Feedback Animation
            androidx.compose.animation.AnimatedVisibility(
                visible = showAnswerFeedback && uiState.isAnswerSubmitted,
                enter = fadeIn(tween(100)) + scaleIn(initialScale = 0.6f, animationSpec = spring(stiffness = Spring.StiffnessMedium)),
                exit = fadeOut(tween(300)) + scaleOut(targetScale = 0.7f),
                label = "AnswerFeedbackLottie"
            ) {
                val isCorrectAnswer = question.correctAnswer == uiState.selectedAnswer
                val composition = if (isCorrectAnswer) correctComposition else wrongComposition
                LottieAnimation(
                    composition = composition,
                    iterations = 1,
                    modifier = Modifier.size(180.dp)
                )
                // Hide after a delay
                LaunchedEffect(Unit) {
                    delay(1200L) // Adjust delay based on animation length
                    showAnswerFeedback = false
                }
            }
        } // End Box

        // Submit/Next Button
        AnimatedContent(
            targetState = uiState.isAnswerSubmitted,
            transitionSpec = {
                val enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(250, delayMillis = 50)) + fadeIn(tween(200, delayMillis = 50))
                val exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(150)) + fadeOut(tween(100))
                enter togetherWith exit
            },
            label = "SubmitNextButtonSwitch"
        ) { isSubmitted ->
            val buttonTextRes = if (isSubmitted) {
                if (uiState.currentQuestionIndex < questions.size - 1) R.string.quiz_next_question else R.string.quiz_finish
            } else {
                R.string.quiz_submit_answer
            }
            val buttonIcon = if (isSubmitted) Icons.AutoMirrored.Filled.ArrowForward else Icons.Filled.Check
            val onClickAction = if (isSubmitted) onNextQuestion else onSubmitAnswer
            val isEnabled = isSubmitted || uiState.selectedAnswer != null

            Button(
                onClick = onClickAction,
                enabled = isEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 8.dp),
                shape = MaterialTheme.shapes.medium,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = if(isEnabled) 4.dp else 0.dp, pressedElevation = 2.dp)
                // Button uses primary color by default from theme
            ) {
                Icon(buttonIcon, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(buttonTextRes), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    } // End Column
}


@Composable
fun AnswerOption(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isSubmitted: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit
) {
    // Use theme colors for states
    val targetBorderColor = when {
        !isSubmitted && isSelected -> MaterialTheme.colorScheme.primary
        // Use the new SuccessGreen and ErrorRed from your Color.kt
        isSubmitted && isCorrect -> SuccessGreen
        isSubmitted && isSelected && !isCorrect -> ErrorRed
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) // Slightly more visible outline
    }
    val targetBackgroundColor = when {
        // Use the new SuccessGreen and ErrorRed from your Color.kt
        isSubmitted && isCorrect -> SuccessGreen.copy(alpha = 0.15f)
        isSubmitted && isSelected && !isCorrect -> ErrorRed.copy(alpha = 0.15f)
        !isSubmitted && isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp) // Subtle elevation background
    }
    val targetScale = if (isSelected && !isSubmitted) 1.02f else 1f
    val targetElevation = if(isSelected && !isSubmitted) 4.dp else 1.dp

    val borderColor by animateColorAsState(targetValue = targetBorderColor, animationSpec = tween(300), label = "AnswerBorderColor")
    val backgroundColor by animateColorAsState(targetValue = targetBackgroundColor, animationSpec = tween(300), label = "AnswerBgColor")
    val scale by animateFloatAsState(targetValue = targetScale, animationSpec = spring(), label = "AnswerScale")
    val elevation by animateDpAsState(targetValue = targetElevation, animationSpec = tween(300), label = "AnswerElevation")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .selectable(
                selected = isSelected,
                onClick = onSelect,
                enabled = enabled,
                role = Role.RadioButton,
                interactionSource = remember { MutableInteractionSource() },
                indication = null // Remove ripple, rely on scale/elevation/color change
            ),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.5.dp, borderColor), // Use animated border color
        colors = CardDefaults.cardColors(containerColor = backgroundColor), // Use animated background color
        elevation = CardDefaults.cardElevation(defaultElevation = elevation) // Use animated elevation
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
                // Use default onSurface color from theme
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(12.dp))

            // Icon/Radio Button based on state
            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                if (isSubmitted) {
                    // Icons shown after submission
                    val iconVector: ImageVector? = when {
                        isSelected && isCorrect -> Icons.Filled.CheckCircle
                        isSelected && !isCorrect -> Icons.Filled.Cancel
                        !isSelected && isCorrect -> Icons.Filled.CheckCircleOutline // Indicate correct if user missed it
                        else -> null // No icon if unselected and incorrect
                    }
                    // Tint based on correctness using new theme colors
                    val tint: Color = when {
                        isSelected && isCorrect -> SuccessGreen
                        isSelected && !isCorrect -> ErrorRed
                        !isSelected && isCorrect -> SuccessGreen.copy(alpha = 0.7f) // Dimmed correct icon
                        else -> Color.Unspecified
                    }

                    if (iconVector != null) {
                        Icon(imageVector = iconVector, contentDescription = null, tint = tint)
                    }
                } else {
                    // RadioButton shown before submission
                    RadioButton(
                        selected = isSelected,
                        onClick = null,
                        enabled = enabled,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                            unselectedColor = MaterialTheme.colorScheme.outline // Veya onSurfaceVariant
                            // disabledSelectedColor ve disabledUnselectedColor
                            // RadioButtonDefaults tarafından otomatik yönetilir.
                        )
                    )
                }
            }
        }
    }
}


@Composable
fun QuizResultScreen(
    score: Int,
    totalQuestions: Int,
    userAnswers: List<Pair<QuestionEntity, String?>>,
    onRestart: () -> Unit,
    onBackToCategories: () -> Unit
) {
    val context = LocalContext.current

    // Background gradient using theme colors
    val resultGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f), // Use secondary container color lightly
            MaterialTheme.colorScheme.background
        )
    )

    // Determine result icon, tint, message, and Lottie based on score
    val resultIconVector: ImageVector
    val iconTint: Color
    val resultMessageResId: Int
    val lottieResId: Int

    // Using updated color names from Color.kt
    when {
        score == totalQuestions -> {
            resultIconVector = Icons.Filled.WorkspacePremium; iconTint = HighlightYellow // Gold/Yellow
            resultMessageResId = R.string.quiz_result_perfect; lottieResId = R.raw.lottie_congrats
        }
        score >= totalQuestions * 0.7 -> {
            resultIconVector = Icons.Filled.EmojiEvents; iconTint = SuccessGreen // Vibrant Green
            resultMessageResId = R.string.quiz_result_great; lottieResId = R.raw.lottie_success
        }
        score >= totalQuestions * 0.4 -> {
            resultIconVector = Icons.Filled.SentimentSatisfied; iconTint = MaterialTheme.colorScheme.primary // Theme Primary
            resultMessageResId = R.string.quiz_result_good; lottieResId = R.raw.lottie_neutral
        }
        else -> {
            resultIconVector = Icons.Filled.SentimentVeryDissatisfied; iconTint = ErrorRed // Vibrant Red
            resultMessageResId = R.string.quiz_result_bad; lottieResId = R.raw.lottie_fail
        }
    }

    val resultComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieResId))
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label="ScoreAnimation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(resultGradient)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Lottie Animation
        LottieAnimation(
            composition = resultComposition,
            iterations = 1, // Play once usually
            modifier = Modifier.size(180.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Result Message
        Text(
            text = stringResource(id = resultMessageResId),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
            // Uses default onBackground/onSurface color from theme
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Animated Score
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "$animatedScore",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = iconTint // Use the determined vibrant tint
            )
            Text(
                text = "/$totalQuestions",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Normal,
                // Use a less prominent color from theme for total
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Summary Title
        Text(
            stringResource(R.string.quiz_result_summary),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            // Use theme color
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Answers Summary List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                // Use a slightly elevated surface color from theme for list background
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp).copy(alpha = 0.7f))
                .padding(vertical = 8.dp)
        ) {
            itemsIndexed(userAnswers, key = { index, item -> "${item.first.id}-$index" }) { index, (question, userAnswer) ->
                val isCorrect = userAnswer == question.correctAnswer
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = if(isCorrect) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                        contentDescription = null,
                        // Use new theme color names for icon tint
                        tint = if(isCorrect) SuccessGreen else ErrorRed,
                        modifier = Modifier.size(20.dp).padding(top = 3.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text( // Question text
                            text = "${index + 1}. ${question.question}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                            // Uses default onSurface color from theme
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text( // User's answer
                            text = "Cevabınız: ${userAnswer ?: "-"}",
                            style = MaterialTheme.typography.bodySmall,
                            // Use new theme color names for feedback
                            color = if(isCorrect) SuccessGreen else ErrorRed,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!isCorrect) { // Show correct answer if wrong
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Doğru Cevap: ${question.correctAnswer}",
                                style = MaterialTheme.typography.bodySmall,
                                // Use less prominent theme color for correct answer hint
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                // Divider between items
                if(index < userAnswers.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical=6.dp),
                        thickness = 0.5.dp,
                        // Use theme outline color, possibly dimmed
                        color = MaterialTheme.colorScheme.outline.copy(alpha=0.2f)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Bottom Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Back to Categories (Outlined)
            OutlinedButton(
                onClick = onBackToCategories,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = MaterialTheme.shapes.medium,
                // Use theme outline color for border
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Icon(Icons.Filled.ListAlt, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.quiz_result_back_to_categories))
                // Text color defaults to theme primary for OutlinedButton
            }
            // Play Again (Filled)
            Button(
                onClick = onRestart,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = MaterialTheme.shapes.medium,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                // Uses theme primary color by default
            ) {
                Icon(Icons.Filled.Replay, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.quiz_result_play_again))
                // Text color defaults to onPrimary
            }
        }
    }
}

// Loading/Error Composables (already present at the end of your original file,
// ensure they use theme colors as updated in the CategoryScreen example if needed)

@Composable
fun LoadingAnimation(modifier: Modifier = Modifier) { // Ensure this uses theme colors if needed
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_loading))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(16.dp) // Optional padding
    ){
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(150.dp)
        )
        // Optional: Add text using theme color
        Spacer(modifier = Modifier.height(8.dp))
        Text("Yükleniyor...", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

}

@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) { // Ensure this uses theme colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize() // Usually takes full space
            .padding(32.dp)
    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_error))
        LottieAnimation(
            composition = composition,
            modifier = Modifier.size(150.dp) // Or 180.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.error_oops), // Generic title
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error, // Use theme error color
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant // Less prominent color
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            // Use theme colors for the button
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.retry))
        }
    }
}