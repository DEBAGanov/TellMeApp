/**
 * @file: ClaudeMessageDto.kt
 * @description: DTOs for Claude Messages API request/response
 * @dependencies: Kotlinx Serialization
 * @created: 2026-05-12
 */

package com.TellMeUp.tellmeapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ClaudeMessageRequest(
    val model: String,
    val max_tokens: Int = 1024,
    val system: String = "",
    val messages: List<ClaudeMessageItem>
)

@Serializable
data class ClaudeMessageItem(
    val role: String,
    val content: String
)

@Serializable
data class ClaudeMessageResponse(
    val id: String = "",
    val type: String = "",
    val role: String = "",
    val content: List<ClaudeContentBlock> = emptyList(),
    val model: String = "",
    val stop_reason: String? = null,
    val usage: ClaudeUsage? = null
)

@Serializable
data class ClaudeContentBlock(
    val type: String = "",
    val text: String = ""
)

@Serializable
data class ClaudeUsage(
    val input_tokens: Int = 0,
    val output_tokens: Int = 0
)
