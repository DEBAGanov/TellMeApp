/**
 * @file: AquaVoiceApi.kt
 * @description: HTTP client for AquaVoice API with SSE streaming support
 * @dependencies: OkHttp, Kotlinx Serialization
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.data.remote.api

import com.TellMeUp.tellmeapp.data.remote.dto.ApiError
import com.TellMeUp.tellmeapp.data.remote.dto.TranscriptionResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.BufferedSource
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AquaVoiceApi @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json
) {
    companion object {
        private const val BASE_URL = "https://api.aqua.sh/v1"
        private const val TRANSCRIPTIONS_ENDPOINT = "$BASE_URL/audio/transcriptions"
        private const val MODEL = "avalon-1"
        private const val LANGUAGE = "ru"
        private const val AUDIO_MEDIA_TYPE = "audio/wav"
    }

    private var apiKey: String = ""

    fun setApiKey(key: String) {
        apiKey = key
    }

    fun hasApiKey(): Boolean = apiKey.isNotBlank()

    suspend fun transcribeBatch(audioFile: File): Result<TranscriptionResponse> =
        withContext(Dispatchers.IO) {
            try {
                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("model", MODEL)
                    .addFormDataPart("language", LANGUAGE)
                    .addFormDataPart(
                        "file",
                        audioFile.name,
                        audioFile.asRequestBody(AUDIO_MEDIA_TYPE.toMediaType())
                    )
                    .build()

                val request = buildRequest(requestBody)
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    val errorBody = response.body?.string()
                    val error = parseError(errorBody)
                    return@withContext Result.failure(
                        ApiException(
                            code = response.code,
                            message = error?.error?.message ?: "Unknown error",
                            errorCode = error?.error?.code ?: "UNKNOWN"
                        )
                    )
                }

                val body = response.body?.string() ?: return@withContext Result.failure(
                    ApiException(0, "Empty response", "EMPTY_RESPONSE")
                )

                val result = json.decodeFromString<TranscriptionResponse>(body)
                Result.success(result)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    fun transcribeStream(audioFile: File): Flow<String> = callbackFlow {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("model", MODEL)
            .addFormDataPart("language", LANGUAGE)
            .addFormDataPart("stream", "true")
            .addFormDataPart(
                "file",
                audioFile.name,
                audioFile.asRequestBody(AUDIO_MEDIA_TYPE.toMediaType())
            )
            .build()

        val request = buildRequest(requestBody)

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                close(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string()
                    val error = parseError(errorBody)
                    close(
                        ApiException(
                            code = response.code,
                            message = error?.error?.message ?: "Unknown error",
                            errorCode = error?.error?.code ?: "UNKNOWN"
                        )
                    )
                    return
                }

                try {
                    val source = response.body?.source() ?: return
                    val fullText = StringBuilder()

                    while (!source.exhausted()) {
                        val line = source.readUtf8Line() ?: continue
                        if (!line.startsWith("data: ")) continue

                        val data = line.removePrefix("data: ").trim()
                        if (data == "[DONE]") break

                        try {
                            val chunk = json.decodeFromString<TranscriptionResponse>(data)
                            if (chunk.text.isNotEmpty()) {
                                fullText.append(chunk.text)
                                trySend(chunk.text)
                            }
                        } catch (_: Exception) {}
                    }

                    close()
                } catch (e: Exception) {
                    close(e)
                }
            }
        })

        awaitClose()
    }

    private fun buildRequest(body: okhttp3.RequestBody): Request {
        return Request.Builder()
            .url(TRANSCRIPTIONS_ENDPOINT)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()
    }

    private fun parseError(body: String?): ApiError? {
        return try {
            body?.let { json.decodeFromString<ApiError>(it) }
        } catch (_: Exception) {
            null
        }
    }

    data class ApiException(
        val code: Int,
        override val message: String,
        val errorCode: String
    ) : Exception(message)
}
