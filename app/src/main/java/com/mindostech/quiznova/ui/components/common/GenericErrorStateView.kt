package com.mindostech.quiznova.ui.components.common

import androidx.annotation.RawRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.mindostech.quiznova.R
import com.mindostech.quiznova.ui.components.AppPrimaryButton

@Composable
fun GenericErrorStateView(
    modifier: Modifier = Modifier,
    @RawRes lottieResId: Int = R.raw.lottie_error,
    lottieSizeSmall: Dp = 170.dp,
    lottieSizeLarge: Dp = 200.dp,
    errorTitle: String = stringResource(R.string.error_oops),
    errorMessage: String,
    retryButtonText: String = stringResource(R.string.retry),
    onRetryClick: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val isCompactScreen = screenWidthDp < 380.dp

    val finalLottieSize = if (isCompactScreen) lottieSizeSmall else lottieSizeLarge
    val titleStyle = if (isCompactScreen) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall
    val messageStyle = if (isCompactScreen) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge
    val buttonPaddingHorizontal = if (isCompactScreen) 24.dp else 32.dp
    val buttonPaddingVertical = if (isCompactScreen) 10.dp else 12.dp

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottieResId))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .padding(32.dp)
            .fillMaxSize()
    ) {
        LottieAnimation(
            composition = composition,
            modifier = Modifier.size(finalLottieSize)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = errorTitle,
            style = titleStyle,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = errorMessage,
            style = messageStyle,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(40.dp))
        AppPrimaryButton(
            onClick = onRetryClick,
            contentPadding = PaddingValues(horizontal = buttonPaddingHorizontal, vertical = buttonPaddingVertical)
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(retryButtonText, fontWeight = FontWeight.Bold)
        }
    }
}
