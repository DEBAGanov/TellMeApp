/**
 * @file: MainViewModel.kt
 * @description: ViewModel for main screen managing service, recording, recognition and text insertion
 * @dependencies: Hilt, VoiceState, Subscription, VoiceForegroundService, AudioRecorder, RecognizeSpeechUseCase
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.ui.screen.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.TellMeUp.tellmeapp.domain.model.Subscription
import com.TellMeUp.tellmeapp.domain.model.VoiceState
import com.TellMeUp.tellmeapp.domain.usecase.RecognizeSpeechUseCase
import com.TellMeUp.tellmeapp.service.AudioRecorder
import com.TellMeUp.tellmeapp.service.StopServiceReceiver
import com.TellMeUp.tellmeapp.service.VoiceAccessibilityService
import com.TellMeUp.tellmeapp.service.VoiceForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class MainUiState(
    val isServiceActive: Boolean = false,
    val voiceState: VoiceState = VoiceState.IDLE,
    val subscription: Subscription? = null,
    val lastRecognizedText: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recognizeSpeechUseCase: RecognizeSpeechUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val audioRecorder = AudioRecorder()
    private var currentAudioFile: File? = null

    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            _uiState.value = _uiState.value.copy(isServiceActive = false)
        }
    }

    private val voiceTriggerReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            if (intent.action == VoiceAccessibilityService.ACTION_VOICE_TRIGGER) {
                onVoiceTrigger()
            }
        }
    }

    init {
        context.registerReceiver(
            stopReceiver,
            IntentFilter(StopServiceReceiver.ACTION_SERVICE_STOPPED),
            Context.RECEIVER_NOT_EXPORTED
        )
        context.registerReceiver(
            voiceTriggerReceiver,
            IntentFilter(VoiceAccessibilityService.ACTION_VOICE_TRIGGER),
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

    private fun onVoiceTrigger() {
        when (_uiState.value.voiceState) {
            VoiceState.IDLE -> startRecording()
            VoiceState.RECORDING -> stopAndRecognize()
            VoiceState.PROCESSING -> {}
        }
    }

    private fun startRecording() {
        try {
            val audioFile = File(context.cacheDir, "voice_${System.currentTimeMillis()}.wav")
            currentAudioFile = audioFile
            audioRecorder.start(audioFile)

            vibrate(shortVibration = true)

            _uiState.value = _uiState.value.copy(
                voiceState = VoiceState.RECORDING,
                lastRecognizedText = null
            )
        } catch (e: Exception) {
            showToast("Ошибка микрофона: ${e.message}")
        }
    }

    private fun stopAndRecognize() {
        vibrate(shortVibration = false)

        _uiState.value = _uiState.value.copy(voiceState = VoiceState.PROCESSING)

        viewModelScope.launch(Dispatchers.IO) {
            val audioFile = audioRecorder.stop()

            if (audioFile == null || !audioFile.exists() || audioFile.length() <= 44) {
                launch(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(voiceState = VoiceState.IDLE)
                    showToast("Ошибка: пустая запись")
                }
                return@launch
            }

            val result = recognizeSpeechUseCase(audioFile)

            launch(Dispatchers.Main) {
                if (result.isSuccess && result.text.isNotBlank()) {
                    insertRecognizedText(result.text)
                    _uiState.value = _uiState.value.copy(
                        voiceState = VoiceState.IDLE,
                        lastRecognizedText = result.text
                    )
                } else {
                    val errorCode = result.errorCode ?: "UNKNOWN"
                    showToast("Не удалось распознать [${errorCode}]")
                    _uiState.value = _uiState.value.copy(voiceState = VoiceState.IDLE)
                }
            }

            audioFile.delete()
        }
    }

    private fun insertRecognizedText(text: String) {
        val service = VoiceAccessibilityService.getInstance()
        if (service != null) {
            service.insertText(text)
        } else {
            showToast("Включите Accessibility в настройках")
        }
    }

    private fun vibrate(shortVibration: Boolean) {
        val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager)
                .defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val effect = if (shortVibration) {
            VibrationEffect.createOneShot(50, 200)
        } else {
            VibrationEffect.createOneShot(100, 255)
        }
        vibrator.vibrate(effect)
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onCleared() {
        super.onCleared()
        audioRecorder.release()
        try {
            context.unregisterReceiver(stopReceiver)
        } catch (_: IllegalArgumentException) {}
        try {
            context.unregisterReceiver(voiceTriggerReceiver)
        } catch (_: IllegalArgumentException) {}
    }
}
