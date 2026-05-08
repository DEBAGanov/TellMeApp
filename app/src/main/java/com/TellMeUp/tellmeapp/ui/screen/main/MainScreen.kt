/**
 * @file: MainScreen.kt
 * @description: Main screen with power button, status indicator and subscription card
 * @dependencies: PowerButton, StatusIndicator, SubscriptionCard, MainViewModel
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.ui.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.TellMeUp.tellmeapp.ui.component.PowerButton
import com.TellMeUp.tellmeapp.ui.component.StatusIndicator
import com.TellMeUp.tellmeapp.ui.component.SubscriptionCard
import com.TellMeUp.tellmeapp.ui.theme.BackgroundDark
import com.TellMeUp.tellmeapp.ui.theme.SurfaceDark
import com.TellMeUp.tellmeapp.ui.theme.TextPrimary

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    MainScreenContent(
        isServiceActive = uiState.isServiceActive,
        voiceState = uiState.voiceState,
        subscription = uiState.subscription,
        onPowerClick = viewModel::toggleService
    )
}

@Composable
private fun MainScreenContent(
    isServiceActive: Boolean,
    voiceState: com.TellMeUp.tellmeapp.domain.model.VoiceState,
    subscription: com.TellMeUp.tellmeapp.domain.model.Subscription?,
    onPowerClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundDark, SurfaceDark, BackgroundDark)
                )
            )
            .padding(24.dp)
    ) {
        Text(
            text = "TellMeApp",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            PowerButton(
                voiceState = voiceState,
                isServiceActive = isServiceActive,
                onClick = onPowerClick
            )

            Spacer(modifier = Modifier.height(32.dp))

            StatusIndicator(
                voiceState = voiceState,
                isServiceActive = isServiceActive
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SubscriptionCard(
                subscription = subscription,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
