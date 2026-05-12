/**
 * @file: SpeechRepositoryImpl.kt
 * @description: Implementation of SpeechRepository using AquaVoiceApi with SSE streaming
 * @dependencies: AquaVoiceApi, SpeechRepository
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.data.repository

import com.TellMeUp.tellmeapp.data.remote.api.AquaVoiceApi
import com.TellMeUp.tellmeapp.domain.model.Transcription
import com.TellMeUp.tellmeapp.domain.repository.SpeechRepository
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeechRepositoryImpl @Inject constructor(
    private val api: AquaVoiceApi
) : SpeechRepository {

    override suspend fun recognizeSpeech(audioFile: File): Transcription {
        val result = api.transcribeBatch(audioFile)

        return result.fold(
            onSuccess = { response ->
                if (response.text.isBlank()) {
                    Transcription(
                        text = "",
                        isSuccess = false,
                        errorCode = "EMPTY_RESULT",
                        errorMessage = "API returned empty text"
                    )
                } else {
                    Transcription(
                        text = response.text,
                        isSuccess = true
                    )
                }
            },
            onFailure = { error ->
                val errorCode: String
                val errorMsg: String
                when (error) {
                    is AquaVoiceApi.ApiException -> {
                        errorCode = error.errorCode
                        errorMsg = "HTTP ${error.code}: ${error.message}"
                    }
                    else -> {
                        errorCode = "NETWORK_ERROR"
                        errorMsg = error.message ?: "Unknown network error"
                    }
                }
                Transcription(
                    text = "",
                    isSuccess = false,
                    errorCode = errorCode,
                    errorMessage = errorMsg
                )
            }
        )
    }

    override fun setApiKey(key: String) {
        api.setApiKey(key)
    }

    override fun hasApiKey(): Boolean {
        return api.hasApiKey()
    }
}
