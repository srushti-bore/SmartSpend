package com.smartspend.app.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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
    }

    val activeProfileIdFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ACTIVE_PROFILE_ID_KEY]
    }

    val preferredCurrencyFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PREFERRED_CURRENCY_KEY] ?: "INR"
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
}
