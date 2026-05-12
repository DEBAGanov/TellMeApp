/**
 * @file: AiChatRepositoryImpl.kt
 * @description: Implementation of AiChatRepository using AiChatApi
 * @dependencies: AiChatApi, AiChatRepository
 * @created: 2026-05-11
 */

package com.TellMeUp.tellmeapp.data.repository

import com.TellMeUp.tellmeapp.data.remote.api.AiChatApi
import com.TellMeUp.tellmeapp.domain.repository.AiChatRepository
import com.TellMeUp.tellmeapp.util.AppLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiChatRepositoryImpl @Inject constructor(
    private val api: AiChatApi
) : AiChatRepository {

    companion object {
        private const val TAG = "AiChatRepo"
    }

    override suspend fun sendMessage(text: String): String? {
        val result = api.chat(text)
        return result.fold(
            onSuccess = { it },
            onFailure = { error ->
                AppLogger.e(TAG, "sendMessage failed: ${error.message}")
                null
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
