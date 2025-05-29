package com.mindostech.quiznova.ui.screen.quiz.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.mindostech.quiznova.R
import com.mindostech.quiznova.ui.components.AppPrimaryButton
import com.mindostech.quiznova.ui.components.AppSecondaryButton

@Composable
fun LoadingAnimationQuiz(modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val isCompactScreen = screenWidthDp < 380.dp

    val lottieSize = if (isCompactScreen) 120.dp else 160.dp
    val textStyle = if (isCompactScreen) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge
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
            modifier = Modifier.size(lottieSize)
        )
        Spacer(modifier = Modifier.height(if (isCompactScreen) 12.dp else 16.dp))
        Text(
            stringResource(R.string.loading_questions), // Quiz'e özel metin
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = textStyle
        )
    }
}

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