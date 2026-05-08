/**
 * @file: Transcription.kt
 * @description: Domain model for speech transcription result
 * @dependencies: None
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.domain.model

data class Transcription(
    val text: String,
    val isSuccess: Boolean,
    val errorCode: String? = null
)
