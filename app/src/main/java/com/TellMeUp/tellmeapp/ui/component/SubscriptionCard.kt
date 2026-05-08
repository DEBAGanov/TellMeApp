/**
 * @file: SubscriptionCard.kt
 * @description: Compact card showing subscription status on main screen
 * @dependencies: Color.kt, Subscription model
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.TellMeUp.tellmeapp.domain.model.Subscription
import com.TellMeUp.tellmeapp.ui.theme.CardDark
import com.TellMeUp.tellmeapp.ui.theme.RecordingRed
import com.TellMeUp.tellmeapp.ui.theme.AccentCyan
import com.TellMeUp.tellmeapp.ui.theme.TextSecondary
import com.TellMeUp.tellmeapp.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SubscriptionCard(
    subscription: Subscription?,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column {
            Text(
                text = "Подписка",
                color = TextSecondary,
                fontSize = 12.sp
            )
            Text(
                text = if (subscription?.isActive == true) "Активна" else "Не активна",
                color = if (subscription?.isActive == true) AccentCyan else RecordingRed,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        if (subscription?.isActive == true && subscription.expiryDate != null) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Действует до",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        .format(Date(subscription.expiryDate)),
                    color = TextTertiary,
                    fontSize = 14.sp
                )
            }
        }
    }
}
