/**
 * @file: SettingsViewModel.kt
 * @description: ViewModel for settings screen managing preferences
 * @dependencies: Hilt, PreferencesStore
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.TellMeUp.tellmeapp.data.local.PreferencesStore
import com.TellMeUp.tellmeapp.domain.repository.AiChatRepository
import com.TellMeUp.tellmeapp.domain.repository.ClaudeRepository
import com.TellMeUp.tellmeapp.domain.repository.SpeechRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val apiKey: String = "",
    val isVibrationEnabled: Boolean = true,
    val isVisualNotificationEnabled: Boolean = true,
    val isDarkTheme: Boolean = true,
    val isApiKeySaved: Boolean = false,
    val aiApiKey: String = "",
    val isAiApiKeySaved: Boolean = false,
    val claudeApiKey: String = "",
    val isClaudeApiKeySaved: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesStore: PreferencesStore,
    private val speechRepository: SpeechRepository,
    private val aiChatRepository: AiChatRepository,
    private val claudeRepository: ClaudeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesStore.apiKey.collect { key ->
                _uiState.value = _uiState.value.copy(
                    apiKey = key ?: "",
                    isApiKeySaved = !key.isNullOrBlank()
                )
                if (!key.isNullOrBlank()) {
                    speechRepository.setApiKey(key)
                }
            }
        }
        viewModelScope.launch {
            preferencesStore.aiApiKey.collect { key ->
                _uiState.value = _uiState.value.copy(
                    aiApiKey = key ?: "",
                    isAiApiKeySaved = !key.isNullOrBlank()
                )
                if (!key.isNullOrBlank()) {
                    aiChatRepository.setApiKey(key)
                }
            }
        }
        viewModelScope.launch {
            preferencesStore.claudeApiKey.collect { key ->
                _uiState.value = _uiState.value.copy(
                    claudeApiKey = key ?: "",
                    isClaudeApiKeySaved = !key.isNullOrBlank()
                )
                if (!key.isNullOrBlank()) {
                    claudeRepository.setApiKey(key)
                }
            }
        }
    }

    fun onApiKeyChanged(key: String) {
        _uiState.value = _uiState.value.copy(apiKey = key, isApiKeySaved = false)
    }

    fun saveApiKey() {
        val key = _uiState.value.apiKey.trim()
        viewModelScope.launch {
            preferencesStore.saveApiKey(key)
            speechRepository.setApiKey(key)
            _uiState.value = _uiState.value.copy(isApiKeySaved = true)
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isVibrationEnabled = enabled)
    }

    fun setVisualNotificationEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isVisualNotificationEnabled = enabled)
    }

    fun setDarkTheme(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isDarkTheme = enabled)
    }

    fun onAiApiKeyChanged(key: String) {
        _uiState.value = _uiState.value.copy(aiApiKey = key, isAiApiKeySaved = false)
    }

    fun saveAiApiKey() {
        val key = _uiState.value.aiApiKey.trim()
        viewModelScope.launch {
            preferencesStore.saveAiApiKey(key)
            aiChatRepository.setApiKey(key)
            _uiState.value = _uiState.value.copy(isAiApiKeySaved = true)
        }
    }

    fun onClaudeApiKeyChanged(key: String) {
        _uiState.value = _uiState.value.copy(claudeApiKey = key, isClaudeApiKeySaved = false)
    }

    fun saveClaudeApiKey() {
        val key = _uiState.value.claudeApiKey.trim()
        viewModelScope.launch {
            preferencesStore.saveClaudeApiKey(key)
            claudeRepository.setApiKey(key)
            _uiState.value = _uiState.value.copy(isClaudeApiKeySaved = true)
        }
    }
}
