package com.technovix.quiznova.ui.screen.settings

import com.technovix.quiznova.util.ThemePreference
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.technovix.quiznova.R
import androidx.compose.foundation.isSystemInDarkTheme // Gerekli
import com.technovix.quiznova.ui.theme.darkAppBackgroundGradient // Gerekli
import com.technovix.quiznova.ui.theme.lightAppBackgroundGradient // Gerekli
import androidx.compose.foundation.background // Gerekli
import androidx.compose.ui.graphics.Color // Gerekli

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    currentTheme: ThemePreference,
    onThemeChange: (ThemePreference) -> Unit
) {
    val backgroundBrush = if (currentTheme == ThemePreference.DARK || (currentTheme == ThemePreference.SYSTEM && isSystemInDarkTheme())) {
        darkAppBackgroundGradient()
    } else {
        lightAppBackgroundGradient()
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.settings_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.cd_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().background(backgroundBrush)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_section_theme),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ThemePreference.values().forEach { themePref ->
                    ThemePreferenceRow(
                        theme = themePref,
                        isSelected = currentTheme == themePref,
                        onSelected = { onThemeChange(themePref) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

            } // End Column
        } // End Box (Background)
    } // End Scaffold
}

/**
 * Ayarlar ekranındaki her bir tema seçeneği satırını temsil eder.
 */
@Composable
private fun ThemePreferenceRow(
    theme: ThemePreference,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    val textRes = when (theme) {
        ThemePreference.LIGHT -> R.string.theme_light
        ThemePreference.DARK -> R.string.theme_dark
        ThemePreference.SYSTEM -> R.string.theme_system
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelected,
                role = Role.RadioButton
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(id = textRes),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}