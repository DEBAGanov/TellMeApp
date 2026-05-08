/**
 * @file: StatusIndicator.kt
 * @description: Text indicator showing current voice/service status
 * @dependencies: Color.kt, VoiceState
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.ui.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.TellMeUp.tellmeapp.domain.model.VoiceState
import com.TellMeUp.tellmeapp.ui.theme.AccentBlue
import com.TellMeUp.tellmeapp.ui.theme.RecordingRed
import com.TellMeUp.tellmeapp.ui.theme.TextSecondary
import com.TellMeUp.tellmeapp.ui.theme.TextTertiary

@Composable
fun StatusIndicator(
    voiceState: VoiceState,
    isServiceActive: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "dotsTransition")
    val dotsCount by transition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "dots"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        val (statusText, statusColor) = when (voiceState) {
            VoiceState.RECORDING -> {
                val dots = ".".repeat((dotsCount.toInt() % 4).coerceAtLeast(1))
                "Запись$dots" to RecordingRed
            }
            VoiceState.PROCESSING -> {
                val dots = ".".repeat((dotsCount.toInt() % 4).coerceAtLeast(1))
                "Обработка$dots" to AccentBlue
            }
            VoiceState.IDLE -> {
                if (isServiceActive) "Готов к работе" to AccentBlue
                else "Сервис остановлен" to TextTertiary
            }
        }

        Text(
            text = statusText,
            color = statusColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isServiceActive) "Зажмите Volume Up для записи" else "Нажмите для запуска",
            color = TextSecondary,
            fontSize = 13.sp
        )

        if (voiceState == VoiceState.PROCESSING) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = AccentBlue,
                strokeWidth = 2.dp
            )
        }
    }
}
