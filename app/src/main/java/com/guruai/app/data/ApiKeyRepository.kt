package com.guruai.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "guru_ai_prefs")

class ApiKeyRepository(private val context: Context) {

    companion object {
        private val GEMINI_KEY = stringPreferencesKey("gemini_api_key")
        private val GROK_KEY = stringPreferencesKey("grok_api_key")
    }

    val geminiKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[GEMINI_KEY] ?: ""
    }

    val grokKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[GROK_KEY] ?: ""
    }

    suspend fun saveGeminiKey(key: String) {
        context.dataStore.edit { prefs ->
            prefs[GEMINI_KEY] = key.trim()
        }
    }

    suspend fun saveGrokKey(key: String) {
        context.dataStore.edit { prefs ->
            prefs[GROK_KEY] = key.trim()
        }
    }

    suspend fun saveBothKeys(gemini: String, grok: String) {
        context.dataStore.edit { prefs ->
            prefs[GEMINI_KEY] = gemini.trim()
            prefs[GROK_KEY] = grok.trim()
        }
    }
}
