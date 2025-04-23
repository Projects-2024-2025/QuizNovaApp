package com.technovix.quiznova.ui.screen.settings

import com.technovix.quiznova.util.ThemePreference
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState // StateFlow'u State'e çevirmek için
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.technovix.quiznova.R
import androidx.compose.foundation.isSystemInDarkTheme
import com.technovix.quiznova.ui.theme.darkAppBackgroundGradient
import com.technovix.quiznova.ui.theme.lightAppBackgroundGradient
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel // ViewModel'i almak için
import com.technovix.quiznova.ui.viewmodel.SettingsViewModel // ViewModel'i import et
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    // ViewModel'i Hilt aracılığıyla alıyoruz
    viewModel: SettingsViewModel = hiltViewModel()
) {
    // ViewModel'den mevcut tema durumunu alıyoruz
    val currentTheme by viewModel.currentTheme.collectAsState()

    // Arka planı mevcut temaya göre ayarlıyoruz
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
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .semantics { heading() }
                )

                // Tema seçeneklerini döngüyle oluşturuyoruz
                ThemePreference.values().forEach { themePref ->
                    ThemePreferenceRow(
                        theme = themePref,
                        // Seçili olup olmadığını ViewModel'deki state'e göre belirliyoruz
                        isSelected = currentTheme == themePref,
                        // Seçildiğinde ViewModel'deki fonksiyonu çağırıyoruz
                        onSelected = { viewModel.changeTheme(themePref) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Diğer ayarlar buraya eklenebilir...

            } // End Column
        } // End Box (Background)
    } // End Scaffold
}

/**
 * Ayarlar ekranındaki her bir tema seçeneği satırını temsil eder.
 * (Bu Composable değişmedi)
 */
/*
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
            onClick = null // Tıklamayı Row hallediyor
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(id = textRes),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

 */
// SettingsScreen.kt veya ilgili dosya içinde

@Composable
private fun ThemePreferenceRow(
    theme: ThemePreference,
    isSelected: Boolean,
    onSelected: () -> Unit
) {
    val textRes = when (theme) { ThemePreference.LIGHT -> R.string.theme_light
        ThemePreference.DARK -> R.string.theme_dark
        ThemePreference.SYSTEM -> R.string.theme_system }
    // Test tag'lerini oluştur
    val radioTag = "theme_radio_${theme.name.lowercase()}" // örn: "theme_radio_system"
    val rowTag = "theme_row_${theme.name.lowercase()}" // örn: "theme_row_system"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onSelected,
                role = Role.RadioButton
            )
            .padding(vertical = 12.dp)
            .testTag(rowTag), // Tıklama için Row'a tag ekle
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null,
            modifier = Modifier.testTag(radioTag) // RadioButton'a tag ekle
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(id = textRes),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}