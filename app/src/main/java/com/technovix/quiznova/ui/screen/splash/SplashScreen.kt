package com.technovix.quiznova.ui.screen.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
import com.technovix.quiznova.ui.theme.DarkIndigo
import com.technovix.quiznova.ui.theme.VibrantPurple
import kotlinx.coroutines.delay
@Composable
fun SplashScreen(navController: NavController) {

    val splashDuration = 2800L
    val delayTime = 1500L

    LaunchedEffect(key1 = true) {
        delay(splashDuration)
        navController.navigate(Screen.Category.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(VibrantPurple, BrightBlue)
    )

    val gradient2 = Brush.verticalGradient(
        colors = listOf(
            DarkIndigo,      // Yeni koyu morumsu üst renk
            VibrantPurple,   // Orta katman (zaten tanımlı)
            BrightBlue       // Alt katman (zaten tanımlı)
        )
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
            .background(gradient2),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // Lottie animation (without the white circle)
            Box(
                modifier = Modifier
                    .size(250.dp) // This size is similar to the circle size in the original animation
                    .padding(16.dp)
                    .let {
                        Modifier.alpha(if (progress > 0.5f) 1f else 0f) // Fade in after delay
                    },
                contentAlignment = Alignment.Center
            ) {
                // Replace the circle with the logo image
                Box(
                    modifier = Modifier
                        .size(160.dp) // Size of the logo (same as the original circle size)
                        .clip(CircleShape) // Make the image circular
                        .border(2.dp, Color.White, CircleShape) // Optional: border around the logo
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.logo_splash),
                        contentDescription = "Logo",
                        modifier = Modifier.fillMaxSize() // Fill the Box with the image
                    )
                }
            }

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

