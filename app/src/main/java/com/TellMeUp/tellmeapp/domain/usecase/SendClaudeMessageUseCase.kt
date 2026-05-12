/**
 * @file: SendClaudeMessageUseCase.kt
 * @description: Use case for sending messages to Claude API
 * @dependencies: ClaudeRepository
 * @created: 2026-05-12
 */

package com.TellMeUp.tellmeapp.domain.usecase

import com.TellMeUp.tellmeapp.domain.repository.ClaudeRepository
import javax.inject.Inject

class SendClaudeMessageUseCase @Inject constructor(
    private val repository: ClaudeRepository
) {
    suspend operator fun invoke(text: String): String? = repository.sendMessage(text)
}
