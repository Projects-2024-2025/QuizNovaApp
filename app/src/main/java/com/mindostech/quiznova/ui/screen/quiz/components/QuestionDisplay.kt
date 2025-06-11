package com.mindostech.quiznova.ui.screen.quiz.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.mindostech.quiznova.R
import com.mindostech.quiznova.data.local.entity.QuestionEntity
import com.mindostech.quiznova.ui.components.AppPrimaryButton
import com.mindostech.quiznova.ui.theme.ErrorRed
import com.mindostech.quiznova.ui.theme.SuccessGreen
import com.mindostech.quiznova.ui.viewmodel.QuizUiState
import kotlinx.coroutines.delay

@Composable
fun QuestionContent(
    uiState: QuizUiState,
    question: QuestionEntity,
    totalQuestions: Int,
    onAnswerSelected: (String) -> Unit,
    onSubmitAnswer: () -> Unit,
    onNextQuestion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp: Dp = configuration.screenWidthDp.dp
    val screenHeightDp: Dp = configuration.screenHeightDp.dp

    val isCompactScreen = screenWidthDp < 380.dp
    val isVerySmallScreenHeight = screenHeightDp < 600.dp

    val progressBarHeight = if (isCompactScreen) 10.dp else 12.dp
    val topSectionBottomPadding = if (isVerySmallScreenHeight) 16.dp else 24.dp

    val questionTextBottomPadding = if (isVerySmallScreenHeight) 20.dp else 32.dp
    val questionTextHorizontalPadding = if (isCompactScreen) 8.dp else 12.dp

    val answerOptionSpacing = if (isVerySmallScreenHeight) 10.dp else 14.dp
    val lottieFeedbackSize = if (isCompactScreen) 160.dp else 200.dp

    val submitButtonVerticalPadding = if (isVerySmallScreenHeight) 8.dp else 16.dp
    val submitButtonFontSize = if (isCompactScreen) 14.sp else 16.sp

    val progress by animateFloatAsState(
        targetValue = if (totalQuestions > 0) (uiState.currentQuestionIndex + 1).toFloat() / totalQuestions.toFloat() else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "QuizProgressAnimation"
    )

    val correctComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_correct))
    val wrongComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_error))

    var showAnswerFeedback by remember(uiState.currentQuestionIndex) { mutableStateOf(false) }
    LaunchedEffect(uiState.isAnswerSubmitted) {
        if (uiState.isAnswerSubmitted) {
            showAnswerFeedback = true
            delay(1300L)
            showAnswerFeedback = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(bottom = topSectionBottomPadding)
                .fillMaxWidth()
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .weight(1f)
                    .height(progressBarHeight)
                    .clip(RoundedCornerShape(progressBarHeight / 2)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(R.string.quiz_question_progress, uiState.currentQuestionIndex + 1, totalQuestions),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = question,
                transitionSpec = {
                    val enter = fadeIn(animationSpec = tween(350, delayMillis = 150)) +
                            slideInVertically(initialOffsetY = { it / 3 }, animationSpec = tween(450, delayMillis = 150))
                    val exit = fadeOut(animationSpec = tween(200)) +
                            slideOutVertically(targetOffsetY = { -it / 3 }, animationSpec = tween(250))
                    (enter togetherWith exit).using(SizeTransform(clip = false))
                },
                label = "QuestionContentSwitch"
            ){ currentQuestion ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = currentQuestion.question,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(
                            top = 8.dp,
                            bottom = questionTextBottomPadding,
                            start = questionTextHorizontalPadding,
                            end = questionTextHorizontalPadding
                        )
                    )

                    currentQuestion.allAnswers.forEach { answer ->
                        AnswerOption(
                            text = answer,
                            isSelected = uiState.selectedAnswer == answer,
                            isCorrect = answer == currentQuestion.correctAnswer,
                            isSubmitted = uiState.isAnswerSubmitted,
                            enabled = !uiState.isAnswerSubmitted,
                            onSelect = { onAnswerSelected(answer) }
                        )
                        Spacer(modifier = Modifier.height(answerOptionSpacing))
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = showAnswerFeedback,
                enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.7f, animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
                ),
                exit = fadeOut(tween(300)) + scaleOut(targetScale = 0.8f),
                label = "AnswerFeedbackLottie"
            ) {
                val isCorrectAnswer = question.correctAnswer == uiState.selectedAnswer
                val composition = if (isCorrectAnswer) correctComposition else wrongComposition
                LottieAnimation(
                    composition = composition,
                    iterations = 1, // Tek sefer oyna
                    modifier = Modifier.size(lottieFeedbackSize)
                )
            }
        }

        AnimatedContent(
            targetState = uiState.isAnswerSubmitted,
            transitionSpec = {
                val enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300, delayMillis = 50)) + fadeIn(
                    tween(250, delayMillis = 50)
                )
                val exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(200)) + fadeOut(
                    tween(150)
                )
                enter togetherWith exit
            },
            label = "SubmitNextButtonSwitch"
        ) { isSubmitted ->
            val buttonTextRes = if (isSubmitted) {
                if (uiState.currentQuestionIndex < totalQuestions - 1) R.string.quiz_next_question else R.string.quiz_finish
            } else {
                R.string.quiz_submit_answer
            }
            val buttonIcon = if (isSubmitted) Icons.AutoMirrored.Filled.ArrowForward else Icons.Filled.Check
            val onClickAction = if (isSubmitted) onNextQuestion else onSubmitAnswer
            val isEnabled = isSubmitted || uiState.selectedAnswer != null

            AppPrimaryButton (
                onClick = onClickAction,
                enabled = isEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        bottom = submitButtonVerticalPadding,
                        top = if (isVerySmallScreenHeight) 4.dp else 8.dp
                    ),
            ) {
                Icon(buttonIcon, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(buttonTextRes), fontWeight = FontWeight.Bold, fontSize = submitButtonFontSize)
            }
        }
    }
}

