/**
 * @file: ClaudeRepository.kt
 * @description: Repository interface for Claude Messages API
 * @dependencies: None
 * @created: 2026-05-12
 */

package com.TellMeUp.tellmeapp.domain.repository

interface ClaudeRepository {
    suspend fun sendMessage(text: String): String?
    fun setApiKey(key: String)
    fun hasApiKey(): Boolean
}
