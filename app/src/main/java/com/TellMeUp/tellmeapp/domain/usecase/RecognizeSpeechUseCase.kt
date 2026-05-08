/**
 * @file: RecognizeSpeechUseCase.kt
 * @description: Use case for speech recognition via AquaVoice API
 * @dependencies: SpeechRepository
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.domain.usecase

import com.TellMeUp.tellmeapp.domain.model.Transcription
import com.TellMeUp.tellmeapp.domain.repository.SpeechRepository
import java.io.File
import javax.inject.Inject

class RecognizeSpeechUseCase @Inject constructor(
    private val speechRepository: SpeechRepository
) {
    suspend operator fun invoke(audioFile: File): Transcription {
        return speechRepository.recognizeSpeech(audioFile)
    }
}
