/**
 * @file: ChatCompletionDto.kt
 * @description: DTOs for z.ai chat completions API request/response
 * @dependencies: Kotlinx Serialization
 * @created: 2026-05-11
 */

package com.TellMeUp.tellmeapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessageDto>,
    val temperature: Double = 1.0
)

@Serializable
data class ChatMessageDto(
    val role: String,
    val content: String
)

@Serializable
data class ChatCompletionResponse(
    val choices: List<ChatChoiceDto> = emptyList()
)

@Serializable
data class ChatChoiceDto(
    val message: ChatMessageDto? = null
)
