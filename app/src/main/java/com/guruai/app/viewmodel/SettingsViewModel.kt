package com.guruai.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.guruai.app.data.Prefs
import com.guruai.app.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val geminiKey: String = "",
    val grokKey: String = "",
    val aiProvider: String = "gemini",
    val onlineMode: Boolean = true,
    val ttsEnabled: Boolean = true,
    val themeIndex: Int = 0,
    val searchApiKey: String = "",
    val searchCx: String = "",
    val isSaved: Boolean = false,
    val themeNames: List<String> = Constants.THEMES.map { it.name }
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = Prefs(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
        _uiState.value = SettingsUiState(
            geminiKey = prefs.geminiKey,
            grokKey = prefs.grokKey,
            aiProvider = prefs.aiProvider,
            onlineMode = prefs.aiOnlineMode,
            ttsEnabled = prefs.ttsEnabled,
            themeIndex = prefs.themeIndex,
            searchApiKey = prefs.searchApiKey,
            searchCx = prefs.searchCx,
            themeNames = Constants.THEMES.map { it.name }
        )
    }

    fun updateGeminiKey(v: String) { _uiState.value = _uiState.value.copy(geminiKey = v, isSaved = false) }
    fun updateGrokKey(v: String) { _uiState.value = _uiState.value.copy(grokKey = v, isSaved = false) }
    fun updateProvider(v: String) { _uiState.value = _uiState.value.copy(aiProvider = v, isSaved = false) }
    fun updateOnlineMode(v: Boolean) { _uiState.value = _uiState.value.copy(onlineMode = v, isSaved = false) }
    fun updateTts(v: Boolean) { _uiState.value = _uiState.value.copy(ttsEnabled = v, isSaved = false) }
    fun updateTheme(index: Int) { _uiState.value = _uiState.value.copy(themeIndex = index, isSaved = false) }
    fun updateSearchKey(v: String) { _uiState.value = _uiState.value.copy(searchApiKey = v, isSaved = false) }
    fun updateSearchCx(v: String) { _uiState.value = _uiState.value.copy(searchCx = v, isSaved = false) }

    fun save() {
        viewModelScope.launch {
            val s = _uiState.value
            prefs.geminiKey = s.geminiKey
            prefs.grokKey = s.grokKey
            prefs.aiProvider = s.aiProvider
            prefs.aiOnlineMode = s.onlineMode
            prefs.ttsEnabled = s.ttsEnabled
            prefs.themeIndex = s.themeIndex
            prefs.searchApiKey = s.searchApiKey
            prefs.searchCx = s.searchCx
            _uiState.value = s.copy(isSaved = true)
        }
    }
}
