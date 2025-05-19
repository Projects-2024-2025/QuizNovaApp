package com.technovix.quiznova.ui.components

import android.provider.CalendarContract.Colors
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.technovix.quiznova.R
import com.technovix.quiznova.data.local.entity.QuestionEntity
import com.technovix.quiznova.ui.theme.* // Kendi tema importlarınız
import com.technovix.quiznova.ui.viewmodel.QuizUiState
import com.technovix.quiznova.util.ResultType // Enum'u import et
import kotlinx.coroutines.delay

// --- Çıkış Onay Dialogu ---
@Composable
fun ExitConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.quiz_exit_dialog_title), fontWeight = FontWeight.Bold) },
        text = { Text(stringResource(id = R.string.quiz_exit_dialog_message)) },
        confirmButton = {
            AppPrimaryButton (
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text(stringResource(id = R.string.exit)) }
        },
        dismissButton = {
            AppTextButton (onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel), color = MaterialTheme.colorScheme.primary)
            }
        },
        shape = MaterialTheme.shapes.medium
    )
}


// --- Soru Gösterim Alanı ---
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
    // İlerleme çubuğu için animasyonlu değer
    val progress by animateFloatAsState(
        targetValue = if (totalQuestions > 0) (uiState.currentQuestionIndex + 1).toFloat() / totalQuestions.toFloat() else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "QuizProgressAnimation"
    )

    // Doğru/Yanlış geri bildirimi için Lottie animasyonları
    val correctComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_correct))
    val wrongComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_error))

    // Cevap geri bildirim animasyonunun görünürlük durumu
    var showAnswerFeedback by remember(uiState.currentQuestionIndex) { mutableStateOf(false) }
    LaunchedEffect(uiState.isAnswerSubmitted) {
        if (uiState.isAnswerSubmitted) {
            showAnswerFeedback = true
            delay(1300L) // Animasyon süresi kadar bekle
            showAnswerFeedback = false // Otomatik gizle
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 16.dp, horizontal = 4.dp), // Üst/alt ve hafif yan boşluk
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Üst Kısım: İlerleme Çubuğu ve Soru Sayacı
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(bottom = 24.dp)
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .weight(1f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = MaterialTheme.colorScheme.primary, // Ana tema rengi
                trackColor = MaterialTheme.colorScheme.surfaceVariant // İz rengi
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(R.string.quiz_question_progress, uiState.currentQuestionIndex + 1, totalQuestions),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Orta Kısım: Soru Metni ve Cevap Seçenekleri Alanı
        Box(
            modifier = Modifier
                .weight(1f) // Kalan dikey alanı doldur
                .fillMaxWidth(),
            contentAlignment = Alignment.Center // İçeriği dikeyde ortala
        ) {
            // Soru değiştiğinde animasyonlu geçiş
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
                    modifier = Modifier.padding(bottom = 16.dp) // Butonla arasına boşluk
                ) {
                    // Soru Metni
                    Text(
                        text = currentQuestion.question,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 32.dp, start = 12.dp, end = 12.dp)
                    )

                    // Cevap Seçenekleri Listesi
                    currentQuestion.allAnswers.forEach { answer ->
                        AnswerOption(
                            text = answer,
                            isSelected = uiState.selectedAnswer == answer,
                            isCorrect = answer == currentQuestion.correctAnswer,
                            isSubmitted = uiState.isAnswerSubmitted,
                            enabled = !uiState.isAnswerSubmitted, // Gönderildiyse tıklanamaz
                            onSelect = { onAnswerSelected(answer) } // Seçim callback'i
                        )
                        Spacer(modifier = Modifier.height(14.dp)) // Seçenekler arası boşluk
                    }
                }
            } // End AnimatedContent (Soru)

            // Cevap Geri Bildirim Animasyonu (Lottie) - Soru metninin üzerinde görünür
            androidx.compose.animation.AnimatedVisibility(
                visible = showAnswerFeedback,
                enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.7f, animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )),
                exit = fadeOut(tween(300)) + scaleOut(targetScale = 0.8f),
                label = "AnswerFeedbackLottie"
            ) {
                val isCorrectAnswer = question.correctAnswer == uiState.selectedAnswer
                val composition = if (isCorrectAnswer) correctComposition else wrongComposition
                LottieAnimation(
                    composition = composition,
                    iterations = 1, // Tek sefer oyna
                    modifier = Modifier.size(200.dp) // Animasyon boyutu
                )
            }
        } // End Box (Orta Kısım)

        // Alt Kısım: Gönder / Sonraki Soru Butonu
        AnimatedContent(
            targetState = uiState.isAnswerSubmitted, // Gönderilme durumuna göre buton değişir
            transitionSpec = {
                val enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300, delayMillis = 50)) + fadeIn(tween(250, delayMillis = 50))
                val exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(200)) + fadeOut(tween(150))
                enter togetherWith exit
            },
            label = "SubmitNextButtonSwitch"
        ) { isSubmitted ->
            // Buton metnini ve ikonunu duruma göre belirle
            val buttonTextRes = if (isSubmitted) {
                if (uiState.currentQuestionIndex < totalQuestions - 1) R.string.quiz_next_question else R.string.quiz_finish
            } else {
                R.string.quiz_submit_answer
            }
            val buttonIcon = if (isSubmitted) Icons.AutoMirrored.Filled.ArrowForward else Icons.Filled.Check
            val onClickAction = if (isSubmitted) onNextQuestion else onSubmitAnswer
            // Buton aktifliği: Gönderildiyse veya bir cevap seçildiyse aktif
            val isEnabled = isSubmitted || uiState.selectedAnswer != null

            AppPrimaryButton (
                onClick = onClickAction,
                enabled = isEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, top = 8.dp), // Alt ve üst boşluk
            ) {
                Icon(buttonIcon, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(buttonTextRes), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        } // End AnimatedContent (Buton)
    } // End Column (QuestionContent)
}


