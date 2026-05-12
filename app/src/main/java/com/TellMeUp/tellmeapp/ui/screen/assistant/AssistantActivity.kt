/**
 * @file: AssistantActivity.kt
 * @description: Transparent activity launched by power button assistant gesture
 * @dependencies: AssistantOverlay, VoiceForegroundService
 * @created: 2026-05-09
 */

package com.TellMeUp.tellmeapp.ui.screen.assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.TellMeUp.tellmeapp.service.VoiceForegroundService
import com.TellMeUp.tellmeapp.ui.theme.TellMeAppTheme
import com.TellMeUp.tellmeapp.util.AppLogger
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AssistantActivity : ComponentActivity() {

    companion object {
        private const val TAG = "AssistantActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogger.i(TAG, "Launched by power button / assistant gesture")

        VoiceForegroundService.start(this)

        setContent {
            TellMeAppTheme(darkTheme = true) {
                AssistantOverlay(
                    onFinish = {
                        AppLogger.d(TAG, "Overlay finished, closing activity")
                        finish()
                    }
                )
            }
        }
    }

    override fun onPause() {
        stopRecordingIfNeeded()
        super.onPause()
        finish()
    }

    private fun stopRecordingIfNeeded() {
        val service = VoiceForegroundService.getInstance()
        if (service != null && service.voiceState.value == com.TellMeUp.tellmeapp.domain.model.VoiceState.RECORDING) {
            AppLogger.w(TAG, "Activity closing while recording — stopping")
            service.stopAndRecognize()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }
}
