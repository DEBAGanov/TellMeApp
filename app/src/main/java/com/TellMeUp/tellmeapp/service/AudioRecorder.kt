/**
 * @file: AudioRecorder.kt
 * @description: Records audio via AudioRecord and saves to WAV format for AquaVoice API
 * @dependencies: None (Android AudioRecord API)
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.service

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import kotlin.concurrent.thread

class AudioRecorder {

    companion object {
        private const val SAMPLE_RATE = 16000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val WAV_HEADER_SIZE = 44
        private const val TIMEOUT_MS = 3000L
    }

    private var audioRecord: AudioRecord? = null
    private var outputFile: File? = null
    private var isRecording = false
    private var recordingThread: Thread? = null

    private val bufferSize by lazy {
        val minSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        if (minSize == AudioRecord.ERROR || minSize == AudioRecord.ERROR_BAD_VALUE) {
            SAMPLE_RATE * 2
        } else {
            minSize
        }
    }

    fun start(outputFile: File) {
        if (isRecording) return

        this.outputFile = outputFile

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            bufferSize
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            audioRecord?.release()
            audioRecord = null
            throw IllegalStateException("AudioRecord not initialized. Microphone may be in use.")
        }

        isRecording = true
        audioRecord?.startRecording()

        recordingThread = thread(name = "AudioRecorder") {
            writePcmData(outputFile)
        }
    }

    fun stop(): File? {
        if (!isRecording) return null

        isRecording = false

        try {
            audioRecord?.stop()
        } catch (_: IllegalStateException) {}

        recordingThread?.join(TIMEOUT_MS)
        audioRecord?.release()
        audioRecord = null
        recordingThread = null

        val file = outputFile
        if (file != null && file.exists()) {
            writeWavHeader(file)
        }

        return file
    }

    fun release() {
        if (isRecording) {
            stop()
        }
    }

    private fun writePcmData(file: File) {
        val buffer = ShortArray(bufferSize / 2)
        var totalSamples: Long = 0

        FileOutputStream(file).use { fos ->
            // Reserve space for WAV header (44 bytes)
            fos.write(ByteArray(WAV_HEADER_SIZE))

            while (isRecording) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: break
                if (read <= 0) break

                val byteBuffer = ByteArray(read * 2)
                for (i in 0 until read) {
                    byteBuffer[i * 2] = (buffer[i].toInt() and 0xFF).toByte()
                    byteBuffer[i * 2 + 1] = (buffer[i].toInt() shr 8).toByte()
                }
                fos.write(byteBuffer)
                totalSamples += read
            }
        }
    }

    private fun writeWavHeader(file: File) {
        val totalSize = file.length()
        val dataSize = totalSize - WAV_HEADER_SIZE

        if (dataSize <= 0) return

        RandomAccessFile(file, "rw").use { raf ->
            raf.seek(0)
            // RIFF header
            raf.writeBytes("RIFF")
            raf.writeIntLE((totalSize - 8).toInt())
            raf.writeBytes("WAVE")
            // fmt chunk
            raf.writeBytes("fmt ")
            raf.writeIntLE(16) // chunk size
            raf.writeShortLE(1) // PCM format
            raf.writeShortLE(1) // mono
            raf.writeIntLE(SAMPLE_RATE)
            raf.writeIntLE(SAMPLE_RATE * 2) // byte rate (sampleRate * channels * bitsPerSample/8)
            raf.writeShortLE(2) // block align (channels * bitsPerSample/8)
            raf.writeShortLE(16) // bits per sample
            // data chunk
            raf.writeBytes("data")
            raf.writeIntLE(dataSize.toInt())
        }
    }

    private fun RandomAccessFile.writeIntLE(value: Int) {
        writeByte(value and 0xFF)
        writeByte((value shr 8) and 0xFF)
        writeByte((value shr 16) and 0xFF)
        writeByte((value shr 24) and 0xFF)
    }

    private fun RandomAccessFile.writeShortLE(value: Int) {
        writeByte(value and 0xFF)
        writeByte((value shr 8) and 0xFF)
    }
}
