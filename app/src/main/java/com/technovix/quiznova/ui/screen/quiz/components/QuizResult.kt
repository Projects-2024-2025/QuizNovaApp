package com.technovix.quiznova.ui.screen.quiz.components

import android.content.res.Configuration
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.technovix.quiznova.R
import com.technovix.quiznova.data.local.entity.QuestionEntity
import com.technovix.quiznova.ui.components.AppPrimaryButton
import com.technovix.quiznova.ui.components.AppSecondaryButton
import com.technovix.quiznova.ui.theme.ErrorRed
import com.technovix.quiznova.ui.theme.PositiveGreen
import com.technovix.quiznova.ui.theme.SuccessGreen
import com.technovix.quiznova.util.ResultType

@Composable
fun QuizResultContent(
    score: Int,
    totalQuestions: Int,
    userAnswers: List<Pair<QuestionEntity, String?>>,
    onRestart: () -> Unit,
    onBackToCategories: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp: Dp = configuration.screenWidthDp.dp
    val screenHeightDp: Dp = configuration.screenHeightDp.dp
    val orientation = configuration.orientation // Yönelimi al

    // --- Dinamik Değerler ---
    val isCompactWidth = screenWidthDp < 380.dp // Daha dar genişlikler için

    val isCompactHeight = screenHeightDp < 600.dp // Daha kısa yükseklikler için

    // Genel dikey padding
    val verticalScreenPadding = if (isCompactHeight) 12.dp else 20.dp
    // Border içindeki padding
    val borderInnerPaddingHorizontal = if (isCompactWidth) 16.dp else 24.dp
    val borderInnerPaddingVertical = if (isCompactWidth || isCompactHeight) 16.dp else 24.dp


    val scorePercentage = if (totalQuestions > 0) score.toFloat() / totalQuestions.toFloat() else 0f

    val resultData = remember(scorePercentage) {
        when {
            scorePercentage == 1f -> Triple(
                R.string.quiz_result_perfect,
                R.raw.lottie_anim_score,
                ResultType.PERFECT
            )

            scorePercentage >= 0.7f -> Triple(
                R.string.quiz_result_great,
                R.raw.lottie_anim_score,
                ResultType.GREAT
            )

            scorePercentage >= 0.4f -> Triple(
                R.string.quiz_result_good,
                R.raw.lottie_anim_score,
                ResultType.GOOD
            )

            else -> Triple(R.string.quiz_result_bad, R.raw.lottie_anim_score, ResultType.BAD)
        }
    }
    val (resultMessageResId, lottieResId, resultType) = resultData // lottieResId şu an kullanılmıyor

    val resultColor = when (resultType) {
        ResultType.PERFECT -> PositiveGreen
        ResultType.GREAT -> SuccessGreen
        ResultType.GOOD -> MaterialTheme.colorScheme.primary
        ResultType.BAD -> ErrorRed
    }

    // val resultLottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieResId)) // Lottie yorumda
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "ScoreAnimation"
    )
    val animatedProgress by animateFloatAsState(
        targetValue = scorePercentage,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label = "ProgressAnimation"
    )

    // --- Metin ve Eleman Boyutları için Dinamik Değerler ---
    val resultMessageTextStyle =
        if (isCompactWidth || isCompactHeight) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium
    val circularProgressSize = if (isCompactWidth || isCompactHeight) 80.dp else 100.dp
    val circularProgressStrokeWidth =
        if (isCompactWidth || isCompactHeight) 7.dp else 8.dp // Hafif ayar
    val scoreTextStyle = MaterialTheme.typography.headlineLarge.copy(
        fontSize = if (isCompactWidth || isCompactHeight) 30.sp else 36.sp // Hafif ayar
    )
    val totalQuestionsTextStyle =
        if (isCompactWidth || isCompactHeight) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium

    val summaryTitleStyle =
        if (isCompactWidth || isCompactHeight) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge
    val summaryTitleBottomPadding = if (isCompactHeight) 8.dp else 12.dp

    val answerSummarySpacing = if (isCompactHeight) 8.dp else 12.dp
    val buttonsSectionTopSpacing =
        if (isCompactHeight) 16.dp else 24.dp // Butonlar ve liste arası boşluk
    val buttonArrangementSpacing = if (isCompactWidth) 12.dp else 16.dp // Butonlar arası boşluk
    // --- Dinamik Değerler Sonu ---

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = verticalScreenPadding) // Dış dikey padding
            .padding(horizontal = if (isCompactWidth) 8.dp else 16.dp) // Dış yatay padding
            .border(
                width = 1.5.dp, // Biraz incelttim
                shape = RoundedCornerShape(size = 20.dp), // Biraz daha yuvarlak
                color = MaterialTheme.colorScheme.outlineVariant // Daha yumuşak bir border rengi
            )
            .padding(
                horizontal = borderInnerPaddingHorizontal,
                vertical = borderInnerPaddingVertical
            ), // Border İÇİ padding
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- ÜST KISIM: Mesaj ve Skor ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = if (isCompactHeight) 16.dp else 24.dp)
        ) {
            // LottieAnimation( ... ) // Eğer eklenecekse buraya
            Text(
                text = stringResource(id = resultMessageResId),
                style = resultMessageTextStyle,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = resultColor
            )
            Spacer(modifier = Modifier.height(if (isCompactWidth || isCompactHeight) 10.dp else 16.dp)) // Ayarlandı

            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(circularProgressSize),
                    color = resultColor,
                    strokeWidth = circularProgressStrokeWidth,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), // Biraz daha soluk
                    strokeCap = StrokeCap.Round
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$animatedScore",
                        style = scoreTextStyle,
                        fontWeight = FontWeight.ExtraBold,
                        color = resultColor
                    )
                    Text(
                        text = "/$totalQuestions",
                        style = totalQuestionsTextStyle,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(
                            start = 4.dp,
                            bottom = if (isCompactWidth) 2.dp else 4.dp
                        ) // Ayarlandı
                    )
                }
            }
        }

        // --- ORTA KISIM: Cevap Özeti Başlığı ---
        Text(
            stringResource(R.string.quiz_result_summary),
            style = summaryTitleStyle,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = summaryTitleBottomPadding)
        )

        // --- CEVAP LİSTESİ (Kartlar İçinde) ---
        LazyColumn(
            modifier = Modifier.weight(1f), // Kalan alanı doldurur
            verticalArrangement = Arrangement.spacedBy(answerSummarySpacing),
            contentPadding = PaddingValues(vertical = 4.dp) // Liste içeriğine hafif dikey padding
        ) {
            itemsIndexed(
                items = userAnswers,
                key = { _, item -> item.first.id } // Soru ID'si yeterli olmalı
            ) { index, (question, userAnswer) ->
                AnswerSummaryItemCard(
                    questionNumber = index + 1,
                    questionText = question.question,
                    userAnswerText = userAnswer
                        ?: stringResource(R.string.quiz_answer_not_given), // Daha açıklayıcı
                    correctAnswerText = question.correctAnswer,
                    isCorrect = userAnswer == question.correctAnswer
                    // AnswerSummaryItemCard'ın da kendi içinde responsive ayarlamaları olmalı (önceki kodda var)
                )
            }
        }
        Spacer(modifier = Modifier.height(buttonsSectionTopSpacing))

        // --- ALT KISIM: Butonlar ---
        if (totalQuestions > 0) { // Sadece soru varsa butonları göster
            // Yatay modda veya yeterince geniş dikey modda butonları yan yana göster
            if (orientation == Configuration.ORIENTATION_LANDSCAPE || screenWidthDp >= 480.dp) { // 480dp eşiği ayarlanabilir
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        buttonArrangementSpacing,
                        Alignment.CenterHorizontally
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppSecondaryButton(
                        onClick = onBackToCategories,
                        modifier = Modifier.weight(1f) // Eşit ağırlık verelim
                    ) {
                        Icon(Icons.Filled.ListAlt, contentDescription = null)
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.quiz_result_back_to_categories))
                    }
                    AppPrimaryButton(
                        onClick = onRestart,
                        modifier = Modifier.weight(1f) // Eşit ağırlık verelim
                    ) {
                        Icon(Icons.Filled.Replay, contentDescription = null)
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.quiz_result_play_again))
                    }
                }
            } else { // Dar dikey ekranlar için butonları alt alta al
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(buttonArrangementSpacing),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Butonların genişliğini biraz daha tutarlı yapalım
                    val buttonModifier = Modifier.fillMaxWidth(if (isCompactWidth) 0.9f else 0.8f)

                    AppSecondaryButton(
                        onClick = onBackToCategories,
                        modifier = buttonModifier
                    ) {
                        Icon(Icons.Filled.ListAlt, contentDescription = null)
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.quiz_result_back_to_categories))
                    }
                    AppPrimaryButton(
                        onClick = onRestart,
                        modifier = buttonModifier
                    ) {
                        Icon(Icons.Filled.Replay, contentDescription = null)
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.quiz_result_play_again))
                    }
                }
            }
        }
    }
}

