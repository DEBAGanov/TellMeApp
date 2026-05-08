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
    val isApiKeySaved: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesStore: PreferencesStore,
    private val speechRepository: SpeechRepository
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
}
