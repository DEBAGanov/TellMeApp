/**
 * @file: MainViewModel.kt
 * @description: ViewModel for main screen managing service state and voice state
 * @dependencies: Hilt, VoiceState, Subscription, VoiceForegroundService
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.ui.screen.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.ViewModel
import com.TellMeUp.tellmeapp.domain.model.Subscription
import com.TellMeUp.tellmeapp.domain.model.VoiceState
import com.TellMeUp.tellmeapp.service.StopServiceReceiver
import com.TellMeUp.tellmeapp.service.VoiceForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            _uiState.value = _uiState.value.copy(isServiceActive = false)
        }
    }

    init {
        context.registerReceiver(
            stopReceiver,
            IntentFilter(StopServiceReceiver.ACTION_SERVICE_STOPPED),
            Context.RECEIVER_NOT_EXPORTED
        )
    }

    fun toggleService() {
        if (_uiState.value.isServiceActive) {
            VoiceForegroundService.stop(context)
            _uiState.value = _uiState.value.copy(isServiceActive = false)
        } else {
            VoiceForegroundService.start(context)
            _uiState.value = _uiState.value.copy(isServiceActive = true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            context.unregisterReceiver(stopReceiver)
        } catch (_: IllegalArgumentException) {}
    }
}
