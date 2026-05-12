/**
 * @file: AppLogger.kt
 * @description: In-app logger for debugging — stores log entries in StateFlow for UI display
 * @dependencies: None
 * @created: 2026-05-10
 */

package com.TellMeUp.tellmeapp.util

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class LogLevel(val label: String) {
    DEBUG("D"),
    INFO("I"),
    WARN("W"),
    ERROR("E")
}

data class LogEntry(
    val timestamp: Long,
    val level: LogLevel,
    val tag: String,
    val message: String
)

object AppLogger {

    private const val MAX_ENTRIES = 500

    private val _entries = MutableStateFlow<List<LogEntry>>(emptyList())
    val entries: StateFlow<List<LogEntry>> = _entries.asStateFlow()

    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())

    private fun addEntry(level: LogLevel, tag: String, message: String) {
        val entry = LogEntry(System.currentTimeMillis(), level, tag, message)
        val current = _entries.value
        val updated = if (current.size >= MAX_ENTRIES) {
            current.takeLast(MAX_ENTRIES - 1) + entry
        } else {
            current + entry
        }
        _entries.value = updated
    }

    fun d(tag: String, message: String) {
        Log.d(tag, message)
        addEntry(LogLevel.DEBUG, tag, message)
    }

    fun i(tag: String, message: String) {
        Log.i(tag, message)
        addEntry(LogLevel.INFO, tag, message)
    }

    fun w(tag: String, message: String) {
        Log.w(tag, message)
        addEntry(LogLevel.WARN, tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) Log.e(tag, message, throwable) else Log.e(tag, message)
        addEntry(LogLevel.ERROR, tag, message)
    }

    fun clear() {
        _entries.value = emptyList()
    }

    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }
}
