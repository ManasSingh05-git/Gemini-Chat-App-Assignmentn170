package com.N170_Manassingh.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

enum class AppTheme {
    SYSTEM, LIGHT, DARK
}

open class UserPreferencesRepository(context: Context) {

    private val applicationContext = context.applicationContext

    companion object {
        private val ENCRYPTED_API_KEY = stringPreferencesKey("encrypted_gemini_api_key")
        private val THEME_KEY = stringPreferencesKey("app_theme_mode")
    }

    val encryptedApiKeyFlow: Flow<String> = applicationContext.dataStore.data.map { preferences ->
        preferences[ENCRYPTED_API_KEY] ?: ""
    }

    open val themeFlow: Flow<AppTheme> = applicationContext.dataStore.data.map { preferences ->
        when (preferences[THEME_KEY]) {
            AppTheme.LIGHT.name -> AppTheme.LIGHT
            AppTheme.DARK.name -> AppTheme.DARK
            else -> AppTheme.SYSTEM
        }
    }

    suspend fun saveEncryptedApiKey(encryptedKey: String) {
        applicationContext.dataStore.edit { preferences ->
            preferences[ENCRYPTED_API_KEY] = encryptedKey
        }
    }

    suspend fun saveTheme(theme: AppTheme) {
        applicationContext.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.name
        }
    }
}
