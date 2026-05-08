/**
 * @file: PowerButton.kt
 * @description: Central circular power button with glow effect (Happ-style)
 * @dependencies: Color.kt, VoiceState
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.ui.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.TellMeUp.tellmeapp.R
import com.TellMeUp.tellmeapp.domain.model.VoiceState
import com.TellMeUp.tellmeapp.ui.theme.ActiveGlow
import com.TellMeUp.tellmeapp.ui.theme.AccentBlue
import com.TellMeUp.tellmeapp.ui.theme.AccentCyan
import com.TellMeUp.tellmeapp.ui.theme.IdleGray
import com.TellMeUp.tellmeapp.ui.theme.RecordingRed
import com.TellMeUp.tellmeapp.ui.theme.RecordingRedGlow
import com.TellMeUp.tellmeapp.ui.theme.SurfaceDark

@Composable
fun PowerButton(
    voiceState: VoiceState,
    isServiceActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp
) {
    val transition = rememberInfiniteTransition(label = "powerButtonTransition")

    val pulseAlpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val glowRadius by transition.animateFloat(
        initialValue = 20f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowRadius"
    )

    val (backgroundColor, glowColor) = when {
        voiceState == VoiceState.RECORDING -> RecordingRed to RecordingRedGlow
        voiceState == VoiceState.PROCESSING -> AccentBlue to ActiveGlow
        isServiceActive -> AccentBlue to ActiveGlow
        else -> IdleGray to Color.Transparent
    }

    val buttonGradient = Brush.radialGradient(
        colors = listOf(backgroundColor, backgroundColor.copy(alpha = 0.7f)),
        radius = size.value / 2
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        if (voiceState == VoiceState.RECORDING || isServiceActive) {
            Box(
                modifier = Modifier
                    .size(size + 40.dp)
                    .blur(30.dp)
                    .graphicsLayer { alpha = if (voiceState == VoiceState.RECORDING) pulseAlpha else 0.3f }
                    .background(glowColor, CircleShape)
            )
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size)
                .shadow(
                    elevation = if (isServiceActive || voiceState != VoiceState.IDLE) glowRadius.dp else 0.dp,
                    shape = CircleShape,
                    ambientColor = glowColor,
                    spotColor = glowColor
                )
                .background(buttonGradient, CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_lock_power_off),
                contentDescription = "Power",
                tint = Color.White,
                modifier = Modifier.size(size * 0.35f)
            )
        }
    }
}
