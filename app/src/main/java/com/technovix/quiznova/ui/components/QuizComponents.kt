package com.technovix.quiznova.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.technovix.quiznova.R
import com.technovix.quiznova.data.local.entity.QuestionEntity
import com.technovix.quiznova.ui.theme.* // Kendi tema importlarınız
import com.technovix.quiznova.ui.viewmodel.QuizUiState
import com.technovix.quiznova.util.ResultType // Enum'u import et
import kotlinx.coroutines.delay


// --- Soru Gösterim Alanı ---



// --- Cevap Seçeneği ---



// --- Quiz Sonuç Ekranı (GÜNCELLENMİŞ) ---


// --- CEVAP ÖZETİ SATIRI İÇİN KART (Yardımcı Composable) ---
// Bu fonksiyonu da QuizResultContent ile aynı dosyaya veya
// genel ui.components altına taşıyabilirsiniz.


// Gerekli Enum (Eğer henüz yoksa veya farklı bir yerdeyse)
// enum class ResultType { PERFECT, GREAT, GOOD, BAD }

// Gerekli Tema Renkleri (Eğer henüz yoksa theme/Color.kt içinde)
// val PositiveGreen = Color(0xFF3DD598)


// --- Yükleme Animasyonu ---



// --- Boş Soru Ekranı ---



// --- Hata Ekranı ---
