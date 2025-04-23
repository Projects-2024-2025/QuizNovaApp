package com.technovix.quiznova.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.technovix.quiznova.util.ThemePreference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

// Context için DataStore örneğini tanımla (genellikle dosya başında yapılır)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name="settings")

@Singleton
class ThemeSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Tema tercihini saklamak için bir anahtar (Key) tanımlıyoruz
    private val THEME_PREFERENCE_KEY = stringPreferencesKey("theme_preference")

    // Tema tercihini DataStore'a kaydetme fonksiyonu
    suspend fun saveThemePreference(themePreference: ThemePreference) {
        try {
            context.dataStore.edit { preferences ->
                preferences[THEME_PREFERENCE_KEY] =
                    themePreference.name // Enum'ı String olarak kaydediyoruz
            }
        } catch (e: IOException) {
            // Hata yönetimi (örn: loglama)
            println("Tema kaydedilirken hata: ${e.localizedMessage}")
        }
    }

    // Tema tercihini DataStore'dan okuyan bir Flow
    val themePreferenceFlow: Flow<ThemePreference> = context.dataStore.data
        .map { preferences ->
            // Kayıtlı String'i oku, yoksa veya geçersizse varsayılan olarak SYSTEM kullan
            val themeName = preferences[THEME_PREFERENCE_KEY] ?: ThemePreference.SYSTEM.name
            try {
                ThemePreference.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                // Geçersiz bir değer kaydedilmişse varsayılana dön
                ThemePreference.SYSTEM
            }
        }
}