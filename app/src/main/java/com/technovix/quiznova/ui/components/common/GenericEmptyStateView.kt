package com.technovix.quiznova.ui.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.technovix.quiznova.ui.components.AppSecondaryButton

@Composable
fun GenericEmptyStateView(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.SearchOff,
    iconSizeSmall: Dp = 70.dp,
    iconSizeLarge: Dp = 80.dp,
    title: String,
    message: String? = null,
    actionButtonText: String? = null,
    onActionButtonClick: (() -> Unit)? = null
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val isCompactScreen = screenWidthDp < 380.dp

    val finalIconSize = if (isCompactScreen) iconSizeSmall else iconSizeLarge
    val titleTextStyle = if (isCompactScreen) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium
    val messageStyle = if (isCompactScreen) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge

    Column(
        modifier = modifier
            .padding(horizontal = 32.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(finalIconSize),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = titleTextStyle,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium
        )
        if (message != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                style = messageStyle,
                textAlign = TextAlign.Center
            )
        }
        if (actionButtonText != null && onActionButtonClick != null) {
            Spacer(modifier = Modifier.height(32.dp))
            AppSecondaryButton(onClick = onActionButtonClick) {
                Text(
                    text = actionButtonText,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}