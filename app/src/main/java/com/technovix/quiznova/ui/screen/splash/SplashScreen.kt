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
import androidx.compose.ui.unit.sp
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

    // Duration for the splash screen (adjust based on Lottie animation length if needed)
    val splashDuration = 2800L // milliseconds

    LaunchedEffect(key1 = true) {
        delay(splashDuration) // Wait for the splash duration
        // Navigate to Category screen and remove Splash from backstack
        navController.navigate(Screen.Category.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    // --- Updated Gradient Background ---
    // Using the new vibrant colors defined in Color.kt
    val gradient = Brush.verticalGradient( // Changed to vertical for a different feel, linearGradient also works
        colors = listOf(
            VibrantPurple, // Start with the vibrant purple
            BrightBlue     // End with the bright blue
            // You can add more colors for a more complex gradient:
            // VibrantPurple, Color(0xFF3F51B5), BrightBlue // Example with an intermediate indigo
        )
    )

    // Lottie animation for the splash screen
    // Ensure you have 'lottie_splash.json' in your `res/raw` folder!
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_splash))
    // Control the animation's progress (optional, useful if matching delay to animation)
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = 1, // Play the animation once
        speed = 1f      // Normal speed
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient), // Apply the new gradient
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Lottie Animation View
            LottieAnimation(
                composition = composition,
                progress = { progress }, // Sync progress if needed, or remove if just playing once
                modifier = Modifier.size(250.dp) // Adjust size as needed
            )
            Spacer(modifier = Modifier.height(20.dp))

            // App Name Text
            Text(
                text = "QuizNova",
                style = MaterialTheme.typography.headlineLarge, // Use style from MaterialTheme
                fontWeight = FontWeight.ExtraBold,             // Make it stand out
                color = Color.White // White usually contrasts well with vibrant gradients
                // Ensure Color.White provides good contrast with your chosen gradient.
                // If not, consider a very light color from your theme like SoftIvory
                // or even a dark color if the gradient is very light at the bottom.
            )
        }
    }
}

// Reminder: Make sure you have a Lottie animation file named `lottie_splash.json`
// placed in your project's `app/src/main/res/raw/` directory.