// --- Cevap Seçeneği ---
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
    // Hedef renkleri, ölçeği ve yüksekliği duruma göre belirle
    val targetBorderColor: Color
    val targetBackgroundColor: Color
    val targetTextColor: Color
    val targetScale: Float
    val targetElevation: Dp

    when {
        isSubmitted && isCorrect -> { // Gönderildi ve Doğru
            targetBorderColor = SuccessGreen // Tema'dan al
            targetBackgroundColor = SuccessGreen.copy(alpha = 0.15f)
            targetTextColor = MaterialTheme.colorScheme.onSurface
            targetScale = 1f
            targetElevation = 2.dp
        }
        isSubmitted && isSelected && !isCorrect -> { // Gönderildi, Seçili ama Yanlış
            targetBorderColor = ErrorRed // Tema'dan al
            targetBackgroundColor = ErrorRed.copy(alpha = 0.15f)
            targetTextColor = MaterialTheme.colorScheme.onSurface
            targetScale = 1f
            targetElevation = 2.dp
        }
        isSubmitted && !isSelected && isCorrect -> { // Gönderildi, Seçilmedi ama Doğruydu
            targetBorderColor = SuccessGreen.copy(alpha = 0.5f)
            targetBackgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
            targetTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            targetScale = 1f
            targetElevation = 1.dp
        }
        !isSubmitted && isSelected -> { // Gönderilmedi ve Seçili
            targetBorderColor = MaterialTheme.colorScheme.primary
            targetBackgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
            targetTextColor = MaterialTheme.colorScheme.primary
            targetScale = 1.03f // Hafif büyütme efekti
            targetElevation = 4.dp // Belirgin gölge
        }
        else -> { // Varsayılan veya diğer durumlar
            targetBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
            targetBackgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
            targetTextColor = MaterialTheme.colorScheme.onSurface
            targetScale = 1f
            targetElevation = 1.dp
        }
    }

    // Değerler arasındaki animasyonlu geçişler
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
            .scale(scale) // Animasyonlu ölçek
            .selectable( // Tıklanabilirlik
                selected = isSelected,
                onClick = onSelect,
                enabled = enabled, // Sadece aktifken tıklanabilir
                role = Role.RadioButton, // Erişilebilirlik rolü
                interactionSource = remember { MutableInteractionSource() },
                indication = null // Varsayılan ripple efektini kaldır
            ),
        shape = MaterialTheme.shapes.medium, // Tema şekli
        border = BorderStroke(1.5.dp, borderColor), // Animasyonlu kenarlık
        colors = CardDefaults.cardColors(containerColor = backgroundColor), // Animasyonlu arka plan
        elevation = CardDefaults.cardElevation(defaultElevation = elevation) // Animasyonlu yükseklik
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp), // İç boşluk
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cevap Metni
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f), // Kalan alanı doldur
                color = textColor // Animasyonlu metin rengi
            )
            Spacer(modifier = Modifier.width(12.dp))

            // Sağdaki İkon/RadioButton Alanı (Animasyonlu Geçişli)
            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
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
                        // Gönderildikten sonra: Doğru/Yanlış ikonu
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
                        // Gönderilmeden önce: RadioButton
                        RadioButton(
                            selected = isSelected,
                            onClick = null, // Tıklamayı Card hallediyor
                            enabled = enabled,
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                } // End AnimatedContent (Icon)
            } // End Box (Icon/Radio)
        } // End Row
    } // End Card
}


