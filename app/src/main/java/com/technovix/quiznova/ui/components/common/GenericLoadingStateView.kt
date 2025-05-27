package com.technovix.quiznova.ui.components.common

import androidx.annotation.RawRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.technovix.quiznova.R

@Composable
fun GenericLoadingStateView(
    modifier: Modifier = Modifier,
    @RawRes lottieResId: Int = R.raw.lottie_loading,
    loadingText: String,
    lottieSizeSmall: Dp = 120.dp,
    lottieSizeLarge: Dp = 160.dp,
    spacing: Dp = 16.dp
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val isCompactScreen = screenWidthDp < 380.dp

    val finalLottieSize = if (isCompactScreen) lottieSizeSmall else lottieSizeLarge
    val textStyle = if (isCompactScreen) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieResId))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(finalLottieSize)
        )
        Spacer(modifier = Modifier.height(spacing))
        Text(
            text = loadingText,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = textStyle
        )
    }
}