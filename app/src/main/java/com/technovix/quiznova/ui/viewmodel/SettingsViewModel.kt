package com.technovix.quiznova.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.technovix.quiznova.data.datastore.ThemeSettingsRepository
import com.technovix.quiznova.util.ThemePreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeSettingsRepository: ThemeSettingsRepository
) : ViewModel() {

    // Repository'den gelen tema Flow'unu alıp bir StateFlow'a dönüştürüyoruz.
    // Bu StateFlow, UI tarafından gözlemlenebilir.
    val currentTheme: StateFlow<ThemePreference> = themeSettingsRepository.themePreferenceFlow
        .stateIn(
            scope = viewModelScope, // ViewModel'in Coroutine scope'u
            //started = SharingStarted.WhileSubscribed(5000), // Ekran görünürken akışı aktif tut
            started = SharingStarted.Eagerly,
            initialValue = ThemePreference.SYSTEM // Başlangıç değeri
        )

    // Kullanıcı yeni bir tema seçtiğinde çağrılacak fonksiyon
    fun changeTheme(newPreference: ThemePreference) {
        viewModelScope.launch {
            themeSettingsRepository.saveThemePreference(newPreference)
        }
    }
}