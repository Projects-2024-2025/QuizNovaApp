package com.technovix.quiznova.ui.screen.privacypolicy

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.technovix.quiznova.R
import com.technovix.quiznova.ui.theme.darkAppBackgroundGradient
import com.technovix.quiznova.ui.theme.lightAppBackgroundGradient
import com.technovix.quiznova.ui.viewmodel.SettingsViewModel // Tema için
import com.technovix.quiznova.util.ThemePreference
import com.technovix.quiznova.util.HtmlDecoder // HTML göstermek için
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel = hiltViewModel(), // Tema için
    //htmlDecoder: HtmlDecoder = hiltViewModel<com.technovix.quiznova.ui.viewmodel.QuizViewModel>().htmlDecoder // Alternatif yol
) {
    val currentTheme by settingsViewModel.currentTheme.collectAsState()
    val context = LocalContext.current

    val backgroundBrush = if (currentTheme == ThemePreference.DARK) {
        darkAppBackgroundGradient()
    } else {
        lightAppBackgroundGradient()
    }

    // HTML metnini decode et
    val policyHtml = stringResource(id = R.string.privacy_policy_text)
    // Basit bir HTML ayrıştırma (daha karmaşık HTML için kütüphane gerekebilir)
    val annotatedString = buildAnnotatedString {
        val decoded = HtmlCompat.fromHtml(policyHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)
        append(decoded)
        // İsteğe bağlı: Belirli stilleri manuel olarak ekleyebilirsiniz
        // Örneğin, "<b>" etiketlerini arayıp fontWeight = FontWeight.Bold uygulayabilirsiniz.
        // Ancak HtmlCompat.fromHtml çoğu temel formatlamayı halletmelidir.
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
                    .verticalScroll(rememberScrollState()) // Kaydırılabilir yap
            ) {
                Text(
                    text = annotatedString, // Decode edilmiş metni kullan
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp // Okunabilirliği artırmak için satır yüksekliği
                )
                Spacer(modifier = Modifier.height(16.dp)) // Alt boşluk
            }
        }
    }
}