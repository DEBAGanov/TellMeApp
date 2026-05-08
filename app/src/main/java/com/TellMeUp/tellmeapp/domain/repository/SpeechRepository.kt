/**
 * @file: SpeechRepository.kt
 * @description: Domain interface for speech recognition
 * @dependencies: Transcription model
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.domain.repository

import com.TellMeUp.tellmeapp.domain.model.Transcription
import java.io.File

interface SpeechRepository {
    suspend fun recognizeSpeech(audioFile: File): Transcription
    fun setApiKey(key: String)
    fun hasApiKey(): Boolean
}
