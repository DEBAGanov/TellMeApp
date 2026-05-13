/**
 * @file: AiProviderViewModel.kt
 * @description: ViewModel for AI provider detail screen — manages prompt text per provider
 * @dependencies: PreferencesStore, AiProvider
 * @created: 2026-05-13
 */

package com.TellMeUp.tellmeapp.ui.screen.aiprovider

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.TellMeUp.tellmeapp.data.local.PreferencesStore
import com.TellMeUp.tellmeapp.domain.model.AiProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiProviderUiState(
    val provider: AiProvider = AiProvider.ZAI,
    val promptText: String = "",
    val isSaved: Boolean = false
)

@HiltViewModel
class AiProviderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val preferencesStore: PreferencesStore
) : ViewModel() {

    private val providerKey: String = savedStateHandle["providerKey"] ?: "zai"
    private val provider: AiProvider = AiProvider.fromKey(providerKey)

    private val _uiState = MutableStateFlow(AiProviderUiState(provider = provider))
    val uiState: StateFlow<AiProviderUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val promptFlow = when (provider) {
                AiProvider.ZAI -> preferencesStore.zaiPrompt
                AiProvider.CLAUDE -> preferencesStore.claudePrompt
            }
            promptFlow.collect { prompt ->
                _uiState.value = _uiState.value.copy(promptText = prompt)
            }
        }
    }

    fun onPromptChanged(text: String) {
        _uiState.value = _uiState.value.copy(promptText = text, isSaved = false)
    }

    fun savePrompt() {
        viewModelScope.launch {
            when (provider) {
                AiProvider.ZAI -> preferencesStore.saveZaiPrompt(_uiState.value.promptText)
                AiProvider.CLAUDE -> preferencesStore.saveClaudePrompt(_uiState.value.promptText)
            }
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }
}
