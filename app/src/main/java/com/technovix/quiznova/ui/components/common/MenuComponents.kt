package com.technovix.quiznova.ui.components.common

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.technovix.quiznova.R

@Composable
fun ThemeMenuItem(
    @StringRes textRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(stringResource(textRes)) },
        onClick = onClick,
        leadingIcon = {
            if (isSelected) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = stringResource(R.string.cd_selected_theme),
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Spacer(modifier = Modifier.size(24.dp))
            }
        }
    )
}