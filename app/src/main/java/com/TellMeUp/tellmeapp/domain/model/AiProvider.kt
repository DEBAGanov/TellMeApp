/**
 * @file: AiProvider.kt
 * @description: Enum for AI provider selection (ZAI or Claude)
 * @dependencies: None
 * @created: 2026-05-12
 */

package com.TellMeUp.tellmeapp.domain.model

enum class AiProvider(val key: String, val displayName: String) {
    ZAI("zai", "z.ai"),
    CLAUDE("claude", "Claude");

    companion object {
        fun fromKey(key: String): AiProvider =
            entries.find { it.key == key } ?: ZAI
    }
}
