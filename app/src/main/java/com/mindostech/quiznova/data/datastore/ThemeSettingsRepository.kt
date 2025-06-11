package com.mindostech.quiznova.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mindostech.quiznova.util.ThemePreference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class ThemeSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val THEME_PREFERENCE_KEY = stringPreferencesKey("theme_preference")

    /**
     * Saves the selected theme preference to DataStore.
     * @param themePreference The theme preference to save.
     */
    suspend fun saveThemePreference(themePreference: ThemePreference) {
        try {
            context.dataStore.edit { preferences ->
                preferences[THEME_PREFERENCE_KEY] = themePreference.name
            }
        } catch (e: IOException) {
            println("Error saving theme: ${e.localizedMessage}")
        }
    }

    /**
     * Flow to observe theme preference changes from DataStore.
     * Defaults to LIGHT if no preference is found or if the stored value is invalid.
     */
    val themePreferenceFlow: Flow<ThemePreference> = context.dataStore.data
        .map { preferences ->
            val themeName = preferences[THEME_PREFERENCE_KEY] ?: ThemePreference.LIGHT.name
            try {
                ThemePreference.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                ThemePreference.LIGHT
            }
        }
}