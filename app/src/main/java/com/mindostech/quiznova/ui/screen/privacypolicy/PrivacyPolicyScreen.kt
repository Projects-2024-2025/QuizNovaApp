package com.mindostech.quiznova.ui.screen.privacypolicy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.mindostech.quiznova.R
import com.mindostech.quiznova.ui.theme.darkAppBackgroundGradient
import com.mindostech.quiznova.ui.theme.lightAppBackgroundGradient
import com.mindostech.quiznova.ui.viewmodel.SettingsViewModel
import com.mindostech.quiznova.util.ThemePreference
import androidx.compose.ui.text.buildAnnotatedString
import com.mindostech.quiznova.ui.theme.DarkGreyText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val currentTheme by settingsViewModel.currentTheme.collectAsState()

    val backgroundBrush = if (currentTheme == ThemePreference.DARK) {
        darkAppBackgroundGradient()
    } else {
        lightAppBackgroundGradient()
    }

    val textColor = DarkGreyText

    val policyHtml = stringResource(id = R.string.privacy_policy_text)
    val annotatedString = buildAnnotatedString {
        append(HtmlCompat.fromHtml(policyHtml, HtmlCompat.FROM_HTML_MODE_LEGACY))
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.privacy_policy_title), fontWeight = FontWeight.Bold) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = annotatedString,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}