/**
 * @file: SubscriptionScreen.kt
 * @description: Subscription activation screen with link input and status display
 * @dependencies: SubscriptionViewModel, SubscriptionCard
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.ui.screen.subscription

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.TellMeUp.tellmeapp.ui.component.SubscriptionCard
import com.TellMeUp.tellmeapp.ui.theme.AccentBlue
import com.TellMeUp.tellmeapp.ui.theme.BackgroundDark
import com.TellMeUp.tellmeapp.ui.theme.CardDark
import com.TellMeUp.tellmeapp.ui.theme.RecordingRed
import com.TellMeUp.tellmeapp.ui.theme.SurfaceDark
import com.TellMeUp.tellmeapp.ui.theme.TextPrimary
import com.TellMeUp.tellmeapp.ui.theme.TextSecondary
import com.TellMeUp.tellmeapp.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SubscriptionScreen(
    viewModel: SubscriptionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    SubscriptionScreenContent(
        uiState = uiState,
        onLinkChanged = viewModel::onLinkChanged,
        onActivate = viewModel::activate
    )
}

@Composable
private fun SubscriptionScreenContent(
    uiState: SubscriptionUiState,
    onLinkChanged: (String) -> Unit,
    onActivate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Подписка",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp)
        )

        if (uiState.subscription != null) {
            SubscriptionCard(
                subscription = uiState.subscription,
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.subscription.isActive && uiState.subscription.expiryDate != null) {
                ActiveSubscriptionInfo(
                    subscription = uiState.subscription
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Активация",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = "Вставьте ссылку-ключ для активации подписки",
            color = TextSecondary,
            fontSize = 14.sp
        )

        OutlinedTextField(
            value = uiState.activationLink,
            onValueChange = onLinkChanged,
            label = { Text("Ссылка активации") },
            placeholder = { Text("https://s.axolab.org/...") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
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

        AnimatedVisibility(visible = uiState.error != null) {
            Text(
                text = uiState.error ?: "",
                color = RecordingRed,
                fontSize = 13.sp
            )
        }

        Button(
            onClick = onActivate,
            enabled = !uiState.isLoading && uiState.activationLink.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue,
                disabledContainerColor = AccentBlue.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = TextPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.height(24.dp)
                )
            } else {
                Text(
                    text = if (uiState.subscription?.isActive == true) "Обновить подписку" else "Активировать",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ActiveSubscriptionInfo(
    subscription: com.TellMeUp.tellmeapp.domain.model.Subscription
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .padding(16.dp)
    ) {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("ru"))
        val daysLeft = subscription.expiryDate?.let {
            ((it - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
        } ?: 0

        InfoRow("Тариф", subscription.tariffType ?: "—")
        InfoRow("Действует до", subscription.expiryDate?.let {
            dateFormat.format(Date(it))
        } ?: "—")
        InfoRow("Осталось дней", "$daysLeft")
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, color = TextSecondary, fontSize = 12.sp)
        Text(text = value, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}
