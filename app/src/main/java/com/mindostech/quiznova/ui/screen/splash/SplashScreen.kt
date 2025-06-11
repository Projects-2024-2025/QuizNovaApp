package com.mindostech.quiznova.ui.screen.splash

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
import com.mindostech.quiznova.R
import com.mindostech.quiznova.ui.navigation.Screen
import com.mindostech.quiznova.ui.theme.BrightBlue
import com.mindostech.quiznova.ui.theme.DarkIndigo
import com.mindostech.quiznova.ui.theme.VibrantPurple
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(navController: NavController) {

    val splashDuration = 2800L
    val logoAnimationDelay = 300L
    val logoAnimationDuration = 1200

    LaunchedEffect(key1 = true) {
        delay(splashDuration)
        navController.navigate(Screen.Category.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.8f) }

    LaunchedEffect(key1 = true) {
        delay(logoAnimationDelay)
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
            )
        }
    }

    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            DarkIndigo,
            VibrantPurple,
            BrightBlue
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_splash),
                contentDescription = "QuizNova Logo",
                modifier = Modifier
                    .size(160.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
                    .clip(CircleShape)
                    .border(2.dp, Color.White.copy(alpha = logoAlpha.value * 0.5f), CircleShape)
            )

            Spacer(modifier = Modifier.height(24.dp))

            val textAlphaDelay = logoAnimationDelay + logoAnimationDuration / 2
            val textAlpha = remember { Animatable(0f) }
            LaunchedEffect(key1 = true) {
                delay(textAlphaDelay)
                textAlpha.animateTo(1f, animationSpec = tween(durationMillis = 4000))
            }

            Text(
                text = "QuizNova",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White.copy(alpha = textAlpha.value),
                modifier = Modifier.alpha(textAlpha.value)
            )
        }
    }
}

