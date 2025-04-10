package com.technovix.quiznova.ui.screen.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.technovix.quiznova.R
import com.technovix.quiznova.ui.navigation.Screen
import com.technovix.quiznova.ui.theme.BrightBlue
import com.technovix.quiznova.ui.theme.VibrantPurple
import kotlinx.coroutines.delay


@Composable
fun SplashScreen(navController: NavController) {

    val splashDuration = 2800L

    LaunchedEffect(key1 = true) {
        delay(splashDuration)
        navController.navigate(Screen.Category.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    val gradient = Brush.verticalGradient(
        colors = listOf( VibrantPurple, BrightBlue )
    )

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_splash))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = 1,
        speed = 1f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.size(250.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "QuizNova",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}
