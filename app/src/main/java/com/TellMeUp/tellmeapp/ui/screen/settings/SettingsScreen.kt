/**
 * @file: SettingsScreen.kt
 * @description: Settings screen with API key input, toggles for vibration/visual/theme
 * @dependencies: SettingsViewModel
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.ui.screen.settings

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.TellMeUp.tellmeapp.ui.theme.AccentBlue
import com.TellMeUp.tellmeapp.ui.theme.CardDark
import com.TellMeUp.tellmeapp.ui.theme.SurfaceDark
import com.TellMeUp.tellmeapp.ui.theme.TextPrimary
import com.TellMeUp.tellmeapp.ui.theme.TextSecondary
import com.TellMeUp.tellmeapp.ui.theme.TextTertiary

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsScreenContent(
        uiState = uiState,
        onApiKeyChanged = viewModel::onApiKeyChanged,
        onSaveApiKey = viewModel::saveApiKey,
        onAiApiKeyChanged = viewModel::onAiApiKeyChanged,
        onSaveAiApiKey = viewModel::saveAiApiKey,
        onClaudeApiKeyChanged = viewModel::onClaudeApiKeyChanged,
        onSaveClaudeApiKey = viewModel::saveClaudeApiKey,
        onVibrationChanged = viewModel::setVibrationEnabled,
        onVisualNotificationChanged = viewModel::setVisualNotificationEnabled,
        onDarkThemeChanged = viewModel::setDarkTheme
    )
}

@Composable
private fun SettingsScreenContent(
    uiState: SettingsUiState,
    onApiKeyChanged: (String) -> Unit,
    onSaveApiKey: () -> Unit,
    onAiApiKeyChanged: (String) -> Unit,
    onSaveAiApiKey: () -> Unit,
    onClaudeApiKeyChanged: (String) -> Unit,
    onSaveClaudeApiKey: () -> Unit,
    onVibrationChanged: (Boolean) -> Unit,
    onVisualNotificationChanged: (Boolean) -> Unit,
    onDarkThemeChanged: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Настройки",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        SectionHeader("API")

        OutlinedTextField(
            value = uiState.apiKey,
            onValueChange = onApiKeyChanged,
            label = { Text("API ключ AquaVoice") },
            placeholder = { Text("sk-...") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = TextTertiary,
                focusedLabelColor = AccentBlue,
                unfocusedLabelColor = TextTertiary,
                cursorColor = AccentBlue,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onSaveApiKey,
            enabled = uiState.apiKey.isNotBlank() && !uiState.isApiKeySaved,
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue,
                disabledContainerColor = AccentBlue.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
        ) {
            Text(
                text = if (uiState.isApiKeySaved) "Сохранено" else "Сохранить ключ",
                fontWeight = FontWeight.Medium
            )
        }

        HorizontalDivider(color = TextTertiary.copy(alpha = 0.3f))

        SectionHeader("AI ассистент")

        OutlinedTextField(
            value = uiState.aiApiKey,
            onValueChange = onAiApiKeyChanged,
            label = { Text("API ключ z.ai") },
            placeholder = { Text("Bearer token...") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = TextTertiary,
                focusedLabelColor = AccentBlue,
                unfocusedLabelColor = TextTertiary,
                cursorColor = AccentBlue,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onSaveAiApiKey,
            enabled = uiState.aiApiKey.isNotBlank() && !uiState.isAiApiKeySaved,
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue,
                disabledContainerColor = AccentBlue.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
        ) {
            Text(
                text = if (uiState.isAiApiKeySaved) "Сохранено" else "Сохранить z.ai ключ",
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = uiState.claudeApiKey,
            onValueChange = onClaudeApiKeyChanged,
            label = { Text("API ключ Claude") },
            placeholder = { Text("sk-ant-...") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = TextTertiary,
                focusedLabelColor = AccentBlue,
                unfocusedLabelColor = TextTertiary,
                cursorColor = AccentBlue,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = onSaveClaudeApiKey,
            enabled = uiState.claudeApiKey.isNotBlank() && !uiState.isClaudeApiKeySaved,
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue,
                disabledContainerColor = AccentBlue.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
        ) {
            Text(
                text = if (uiState.isClaudeApiKeySaved) "Сохранено" else "Сохранить Claude ключ",
                fontWeight = FontWeight.Medium
            )
        }

        HorizontalDivider(color = TextTertiary.copy(alpha = 0.3f))

        SettingsToggle(
            title = "Виброотклик",
            description = "Вибрация при старте и остановке записи",
            checked = uiState.isVibrationEnabled,
            onCheckedChange = onVibrationChanged
        )

        SettingsToggle(
            title = "Визуальное уведомление",
            description = "Показывать индикатор при записи",
            checked = uiState.isVisualNotificationEnabled,
            onCheckedChange = onVisualNotificationChanged
        )

        HorizontalDivider(color = TextTertiary.copy(alpha = 0.3f))

        SectionHeader("Внешний вид")

        SettingsToggle(
            title = "Тёмная тема",
            description = "Тёмный фон и голубые акценты",
            checked = uiState.isDarkTheme,
            onCheckedChange = onDarkThemeChanged
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = AccentBlue,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun SettingsToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = AccentBlue,
                checkedThumbColor = TextPrimary,
                uncheckedTrackColor = TextTertiary.copy(alpha = 0.3f),
                uncheckedThumbColor = TextSecondary
            )
        )
    }
}