// --- Quiz Sonuç Ekranı (GÜNCELLENMİŞ) ---
@OptIn(ExperimentalMaterial3Api::class) // Scaffold gibi bileşenler için gerekebilir
@Composable
fun QuizResultContent( // İsim aynı kalıyor
    score: Int,
    totalQuestions: Int,
    userAnswers: List<Pair<QuestionEntity, String?>>,
    onRestart: () -> Unit,
    onBackToCategories: () -> Unit,
    modifier: Modifier = Modifier // Dışarıdan modifier alabilmesi iyi bir pratik
) {
    val scorePercentage = if (totalQuestions > 0) score.toFloat() / totalQuestions.toFloat() else 0f

    val resultData = remember(scorePercentage) {
        when {
            scorePercentage == 1f -> Triple(R.string.quiz_result_perfect, R.raw.lottie_anim_score, ResultType.PERFECT)
            scorePercentage >= 0.7f -> Triple(R.string.quiz_result_great, R.raw.lottie_anim_score, ResultType.GREAT)
            scorePercentage >= 0.4f -> Triple(R.string.quiz_result_good, R.raw.lottie_anim_score, ResultType.GOOD)
            else -> Triple(R.string.quiz_result_bad, R.raw.lottie_anim_score, ResultType.BAD)
        }
    }
    val (resultMessageResId, lottieResId, resultType) = resultData

    val resultColor = when (resultType) {
        ResultType.PERFECT -> PositiveGreen // Tema'dan veya özel bir renk
        ResultType.GREAT -> SuccessGreen
        ResultType.GOOD -> MaterialTheme.colorScheme.primary
        ResultType.BAD -> ErrorRed
    }

    val resultLottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieResId))
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

    // Ekranın geneli için yumuşak bir gradient (isteğe bağlı, kaldırılabilir veya QuizScreen'e taşınabilir)
    val screenBackground = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        )
    )

    // Box yerine Column kullandık, çünkü QuizScreen'deki Scaffold zaten bir Box gibi davranıyor
    // ve paddingleri yönetiyor. Arka planı QuizScreen'e taşıyabilirsiniz.
    Column(
        modifier = modifier // Dışarıdan gelen modifier'ı uygula
            .fillMaxSize()
            // .background(screenBackground) // Bu arka planı QuizScreen'in Box'ına taşıyabilirsiniz
            //.padding(16.dp)
            .border(width = 2.dp, shape = RoundedCornerShape(size = 16.dp), color = MaterialTheme.colorScheme.onPrimaryContainer).padding(32.dp), // QuizScreen'den gelen padding'e ek olarak veya onun yerine
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- ÜST KISIM: Lottie, Mesaj ve Skor ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            /*LottieAnimation(
                composition = resultLottieComposition,
                iterations = 1,
                modifier = Modifier
                    .size(if (resultType == ResultType.PERFECT || resultType == ResultType.GREAT) 180.dp else 150.dp)
                    .padding(bottom = 16.dp)
            )*/
            Text(
                text = stringResource(id = resultMessageResId),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = resultColor
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Skor ve Dairesel İlerleme Göstergesi
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(100.dp),
                    color = resultColor,
                    strokeWidth = 8.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    strokeCap = StrokeCap.Round
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$animatedScore",
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = resultColor
                    )
                    Text(
                        text = "/$totalQuestions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                }
            }
        }

        // --- ORTA KISIM: Cevap Özeti Başlığı ---
        Text(
            stringResource(R.string.quiz_result_summary),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // --- CEVAP LİSTESİ (Kartlar İçinde) ---
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(
                items = userAnswers,
                key = { index, item -> "${item.first.id}-$index" }
            ) { index, (question, userAnswer) ->
                AnswerSummaryItemCard( // Bu yardımcı Composable'ı da aynı dosyada veya ui.components altında tutun
                    questionNumber = index + 1,
                    questionText = question.question,
                    userAnswerText = userAnswer ?: stringResource(R.string.quiz_question_progress),
                    correctAnswerText = question.correctAnswer,
                    isCorrect = userAnswer == question.correctAnswer
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // --- ALT KISIM: Butonlar ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppSecondaryButton( // Kendi özel butonunuzu kullanın
                onClick = onBackToCategories,
                modifier = Modifier.weight(1.15f)
            ) {
                Icon(Icons.Filled.ListAlt, contentDescription = null)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.quiz_result_back_to_categories))
            }
            AppPrimaryButton( // Kendi özel butonunuzu kullanın
                onClick = onRestart,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Replay, contentDescription = null)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.quiz_result_play_again))
            }
        }
    }
}

