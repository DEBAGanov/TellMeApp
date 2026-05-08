/**
 * @file: TranscriptionResponse.kt
 * @description: DTO models for AquaVoice API responses
 * @dependencies: Kotlinx Serialization
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TranscriptionResponse(
    val text: String = ""
)

@Serializable
data class StreamDelta(
    val text: String = ""
)

@Serializable
data class StreamChunk(
    val choices: List<StreamChoice> = emptyList()
)

@Serializable
data class StreamChoice(
    val delta: StreamDelta = StreamDelta()
)

@Serializable
data class ApiError(
    val error: ApiErrorDetail? = null
)

@Serializable
data class ApiErrorDetail(
    val message: String = "",
    val code: String = ""
)