@Composable
fun AnswerSummaryItemCard(
    questionNumber: Int,
    questionText: String,
    userAnswerText: String,
    correctAnswerText: String,
    isCorrect: Boolean
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp: Dp = configuration.screenWidthDp.dp
    val isCompactScreen = screenWidthDp < 380.dp

    val cardPaddingHorizontal = if (isCompactScreen) 12.dp else 16.dp
    val cardPaddingVertical = if (isCompactScreen) 10.dp else 12.dp
    val iconPaddingTop = if (isCompactScreen) 0.dp else 2.dp
    val textSpacing = if (isCompactScreen) 4.dp else 6.dp
    val questionTextStyle = if (isCompactScreen) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge
    val answerTextStyle = if (isCompactScreen) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium

    val cardColor = if (isCorrect) SuccessGreen.copy(alpha = 0.1f) else ErrorRed.copy(alpha = 0.1f)
    val icon = if (isCorrect) Icons.Filled.CheckCircle else Icons.Filled.Cancel
    val iconColor = if (isCorrect) SuccessGreen else ErrorRed

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = MaterialTheme.shapes.medium, ambientColor = iconColor.copy(alpha = 0.3f)),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = cardPaddingHorizontal, vertical = cardPaddingVertical),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = if (isCorrect) stringResource(R.string.cd_correct_answer) else stringResource(R.string.cd_incorrect_answer),
                tint = iconColor,
                modifier = Modifier.padding(top = iconPaddingTop)
            )
            Spacer(modifier = Modifier.width(if (isCompactScreen) 8.dp else 12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$questionNumber. $questionText",
                    style = questionTextStyle,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(textSpacing))
                Text(
                    text = stringResource(R.string.quiz_result_your_answer, userAnswerText),
                    style = answerTextStyle,
                    color = if (isCorrect) MaterialTheme.colorScheme.onSurfaceVariant else iconColor,
                    fontWeight = if (!isCorrect) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!isCorrect) {
                    Spacer(modifier = Modifier.height(if (isCompactScreen) 2.dp else 4.dp))
                    Text(
                        text = stringResource(R.string.quiz_result_correct_answer, correctAnswerText),
                        style = answerTextStyle,
                        color = SuccessGreen,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}