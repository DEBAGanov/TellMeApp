/**
 * @file: MainScreen.kt
 * @description: Main screen with permissions, accessibility check, power button and status
 * @dependencies: PowerButton, StatusIndicator, SubscriptionCard, MainViewModel
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.ui.screen.main

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.TellMeUp.tellmeapp.service.VoiceAccessibilityService
import com.TellMeUp.tellmeapp.ui.component.PowerButton
import com.TellMeUp.tellmeapp.ui.component.StatusIndicator
import com.TellMeUp.tellmeapp.ui.component.SubscriptionCard
import com.TellMeUp.tellmeapp.ui.theme.AccentBlue
import com.TellMeUp.tellmeapp.ui.theme.BackgroundDark
import com.TellMeUp.tellmeapp.ui.theme.CardDark
import com.TellMeUp.tellmeapp.ui.theme.RecordingRed
import com.TellMeUp.tellmeapp.ui.theme.SurfaceDark
import com.TellMeUp.tellmeapp.ui.theme.TextPrimary
import com.TellMeUp.tellmeapp.ui.theme.TextSecondary

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var hasAudioPermission by remember { mutableStateOf(false) }
    var hasNotificationPermission by remember { mutableStateOf(false) }
    var isAccessibilityEnabled by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasAudioPermission = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        hasNotificationPermission = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false
        if (hasAudioPermission) {
            viewModel.onPermissionsGranted()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        )
    }

    LaunchedEffect(Unit) {
        isAccessibilityEnabled = checkAccessibilityEnabled(context)
    }

    MainScreenContent(
        uiState = uiState,
        hasAudioPermission = hasAudioPermission,
        isAccessibilityEnabled = isAccessibilityEnabled,
        onPowerClick = {
            if (hasAudioPermission) {
                viewModel.toggleService()
            } else {
                permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
            }
        },
        onEnableAccessibility = {
            context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        },
        onGrantPermission = {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            )
        }
    )
}

private fun checkAccessibilityEnabled(context: Context): Boolean {
    val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
    val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
    return enabledServices.any {
        it.resolveInfo.serviceInfo.packageName == context.packageName
    }
}

@Composable
private fun MainScreenContent(
    uiState: MainUiState,
    hasAudioPermission: Boolean,
    isAccessibilityEnabled: Boolean,
    onPowerClick: () -> Unit,
    onEnableAccessibility: () -> Unit,
    onGrantPermission: () -> Unit
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

        if (!hasAudioPermission) {
            PermissionCard(
                text = "Требуется разрешение на микрофон",
                buttonText = "Разрешить",
                onClick = onGrantPermission
            )
        }

        if (!isAccessibilityEnabled && uiState.isServiceActive) {
            PermissionCard(
                text = "Включите Accessibility для работы кнопки громкости",
                buttonText = "Открыть настройки",
                onClick = onEnableAccessibility
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            PowerButton(
                voiceState = uiState.voiceState,
                isServiceActive = uiState.isServiceActive,
                onClick = onPowerClick
            )

            Spacer(modifier = Modifier.height(32.dp))

            StatusIndicator(
                voiceState = uiState.voiceState,
                isServiceActive = uiState.isServiceActive
            )

            if (uiState.lastRecognizedText != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "\"${uiState.lastRecognizedText}\"",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SubscriptionCard(
                subscription = uiState.subscription,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PermissionCard(
    text: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .padding(16.dp)
    ) {
        Text(
            text = text,
            color = RecordingRed,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(buttonText, fontWeight = FontWeight.Medium)
        }
    }
}
