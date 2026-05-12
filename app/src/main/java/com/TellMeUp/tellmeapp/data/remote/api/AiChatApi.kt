/**
 * @file: AiChatApi.kt
 * @description: HTTP client for z.ai chat completions API
 * @dependencies: OkHttp, Kotlinx Serialization
 * @created: 2026-05-11
 */

package com.TellMeUp.tellmeapp.data.remote.api

import com.TellMeUp.tellmeapp.data.remote.dto.ChatCompletionRequest
import com.TellMeUp.tellmeapp.data.remote.dto.ChatCompletionResponse
import com.TellMeUp.tellmeapp.data.remote.dto.ChatMessageDto
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
class AiChatApi @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json
) {
    companion object {
        private const val TAG = "AiChatApi"
        private const val BASE_URL = "https://api.z.ai/api/coding/paas/v4"
        private const val CHAT_ENDPOINT = "$BASE_URL/chat/completions"
        private const val MODEL = "glm-5.1"
        private const val SYSTEM_PROMPT = "You are a helpful AI assistant. Respond concisely in the same language the user writes."
        private const val JSON_MEDIA_TYPE = "application/json; charset=utf-8"
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

                val request = ChatCompletionRequest(
                    model = MODEL,
                    messages = listOf(
                        ChatMessageDto(role = "system", content = SYSTEM_PROMPT),
                        ChatMessageDto(role = "user", content = userText)
                    )
                )

                val body = json.encodeToString(ChatCompletionRequest.serializer(), request)
                    .toRequestBody(JSON_MEDIA_TYPE.toMediaType())

                val httpRequest = Request.Builder()
                    .url(CHAT_ENDPOINT)
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
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

                val chatResponse = json.decodeFromString<ChatCompletionResponse>(responseBody)
                val answer = chatResponse.choices.firstOrNull()?.message?.content

                if (answer.isNullOrBlank()) {
                    AppLogger.w(TAG, "Chat: empty answer from AI")
                    return@withContext Result.failure(IOException("AI returned empty response"))
                }

                AppLogger.i(TAG, "Chat success: answer='$answer'")
                Result.success(answer)
            } catch (e: Exception) {
                AppLogger.e(TAG, "Chat exception: ${e.javaClass.simpleName}: ${e.message}")
                Result.failure(e)
            }
        }
}
