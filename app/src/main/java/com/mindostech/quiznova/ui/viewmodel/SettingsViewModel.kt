package com.mindostech.quiznova.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindostech.quiznova.data.datastore.ThemeSettingsRepository
import com.mindostech.quiznova.util.NetworkMonitor
import com.mindostech.quiznova.util.ThemePreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themeSettingsRepository: ThemeSettingsRepository,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    // Repository'den gelen tema Flow'unu alıp bir StateFlow'a dönüştürüyoruz.
    val currentTheme: StateFlow<ThemePreference> = themeSettingsRepository.themePreferenceFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemePreference.LIGHT
        )

    // NetworkMonitor'den gelen internet durumunu tutacak StateFlow
    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    // Kullanıcı yeni bir tema seçtiğinde çağrılacak fonksiyon
    fun changeTheme(newPreference: ThemePreference) {
        viewModelScope.launch {
            themeSettingsRepository.saveThemePreference(newPreference)
        }
    }
}