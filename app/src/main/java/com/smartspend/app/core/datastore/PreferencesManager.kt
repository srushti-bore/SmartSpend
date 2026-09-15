package com.smartspend.app.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "smartspend_preferences")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val ACTIVE_PROFILE_ID_KEY = stringPreferencesKey("active_profile_id")
        private val PREFERRED_CURRENCY_KEY = stringPreferencesKey("preferred_currency")
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode") // "SYSTEM", "LIGHT", "DARK"
        private val AI_ENABLED_KEY = booleanPreferencesKey("ai_enabled")
        private val GEMINI_API_KEY_ENCRYPTED_KEY = stringPreferencesKey("gemini_api_key_encrypted")
    }

    val activeProfileIdFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ACTIVE_PROFILE_ID_KEY]
    }

    val preferredCurrencyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PREFERRED_CURRENCY_KEY] ?: "INR"
    }

    val themeModeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE_KEY] ?: "SYSTEM"
    }

    val aiEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AI_ENABLED_KEY] ?: false
    }

    val encryptedGeminiApiKeyFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[GEMINI_API_KEY_ENCRYPTED_KEY]
    }

    suspend fun setActiveProfileId(profileId: String?) {
        context.dataStore.edit { preferences ->
            if (profileId != null) {
                preferences[ACTIVE_PROFILE_ID_KEY] = profileId
            } else {
                preferences.remove(ACTIVE_PROFILE_ID_KEY)
            }
        }
    }

    suspend fun setPreferredCurrency(currency: String) {
        context.dataStore.edit { preferences ->
            preferences[PREFERRED_CURRENCY_KEY] = currency
        }
    }

    suspend fun setThemeMode(themeMode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode
        }
    }

    suspend fun setAiEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AI_ENABLED_KEY] = enabled
        }
    }

    suspend fun setEncryptedGeminiApiKey(encryptedKey: String?) {
        context.dataStore.edit { preferences ->
            if (encryptedKey != null) {
                preferences[GEMINI_API_KEY_ENCRYPTED_KEY] = encryptedKey
            } else {
                preferences.remove(GEMINI_API_KEY_ENCRYPTED_KEY)
            }
        }
    }
}
