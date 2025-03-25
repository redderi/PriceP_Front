package com.redderi.pricep.utils

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_settings")

class UserPreferences(private val context: Context) {
    companion object {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
    }

    val darkModeFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[DARK_MODE] ?: false }


    val languageFlow: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[SELECTED_LANGUAGE] ?: "Русский" }

    suspend fun saveDarkMode(isEnabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE] = isEnabled
        }
    }

    suspend fun saveSelectedLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_LANGUAGE] = language
        }
    }

    suspend fun resetSettings() {
        context.dataStore.edit { preferences ->
            val currentLanguage = preferences[SELECTED_LANGUAGE]
            preferences.clear()
            if (currentLanguage != null) {
                preferences[SELECTED_LANGUAGE] = currentLanguage
            }
        }
    }
}