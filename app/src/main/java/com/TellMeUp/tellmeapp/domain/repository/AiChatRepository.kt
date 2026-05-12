/**
 * @file: AiChatRepository.kt
 * @description: Repository interface for AI chat interactions
 * @dependencies: Domain layer
 * @created: 2026-05-11
 */

package com.TellMeUp.tellmeapp.domain.repository

interface AiChatRepository {
    suspend fun sendMessage(text: String): String?
    fun setApiKey(key: String)
    fun hasApiKey(): Boolean
}
