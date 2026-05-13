/**
 * @file: MainScreen.kt
 * @description: Main screen with service control, assistant setup and manual recording
 * @dependencies: MainViewModel
 * @created: 2026-05-09
 */

package com.TellMeUp.tellmeapp.ui.screen.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.TellMeUp.tellmeapp.domain.model.AiProvider
import com.TellMeUp.tellmeapp.domain.model.VoiceState
import com.TellMeUp.tellmeapp.ui.theme.AccentBlue
import com.TellMeUp.tellmeapp.ui.theme.BackgroundDark
import com.TellMeUp.tellmeapp.ui.theme.CardDark
import com.TellMeUp.tellmeapp.ui.theme.RecordingRed
import com.TellMeUp.tellmeapp.ui.theme.SurfaceDark
import com.TellMeUp.tellmeapp.ui.theme.TextPrimary
import com.TellMeUp.tellmeapp.ui.theme.TextSecondary
import com.TellMeUp.tellmeapp.ui.theme.TextTertiary

@Composable
fun MainScreen(
    onNavigateToProvider: (AiProvider) -> Unit = {},
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasMicPermission = perms[Manifest.permission.RECORD_AUDIO] == true
        if (hasMicPermission) {
            viewModel.onPermissionsGranted()
            Toast.makeText(context, "Разрешение получено", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasMicPermission) {
            permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.POST_NOTIFICATIONS))
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = listOf(BackgroundDark, SurfaceDark, BackgroundDark)))
            .padding(24.dp)
    ) {
        Text(
            text = "TellMeApp",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 24.dp)
        )

        // Permission status
        if (!hasMicPermission) {
            StatusCard(
                text = "Нет разрешения на микрофон",
                buttonText = "Разрешить",
                onClick = { permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO)) },
                isWarning = true
            )
        } else {
            // Service toggle
            Button(
                onClick = {
                    Toast.makeText(context, "Переключение сервиса...", Toast.LENGTH_SHORT).show()
                    viewModel.toggleService()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isServiceActive) RecordingRed else AccentBlue
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(
                    text = if (uiState.isServiceActive) "СТОП СЕРВИС" else "СТАРТ СЕРВИС",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        // Service status
        Text(
            text = when {
                !hasMicPermission -> "Нет разрешения"
                uiState.isServiceActive -> "Сервис активен"
                else -> "Сервис остановлен"
            },
            color = if (uiState.isServiceActive) AccentBlue else TextTertiary,
            fontSize = 14.sp
        )

        // Setup instructions when service is active
        if (uiState.isServiceActive && hasMicPermission) {
            Spacer(modifier = Modifier.height(4.dp))

            // Step 1: Assistant setup
            StepCard(
                step = "1",
                title = "Кнопка питания",
                description = "Зажмите кнопку питания\n→ TellMeApp запишет голос\n→ Тапните чтобы остановить",
                buttonText = "Настройки ассистента",
                onClick = {
                    try {
                        context.startActivity(Intent(Settings.ACTION_VOICE_INPUT_SETTINGS))
                    } catch (_: Exception) {
                        context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            // AI mode toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardDark)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI ассистент",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Обработка текста через ${uiState.aiProvider.displayName}",
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = uiState.isAiModeEnabled,
                    onCheckedChange = { viewModel.toggleAiMode() },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = AccentBlue,
                        checkedThumbColor = TextPrimary,
                        uncheckedTrackColor = TextTertiary.copy(alpha = 0.3f),
                        uncheckedThumbColor = TextSecondary
                    )
                )
            }

            // Provider selector (visible when AI is enabled)
            if (uiState.isAiModeEnabled) {
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardDark)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AiProvider.entries.forEach { provider ->
                        val isSelected = uiState.aiProvider == provider
                        Button(
                            onClick = {
                                viewModel.selectProvider(provider)
                                onNavigateToProvider(provider)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) AccentBlue else CardDark
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Text(
                                text = provider.displayName,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Result text
        val resultText = uiState.lastRecognizedText
        if (resultText != null) {
            Text(
                text = resultText,
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardDark)
                    .padding(12.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StepCard(
    step: String,
    title: String,
    description: String,
    buttonText: String?,
    onClick: (() -> Unit)?
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
            text = "$step. $title",
            color = AccentBlue,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            color = TextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )
        if (buttonText != null && onClick != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(buttonText, fontWeight = FontWeight.Medium, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun StatusCard(
    text: String,
    buttonText: String,
    onClick: () -> Unit,
    isWarning: Boolean = false
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
            color = if (isWarning) RecordingRed else TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
            shape = RoundedCornerShape(8.dp)
        ) { Text(buttonText, fontWeight = FontWeight.Medium) }
    }
}
