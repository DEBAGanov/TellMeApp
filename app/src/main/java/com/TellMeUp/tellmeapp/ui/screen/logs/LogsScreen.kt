/**
 * @file: LogsScreen.kt
 * @description: In-app log viewer for debugging volume button trigger and recording pipeline
 * @dependencies: AppLogger
 * @created: 2026-05-10
 */

package com.TellMeUp.tellmeapp.ui.screen.logs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.TellMeUp.tellmeapp.ui.theme.AccentBlue
import com.TellMeUp.tellmeapp.ui.theme.BackgroundDark
import com.TellMeUp.tellmeapp.ui.theme.CardDark
import com.TellMeUp.tellmeapp.ui.theme.RecordingRed
import com.TellMeUp.tellmeapp.ui.theme.SurfaceDark
import com.TellMeUp.tellmeapp.ui.theme.TextPrimary
import com.TellMeUp.tellmeapp.ui.theme.TextSecondary
import com.TellMeUp.tellmeapp.ui.theme.TextTertiary
import com.TellMeUp.tellmeapp.util.AppLogger
import com.TellMeUp.tellmeapp.util.LogEntry
import com.TellMeUp.tellmeapp.util.LogLevel

@Composable
fun LogsScreen() {
    val entries by AppLogger.entries.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    LaunchedEffect(entries.size) {
        if (entries.isNotEmpty()) {
            listState.scrollToItem(entries.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
    ) {
        Text(
            text = "Логи",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { AppLogger.clear() },
                colors = ButtonDefaults.buttonColors(containerColor = CardDark),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Очистить", color = TextSecondary, fontSize = 13.sp)
            }
            Button(
                onClick = {
                    val text = entries.joinToString("\n") { e ->
                        "${AppLogger.formatTime(e.timestamp)} ${e.level.label} ${e.tag}: ${e.message}"
                    }
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("logs", text))
                    Toast.makeText(context, "Логи скопированы", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CardDark),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Копировать", color = AccentBlue, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (entries.isEmpty()) {
            Text(
                text = "Нет логов.\nЗапустите сервис и нажмите Volume Up.",
                color = TextTertiary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 24.dp).align(Alignment.CenterHorizontally)
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceDark)
            ) {
                items(entries) { entry ->
                    LogEntryRow(entry)
                    HorizontalDivider(color = CardDark, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun LogEntryRow(entry: LogEntry) {
    val levelColor = when (entry.level) {
        LogLevel.DEBUG -> TextTertiary
        LogLevel.INFO -> AccentBlue
        LogLevel.WARN -> androidx.compose.ui.graphics.Color(0xFFFFA500)
        LogLevel.ERROR -> RecordingRed
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = AppLogger.formatTime(entry.timestamp),
            color = TextTertiary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(end = 6.dp)
        )
        Text(
            text = entry.level.label,
            color = levelColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(end = 6.dp)
        )
        Text(
            text = entry.tag,
            color = levelColor,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(end = 6.dp)
        )
        Text(
            text = entry.message,
            color = TextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
