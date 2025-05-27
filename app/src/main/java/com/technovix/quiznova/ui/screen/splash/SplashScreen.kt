package com.technovix.quiznova.ui.screen.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.technovix.quiznova.R
import com.technovix.quiznova.ui.navigation.Screen
import com.technovix.quiznova.ui.theme.BrightBlue
import com.technovix.quiznova.ui.theme.DarkIndigo
import com.technovix.quiznova.ui.theme.VibrantPurple
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(navController: NavController) {

    val splashDuration = 2800L
    val logoAnimationDelay = 300L
    val logoAnimationDuration = 1200
    val delayTime = 1500L

    LaunchedEffect(key1 = true) {
        delay(splashDuration)
        navController.navigate(Screen.Category.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    // Logo animasyonu için Animatable state'ler
    val logoAlpha = remember { Animatable(0f) } // Başlangıçta alfa 0 (görünmez)
    val logoScale = remember { Animatable(0.8f) } // Başlangıçta %80 ölçekli

    LaunchedEffect(key1 = true) {
        delay(logoAnimationDelay) // Kısa bir bekleme
        launch {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = logoAnimationDuration, easing = FastOutSlowInEasing)
            )
        }
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = logoAnimationDuration, easing = FastOutSlowInEasing)
                // Veya spring animasyonu da kullanabilirsiniz:
                // animationSpec = spring(
                //     dampingRatio = Spring.DampingRatioMediumBouncy,
                //     stiffness = Spring.StiffnessLow
                // )
            )
        }
    }

    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            DarkIndigo,      // Koyu morumsu üst renk
            VibrantPurple,   // Orta katman
            BrightBlue       // Alt katman
        )
    )



    // Lottie animasyonu (isteğe bağlı, logonuzla birlikte veya onun yerine olabilir)
    val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_splash))
    // val lottieProgress by animateLottieCompositionAsState(lottieComposition, iterations = 1, speed = 1f) // Eğer Lottie'yi de kullanacaksanız

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // İçeriği dikeyde de ortala
        ) {
            // Lottie Animasyonu (Eğer kullanmak isterseniz)
            // LottieAnimation(
            //     composition = lottieComposition,
            //     progress = { lottieProgress },
            //     modifier = Modifier.size(250.dp) // Boyutunu ayarlayın
            // )
            // Spacer(modifier = Modifier.height(16.dp)) // Lottie ve logo arası boşluk

            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo_splash), // Logonuzun drawable kaynağı
                contentDescription = "QuizNova Logo",
                modifier = Modifier
                    .size(160.dp) // Logo boyutu
                    .scale(logoScale.value) // Animasyonlu ölçek
                    .alpha(logoAlpha.value) // Animasyonlu alfa
                    .clip(CircleShape) // Dairesel kırpma (isteğe bağlı)
                    .border(2.dp, Color.White.copy(alpha = logoAlpha.value * 0.5f), CircleShape) // Alfa ile solan border (isteğe bağlı)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Uygulama Adı (Logonun alfasından biraz daha sonra belirebilir)
            val textAlphaDelay = logoAnimationDelay + logoAnimationDuration / 2
            val textAlpha = remember { Animatable(0f) }
            LaunchedEffect(key1 = true) {
                delay(textAlphaDelay)
                textAlpha.animateTo(1f, animationSpec = tween(durationMillis = 4000))
            }

            Text(
                text = "QuizNova",
                style = MaterialTheme.typography.headlineLarge, // Responsive stil kullan
                fontWeight = FontWeight.ExtraBold, // Gerekirse style'daki fontWeight'ı ezer
                color = Color.White.copy(alpha = textAlpha.value), // Alfa animasyonlu renk
                modifier = Modifier.alpha(textAlpha.value) // Genel alfa
            )
        }
    }
}

