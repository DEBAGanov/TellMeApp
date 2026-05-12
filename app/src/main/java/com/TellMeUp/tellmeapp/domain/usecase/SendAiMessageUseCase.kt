/**
 * @file: SendAiMessageUseCase.kt
 * @description: Use case for sending text to AI and receiving response
 * @dependencies: AiChatRepository
 * @created: 2026-05-11
 */

package com.TellMeUp.tellmeapp.domain.usecase

import com.TellMeUp.tellmeapp.domain.repository.AiChatRepository
import javax.inject.Inject

class SendAiMessageUseCase @Inject constructor(
    private val repository: AiChatRepository
) {
    suspend operator fun invoke(text: String): String? {
        return repository.sendMessage(text)
    }
}
