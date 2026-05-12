/**
 * @file: MainViewModel.kt
 * @description: ViewModel for main screen — delegates recording to VoiceForegroundService
 * @dependencies: Hilt, VoiceState, VoiceForegroundService
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.ui.screen.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.TellMeUp.tellmeapp.data.local.PreferencesStore
import com.TellMeUp.tellmeapp.domain.model.AiProvider
import com.TellMeUp.tellmeapp.domain.model.Subscription
import com.TellMeUp.tellmeapp.domain.model.VoiceState
import com.TellMeUp.tellmeapp.service.StopServiceReceiver
import com.TellMeUp.tellmeapp.service.VoiceForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val isServiceActive: Boolean = false,
    val voiceState: VoiceState = VoiceState.IDLE,
    val subscription: Subscription? = null,
    val lastRecognizedText: String? = null,
    val hasPermissions: Boolean = false,
    val isAiModeEnabled: Boolean = false,
    val aiProvider: AiProvider = AiProvider.ZAI
)

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesStore: PreferencesStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            _uiState.value = _uiState.value.copy(isServiceActive = false)
        }
    }

    private val stateReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            syncServiceState()
        }
    }

    init {
        viewModelScope.launch {
            preferencesStore.aiEnabled.collect { enabled ->
                _uiState.value = _uiState.value.copy(isAiModeEnabled = enabled)
            }
        }

        viewModelScope.launch {
            preferencesStore.aiProvider.collect { provider ->
                _uiState.value = _uiState.value.copy(aiProvider = AiProvider.fromKey(provider))
            }
        }

        context.registerReceiver(
            stopReceiver,
            IntentFilter(StopServiceReceiver.ACTION_SERVICE_STOPPED),
            Context.RECEIVER_NOT_EXPORTED
        )

        context.registerReceiver(
            stateReceiver,
            IntentFilter(VoiceForegroundService.ACTION_VOICE_STATE_CHANGED),
            Context.RECEIVER_NOT_EXPORTED
        )
    }

    fun onPermissionsGranted() {
        _uiState.value = _uiState.value.copy(hasPermissions = true)
    }

    fun toggleService() {
        if (_uiState.value.isServiceActive) {
            VoiceForegroundService.stop(context)
            _uiState.value = _uiState.value.copy(isServiceActive = false)
        } else {
            VoiceForegroundService.start(context)
            _uiState.value = _uiState.value.copy(isServiceActive = true)
            syncServiceState()
        }
    }

    fun startRecordingManually() {
        val service = VoiceForegroundService.getInstance()
        if (service != null) {
            service.startRecording()
            syncServiceState()
        } else {
            showToast("Сначала запустите сервис")
        }
    }

    fun stopRecordingManually() {
        val service = VoiceForegroundService.getInstance()
        if (service != null) {
            service.stopAndRecognize()
            syncServiceState()
        }
    }

    fun toggleAiMode() {
        val newValue = !_uiState.value.isAiModeEnabled
        _uiState.value = _uiState.value.copy(isAiModeEnabled = newValue)
        viewModelScope.launch {
            preferencesStore.saveAiEnabled(newValue)
        }
    }

    fun selectProvider(provider: AiProvider) {
        _uiState.value = _uiState.value.copy(aiProvider = provider)
        viewModelScope.launch {
            preferencesStore.saveAiProvider(provider.key)
        }
    }

    private fun syncServiceState() {
        val service = VoiceForegroundService.getInstance()
        if (service != null) {
            viewModelScope.launch(Dispatchers.Main) {
                service.voiceState.collect { state ->
                    _uiState.value = _uiState.value.copy(voiceState = state)
                }
            }
            _uiState.value = _uiState.value.copy(
                lastRecognizedText = service.lastRecognizedText.value
            )
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCleared() {
        super.onCleared()
        try { context.unregisterReceiver(stopReceiver) } catch (_: IllegalArgumentException) {}
        try { context.unregisterReceiver(stateReceiver) } catch (_: IllegalArgumentException) {}
    }
}
