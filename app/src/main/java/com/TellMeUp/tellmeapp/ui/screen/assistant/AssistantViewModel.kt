/**
 * @file: AssistantViewModel.kt
 * @description: ViewModel for assistant overlay — delegates recording to VoiceForegroundService
 * @dependencies: VoiceForegroundService
 * @created: 2026-05-09
 */

package com.TellMeUp.tellmeapp.ui.screen.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.TellMeUp.tellmeapp.domain.model.VoiceState
import com.TellMeUp.tellmeapp.service.VoiceForegroundService
import com.TellMeUp.tellmeapp.util.AppLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssistantUiState(
    val voiceState: VoiceState = VoiceState.IDLE,
    val recognizedText: String? = null,
    val error: String? = null,
    val finished: Boolean = false
)

@HiltViewModel
class AssistantViewModel @Inject constructor() : ViewModel() {

    companion object {
        private const val TAG = "AssistantViewModel"
    }

    private val _uiState = MutableStateFlow(AssistantUiState())
    val uiState: StateFlow<AssistantUiState> = _uiState.asStateFlow()

    fun startRecording() {
        val service = VoiceForegroundService.getInstance()
        if (service != null) {
            when (service.voiceState.value) {
                VoiceState.IDLE -> {
                    AppLogger.i(TAG, "Starting recording via ForegroundService")
                    service.startRecording()
                }
                VoiceState.RECORDING -> {
                    AppLogger.i(TAG, "Already recording — attaching observer")
                }
                VoiceState.PROCESSING -> {
                    AppLogger.w(TAG, "Service is processing, waiting...")
                }
                VoiceState.AI_PROCESSING -> {
                    AppLogger.w(TAG, "Service is AI processing, waiting...")
                }
            }
            observeService()
        } else {
            AppLogger.e(TAG, "VoiceForegroundService not running — cannot record")
            _uiState.value = _uiState.value.copy(
                error = "Сервис не запущен. Запустите сервис в приложении.",
                finished = true
            )
        }
    }

    fun stopAndRecognize() {
        AppLogger.i(TAG, "Stopping recording, starting recognition")
        val service = VoiceForegroundService.getInstance()
        service?.stopAndRecognize()
    }

    private fun observeService() {
        viewModelScope.launch(Dispatchers.Main) {
            VoiceForegroundService.getInstance()?.voiceState?.collect { state ->
                val service = VoiceForegroundService.getInstance()
                val text = service?.lastRecognizedText?.value

                _uiState.value = _uiState.value.copy(
                    voiceState = state,
                    recognizedText = text,
                    finished = state == VoiceState.IDLE && text != null
                )
            }
        }
    }
}
