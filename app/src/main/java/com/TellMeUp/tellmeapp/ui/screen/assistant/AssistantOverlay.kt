/**
 * @file: AssistantOverlay.kt
 * @description: Transparent overlay UI for digital assistant — auto-record, tap to stop
 * @dependencies: AssistantViewModel, VoiceState
 * @created: 2026-05-09
 */

package com.TellMeUp.tellmeapp.ui.screen.assistant

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.TellMeUp.tellmeapp.domain.model.VoiceState
import com.TellMeUp.tellmeapp.ui.theme.AccentBlue
import com.TellMeUp.tellmeapp.ui.theme.RecordingRed
import com.TellMeUp.tellmeapp.ui.theme.SurfaceDark
import com.TellMeUp.tellmeapp.ui.theme.TextPrimary
import com.TellMeUp.tellmeapp.ui.theme.TextSecondary
import com.TellMeUp.tellmeapp.ui.theme.TextTertiary
import kotlinx.coroutines.delay

@Composable
fun AssistantOverlay(
    viewModel: AssistantViewModel = hiltViewModel(),
    onFinish: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startRecording()
    }

    LaunchedEffect(uiState.voiceState) {
        if (uiState.voiceState == VoiceState.RECORDING) {
            delay(30_000)
            viewModel.stopAndRecognize()
        }
    }

    LaunchedEffect(uiState.finished) {
        if (uiState.finished) {
            delay(600)
            onFinish()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .clickable { viewModel.stopAndRecognize() }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceDark.copy(alpha = 0.95f))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (uiState.voiceState) {
                VoiceState.RECORDING -> RecordingIndicator()
                VoiceState.PROCESSING -> ProcessingIndicator("Обработка...")
                VoiceState.AI_PROCESSING -> ProcessingIndicator("AI обработка...")
                VoiceState.IDLE -> ResultIndicator(
                    recognizedText = uiState.recognizedText,
                    error = uiState.error
                )
            }
        }
    }
}

@Composable
private fun RecordingIndicator() {
    val transition = rememberInfiniteTransition(label = "pulse")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .size(64.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(RecordingRed)
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Слушаю...",
        color = TextPrimary,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Нажмите чтобы остановить",
        color = TextTertiary,
        fontSize = 13.sp
    )
}

@Composable
private fun ProcessingIndicator(label: String) {
    Text(
        text = label,
        color = AccentBlue,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium
    )
}

@Composable
private fun ResultIndicator(recognizedText: String?, error: String?) {
    when {
        recognizedText != null -> {
            Text(
                text = recognizedText,
                color = TextSecondary,
                fontSize = 15.sp
            )
        }
        error != null -> {
            Text(
                text = error,
                color = RecordingRed,
                fontSize = 14.sp
            )
        }
    }
}