// --- CEVAP ÖZETİ SATIRI İÇİN KART (Yardımcı Composable) ---
// Bu fonksiyonu da QuizResultContent ile aynı dosyaya veya
// genel ui.components altına taşıyabilirsiniz.
@Composable
fun AnswerSummaryItemCard(
    questionNumber: Int,
    questionText: String,
    userAnswerText: String,
    correctAnswerText: String,
    isCorrect: Boolean
) {
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = if (isCorrect) stringResource(R.string.cd_correct_answer) else stringResource(R.string.cd_incorrect_answer),
                tint = iconColor,
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$questionNumber. $questionText",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.quiz_result_your_answer, userAnswerText),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCorrect) MaterialTheme.colorScheme.onSurfaceVariant else iconColor,
                    fontWeight = if (!isCorrect) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!isCorrect) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.quiz_result_correct_answer, correctAnswerText),
                        style = MaterialTheme.typography.bodyMedium,
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

// Gerekli Enum (Eğer henüz yoksa veya farklı bir yerdeyse)
// enum class ResultType { PERFECT, GREAT, GOOD, BAD }

// Gerekli Tema Renkleri (Eğer henüz yoksa theme/Color.kt içinde)
// val PositiveGreen = Color(0xFF3DD598)


// --- Yükleme Animasyonu ---
@Composable
fun LoadingAnimationQuiz(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_loading))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center, // Ortala
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize() // Tüm alanı kapla
    ){
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(160.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.loading_questions), // Quiz'e özel metin
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}


// --- Boş Soru Ekranı ---
@Composable
fun EmptyQuestionsView(modifier: Modifier = Modifier, onBack: () -> Unit) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.CloudOff, // Veya ErrorOutline, SentimentDissatisfied
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.quiz_no_questions_found),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(32.dp))
        // Geri dönme butonu
        AppSecondaryButton (onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.back))
        }
    }
}


// --- Hata Ekranı ---
@Composable
fun ErrorViewQuiz(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_error))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(32.dp) // Kenar boşlukları
    ) {
        LottieAnimation(
            composition = composition,
            modifier = Modifier.size(200.dp) // Animasyon boyutu
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text( // Hata başlığı
            text = stringResource(R.string.error_oops),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error, // Hata rengi
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text( // Hata mesajı
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant // Yardımcı metin rengi
        )
        Spacer(modifier = Modifier.height(40.dp)) // Buton öncesi boşluk
        AppPrimaryButton ( // Tekrar Dene Butonu
            onClick = onRetry
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null) // Yenile ikonu
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.retry), fontWeight = FontWeight.Bold) // Kalın metin
        }
    }
}