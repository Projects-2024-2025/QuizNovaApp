package com.technovix.quiznova.ui.components.common

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
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
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
import com.airbnb.lottie.compose.rememberLottieComposition
import com.technovix.quiznova.R

@Composable
fun ErrorView(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val isCompact = screenWidthDp < 380.dp

    val lottieSize = if (isCompact) 170.dp else 200.dp
    val titleStyle = if (isCompact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall
    val messageStyle = if (isCompact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge
    val buttonPaddingHorizontal = if (isCompact) 24.dp else 32.dp
    val buttonPaddingVertical = if (isCompact) 10.dp else 12.dp

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_error))
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(32.dp).fillMaxSize()
    ) {
        LottieAnimation(
            composition = composition,
            modifier = Modifier.size(lottieSize)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.error_oops),
            style = titleStyle,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            style = messageStyle,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onRetry,
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            contentPadding = PaddingValues(horizontal = buttonPaddingHorizontal, vertical = buttonPaddingVertical)
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null)
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.retry), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EmptyStateView(modifier: Modifier = Modifier) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val isCompact = screenWidthDp < 380.dp

    val iconSize = if (isCompact) 70.dp else 80.dp
    val textStyle = if (isCompact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium
    Column(
        modifier = modifier.padding(horizontal = 32.dp).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            stringResource(R.string.category_empty_state),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = textStyle,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
    }
}