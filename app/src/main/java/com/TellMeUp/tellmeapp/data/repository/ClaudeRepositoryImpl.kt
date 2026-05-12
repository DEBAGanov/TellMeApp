/**
 * @file: ClaudeRepositoryImpl.kt
 * @description: Implementation of ClaudeRepository using ClaudeApi
 * @dependencies: ClaudeApi, ClaudeRepository
 * @created: 2026-05-12
 */

package com.TellMeUp.tellmeapp.data.repository

import com.TellMeUp.tellmeapp.data.remote.api.ClaudeApi
import com.TellMeUp.tellmeapp.domain.repository.ClaudeRepository
import com.TellMeUp.tellmeapp.util.AppLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClaudeRepositoryImpl @Inject constructor(
    private val api: ClaudeApi
) : ClaudeRepository {

    companion object {
        private const val TAG = "ClaudeRepo"
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
