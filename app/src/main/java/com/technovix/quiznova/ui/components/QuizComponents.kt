package com.technovix.quiznova.ui.components

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
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text(stringResource(id = R.string.exit)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel), color = MaterialTheme.colorScheme.primary)
            }
        },
        shape = MaterialTheme.shapes.medium
    )
}


// --- Soru Gösterim Alanı ---
@OptIn(ExperimentalAnimationApi::class)
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

            Button(
                onClick = onClickAction,
                enabled = isEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 16.dp, top = 8.dp), // Alt ve üst boşluk
                shape = MaterialTheme.shapes.medium, // Tema şekli
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if(isEnabled) 4.dp else 0.dp,
                    pressedElevation = 2.dp,
                    disabledElevation = 0.dp
                ),
                colors = ButtonDefaults.buttonColors( // Tema renklerini kullan
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                )
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


// --- Quiz Sonuç Ekranı ---
@Composable
fun QuizResultContent( // İsim değişikliği (isteğe bağlı)
    score: Int,
    totalQuestions: Int,
    userAnswers: List<Pair<QuestionEntity, String?>>, // Kullanıcının cevapları ve sorular
    onRestart: () -> Unit,
    onBackToCategories: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Sonuç ekranı için yumuşak gradient arka plan
    val resultGradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.10f),
            MaterialTheme.colorScheme.background
        )
    )

    // Skor yüzdesi
    val scorePercentage = if (totalQuestions > 0) score.toFloat() / totalQuestions else 0f

    // remember içinde sadece kaynak ID'leri ve renk türünü hesapla (Composable çağrısı yok)
    val resultData = remember(scorePercentage) {
        when {
            scorePercentage == 1f -> Triple(R.string.quiz_result_perfect, R.raw.lottie_congrats, ResultType.PERFECT)
            scorePercentage >= 0.7f -> Triple(R.string.quiz_result_great, R.raw.lottie_success, ResultType.GREAT)
            scorePercentage >= 0.4f -> Triple(R.string.quiz_result_good, R.raw.lottie_neutral, ResultType.GOOD)
            else -> Triple(R.string.quiz_result_bad, R.raw.lottie_fail, ResultType.BAD)
        }
    }
    val (resultMessageResId, lottieResId, resultType) = resultData

    // Gerçek rengi Composable kapsamında al (MaterialTheme.colorScheme erişimi burada güvenli)
    val resultColor = when (resultType) {
        ResultType.PERFECT -> HighlightYellow // Tema'dan al
        ResultType.GREAT -> SuccessGreen     // Tema'dan al
        ResultType.GOOD -> MaterialTheme.colorScheme.primary // Tema rengi
        ResultType.BAD -> ErrorRed         // Tema'dan al
    }

    // Lottie animasyonunu yükle
    val resultComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieResId))
    // Skoru animasyonlu olarak göster
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = tween(1500, easing = FastOutSlowInEasing),
        label="ScoreAnimation"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(resultGradient) // Gradient arka plan
            .padding(horizontal = 20.dp, vertical = 24.dp), // İç boşluklar
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Üst Kısım: Lottie Animasyonu
        LottieAnimation(
            composition = resultComposition,
            iterations = 1, // Tek sefer oyna
            modifier = Modifier
                .size(if (resultType == ResultType.PERFECT || resultType == ResultType.GREAT) 220.dp else 180.dp) // Başarıya göre boyut
                .padding(top = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Sonuç Mesajı
        Text(
            text = stringResource(id = resultMessageResId),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = resultColor // Sonuca göre renk
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Animasyonlu Skor Gösterimi
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "$animatedScore",
                style = MaterialTheme.typography.displayLarge, // Büyük skor rakamı
                fontWeight = FontWeight.Bold,
                color = resultColor
            )
            Text(
                text = "/$totalQuestions",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 6.dp, bottom = 6.dp) // Hizalama
            )
        }
        Spacer(modifier = Modifier.height(32.dp)) // Özet öncesi boşluk

        // Orta Kısım: Cevap Özeti
        Text(
            stringResource(R.string.quiz_result_summary), // Başlık
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Kaydırılabilir Cevap Listesi
        LazyColumn(
            modifier = Modifier
                .weight(1f) // Kalan alanı doldur
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium) // Yuvarlak köşeler
                .background(
                    MaterialTheme.colorScheme
                        .surfaceColorAtElevation(1.dp)
                        .copy(alpha = 0.5f)
                ) // Hafif arka plan
                .padding(vertical = 8.dp)
        ) {
            itemsIndexed(
                items = userAnswers,
                key = { index, item -> "${item.first.id}-$index" } // Performans için anahtar
            ) { index, (question, userAnswer) ->
                val isCorrect = userAnswer == question.correctAnswer
                // Her bir cevap özeti satırı
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Doğru/Yanlış ikonu
                    val correctnessDesc = if (isCorrect) stringResource(R.string.cd_correct_answer) else
                        stringResource(R.string.cd_incorrect_answer)
                    Icon(
                        imageVector = if(isCorrect) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                        contentDescription = correctnessDesc,
                        tint = if(isCorrect) SuccessGreen else ErrorRed,
                        modifier = Modifier
                            .size(22.dp)
                            .padding(top = 2.dp) // Metinle hizalama
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    // Soru ve Cevap Metinleri
                    Column(modifier = Modifier.weight(1f)) {
                        Text( // Soru
                            text = "${index + 1}. ${question.question}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text( // Kullanıcının Cevabı
                            text = stringResource(R.string.quiz_result_your_answer, userAnswer ?: "-"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if(isCorrect) SuccessGreen else ErrorRed, // Renkli vurgu
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!isCorrect) { // Yanlışsa doğru cevabı göster
                            Spacer(modifier = Modifier.height(4.dp))
                            Text( // Doğru Cevap
                                text = stringResource(R.string.quiz_result_correct_answer, question.correctAnswer),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                // Ayırıcı Çizgi
                if(index < userAnswers.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha=0.3f)
                    )
                }
            }
        } // End LazyColumn
        Spacer(modifier = Modifier.height(24.dp)) // Butonlar öncesi boşluk

        // Alt Kısım: Butonlar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp) // Buton arası boşluk
        ) {
            // Kategorilere Dön Butonu (Kenarlıklı)
            OutlinedButton(
                onClick = onBackToCategories,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Icon(Icons.Filled.ListAlt, contentDescription = null)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.quiz_result_back_to_categories), fontWeight = FontWeight.Medium)
            }
            // Tekrar Oyna Butonu (Dolgulu)
            Button(
                onClick = onRestart,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = MaterialTheme.shapes.medium,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Filled.Replay, contentDescription = null)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.quiz_result_play_again), fontWeight = FontWeight.Bold)
            }
        } // End Row (Butonlar)
    } // End Column (QuizResultContent)
}


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
        Button(onClick = onBack) {
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
        Button( // Tekrar Dene Butonu
            onClick = onRetry,
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp) // Daha geniş buton
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null) // Yenile ikonu
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.retry), fontWeight = FontWeight.Bold) // Kalın metin
        }
    }
}