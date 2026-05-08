/**
 * @file: MainViewModel.kt
 * @description: ViewModel for main screen managing service state and voice state
 * @dependencies: Hilt, VoiceState, Subscription
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.ui.screen.main

import androidx.lifecycle.ViewModel
import com.TellMeUp.tellmeapp.domain.model.Subscription
import com.TellMeUp.tellmeapp.domain.model.VoiceState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class MainUiState(
    val isServiceActive: Boolean = false,
    val voiceState: VoiceState = VoiceState.IDLE,
    val subscription: Subscription? = null
)

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun toggleService() {
        _uiState.value = _uiState.value.copy(
            isServiceActive = !_uiState.value.isServiceActive
        )
    }
}
