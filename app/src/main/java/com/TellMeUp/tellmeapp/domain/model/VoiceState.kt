/**
 * @file: VoiceState.kt
 * @description: State model representing the voice recording status
 * @dependencies: None
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.domain.model

enum class VoiceState {
    IDLE,
    RECORDING,
    PROCESSING
}
