/**
 * @file: ClaudeApi.kt
 * @description: HTTP client for Claude Messages API
 * @dependencies: OkHttp, Kotlinx Serialization
 * @created: 2026-05-12
 */

package com.TellMeUp.tellmeapp.data.remote.api

import com.TellMeUp.tellmeapp.data.remote.dto.ClaudeMessageItem
import com.TellMeUp.tellmeapp.data.remote.dto.ClaudeMessageRequest
import com.TellMeUp.tellmeapp.data.remote.dto.ClaudeMessageResponse
import com.TellMeUp.tellmeapp.util.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClaudeApi @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json
) {
    companion object {
        private const val TAG = "ClaudeApi"
        private const val BASE_URL = "https://api.anthropic.com"
        private const val MESSAGES_ENDPOINT = "$BASE_URL/v1/messages"
        private const val MODEL = "claude-sonnet-4-20250514"
        private const val SYSTEM_PROMPT = "You are a helpful AI assistant. Respond concisely in the same language the user writes."
        private const val JSON_MEDIA_TYPE = "application/json; charset=utf-8"
        private const val ANTHROPIC_VERSION = "2023-06-01"
    }

    private var apiKey: String = ""

    fun setApiKey(key: String) {
        apiKey = key
    }

    fun hasApiKey(): Boolean = apiKey.isNotBlank()

    suspend fun chat(userText: String): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val keyPrefix = if (apiKey.length > 8) apiKey.take(8) + "..." else "EMPTY"
                AppLogger.d(TAG, "Chat request: model=$MODEL key=$keyPrefix textLen=${userText.length}")

                val request = ClaudeMessageRequest(
                    model = MODEL,
                    system = SYSTEM_PROMPT,
                    messages = listOf(
                        ClaudeMessageItem(role = "user", content = userText)
                    )
                )

                val body = json.encodeToString(ClaudeMessageRequest.serializer(), request)
                    .toRequestBody(JSON_MEDIA_TYPE.toMediaType())

                val httpRequest = Request.Builder()
                    .url(MESSAGES_ENDPOINT)
                    .addHeader("x-api-key", apiKey)
                    .addHeader("anthropic-version", ANTHROPIC_VERSION)
                    .addHeader("content-type", "application/json")
                    .post(body)
                    .build()

                val response = client.newCall(httpRequest).execute()
                AppLogger.d(TAG, "Chat response: HTTP ${response.code}")

                if (!response.isSuccessful) {
                    val errorBody = response.body?.string()
                    AppLogger.e(TAG, "Chat error: HTTP ${response.code}, body: $errorBody")
                    return@withContext Result.failure(
                        IOException("HTTP ${response.code}: $errorBody")
                    )
                }

                val responseBody = response.body?.string() ?: return@withContext Result.failure(
                    IOException("Empty response body")
                )

                AppLogger.d(TAG, "Chat response body: $responseBody")

                val messageResponse = json.decodeFromString<ClaudeMessageResponse>(responseBody)
                val answer = messageResponse.content.firstOrNull()?.text

                if (answer.isNullOrBlank()) {
                    AppLogger.w(TAG, "Chat: empty answer from Claude")
                    return@withContext Result.failure(IOException("Claude returned empty response"))
                }

                AppLogger.i(TAG, "Chat success: answer='$answer'")
                Result.success(answer)
            } catch (e: Exception) {
                AppLogger.e(TAG, "Chat exception: ${e.javaClass.simpleName}: ${e.message}")
                Result.failure(e)
            }
        }
}