@Composable
fun AnswerOption(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isSubmitted: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp: Dp = configuration.screenWidthDp.dp
    val isCompactScreen = screenWidthDp < 380.dp

    val cardPaddingVertical = if (isCompactScreen) 12.dp else 16.dp
    val cardPaddingHorizontal = 16.dp
    val answerTextStyle = if (isCompactScreen) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge
    val iconBoxSize = if (isCompactScreen) 20.dp else 24.dp

    val targetBorderColor: Color
    val targetBackgroundColor: Color
    val targetTextColor: Color
    val targetScale: Float
    val targetElevation: Dp

    when {
        isSubmitted && isCorrect -> {
            targetBorderColor = SuccessGreen
            targetBackgroundColor = SuccessGreen.copy(alpha = 0.15f)
            targetTextColor = MaterialTheme.colorScheme.onSurface
            targetScale = 1f
            targetElevation = 2.dp
        }
        isSubmitted && isSelected && !isCorrect -> {
            targetBorderColor = ErrorRed
            targetBackgroundColor = ErrorRed.copy(alpha = 0.15f)
            targetTextColor = MaterialTheme.colorScheme.onSurface
            targetScale = 1f
            targetElevation = 2.dp
        }
        isSubmitted && !isSelected && isCorrect -> {
            targetBorderColor = SuccessGreen.copy(alpha = 0.5f)
            targetBackgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
            targetTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            targetScale = 1f
            targetElevation = 1.dp
        }
        !isSubmitted && isSelected -> {
            targetBorderColor = MaterialTheme.colorScheme.primary
            targetBackgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
            targetTextColor = MaterialTheme.colorScheme.primary
            targetScale = 1.03f
            targetElevation = 4.dp
        }
        else -> {
            targetBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
            targetBackgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
            targetTextColor = MaterialTheme.colorScheme.onSurface
            targetScale = 1f
            targetElevation = 1.dp
        }
    }

    val borderColor by animateColorAsState(targetValue = targetBorderColor, animationSpec = tween(350), label = "AnswerBorderColor")
    val backgroundColor by animateColorAsState(targetValue = targetBackgroundColor, animationSpec = tween(350), label = "AnswerBgColor")
    val textColor by animateColorAsState(targetValue = targetTextColor, animationSpec = tween(350), label = "AnswerTextColor")
    val scale by animateFloatAsState(targetValue = targetScale, animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow
    ), label = "AnswerScale")
    val elevation by animateDpAsState(targetValue = targetElevation, animationSpec = tween(350), label = "AnswerElevation")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .selectable(
                selected = isSelected,
                onClick = onSelect,
                enabled = enabled,
                role = Role.RadioButton,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.5.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = cardPaddingHorizontal,
                vertical = cardPaddingVertical
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = answerTextStyle,
                modifier = Modifier.weight(1f),
                color = textColor
            )
            Spacer(modifier = Modifier.width(if (isCompactScreen) 8.dp else 12.dp))

            Box(modifier = Modifier.size(iconBoxSize), contentAlignment = Alignment.Center) {
                AnimatedContent(
                    targetState = isSubmitted,
                    transitionSpec = {
                        fadeIn(tween(150)) + scaleIn(tween(150), initialScale = 0.8f) togetherWith
                                fadeOut(tween(100)) + scaleOut(tween(100), targetScale = 0.8f) using
                                SizeTransform(clip = false)
                    },
                    label = "AnswerIconSwitch"
                ) { submitted ->
                    if (submitted) {
                        val iconVector: ImageVector? = when {
                            isSelected && isCorrect -> Icons.Filled.CheckCircle
                            isSelected && !isCorrect -> Icons.Filled.Cancel
                            !isSelected && isCorrect -> Icons.Filled.CheckCircleOutline
                            else -> null
                        }
                        val tint: Color = when {
                            isSelected && isCorrect -> SuccessGreen
                            isSelected && !isCorrect -> ErrorRed
                            !isSelected && isCorrect -> SuccessGreen.copy(alpha = 0.8f)
                            else -> Color.Unspecified
                        }
                        if (iconVector != null) {
                            Icon(imageVector = iconVector, contentDescription = null, tint = tint)
                        }
                    } else {
                        RadioButton(
                            selected = isSelected,
                            onClick = null,
                            enabled = enabled,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }
            }
        }
    }
}