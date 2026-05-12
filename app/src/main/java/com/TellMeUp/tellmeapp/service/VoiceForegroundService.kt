/**
 * @file: VoiceForegroundService.kt
 * @description: Foreground service with recording, recognition, text insertion and volume key trigger
 * @dependencies: AudioRecorder, RecognizeSpeechUseCase, SpeechRepository, VoiceAccessibilityService
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import com.TellMeUp.tellmeapp.MainActivity
import com.TellMeUp.tellmeapp.R
import com.TellMeUp.tellmeapp.domain.model.AiProvider
import com.TellMeUp.tellmeapp.domain.model.VoiceState
import com.TellMeUp.tellmeapp.domain.repository.AiChatRepository
import com.TellMeUp.tellmeapp.domain.repository.ClaudeRepository
import com.TellMeUp.tellmeapp.domain.repository.SpeechRepository
import com.TellMeUp.tellmeapp.domain.usecase.RecognizeSpeechUseCase
import com.TellMeUp.tellmeapp.domain.usecase.SendAiMessageUseCase
import com.TellMeUp.tellmeapp.domain.usecase.SendClaudeMessageUseCase
import com.TellMeUp.tellmeapp.data.local.PreferencesStore
import com.TellMeUp.tellmeapp.util.AppLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class VoiceForegroundService : Service() {

    companion object {
        private const val TAG = "VoiceForegroundService"
        const val CHANNEL_ID = "tellmeapp_voice_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "com.TellMeUp.tellmeapp.ACTION_STOP_SERVICE"
        const val ACTION_VOICE_STATE_CHANGED = "com.TellMeUp.tellmeapp.VOICE_STATE_CHANGED"
        const val EXTRA_VOICE_STATE = "extra_voice_state"
        const val EXTRA_RECOGNIZED_TEXT = "extra_recognized_text"

        private var instance: VoiceForegroundService? = null
        fun getInstance(): VoiceForegroundService? = instance
        fun isRunning(): Boolean = instance != null

        fun start(context: Context) {
            val intent = Intent(context, VoiceForegroundService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, VoiceForegroundService::class.java)
            context.stopService(intent)
        }
    }

    @Inject lateinit var recognizeSpeechUseCase: RecognizeSpeechUseCase
    @Inject lateinit var speechRepository: SpeechRepository
    @Inject lateinit var preferencesStore: PreferencesStore
    @Inject lateinit var sendAiMessageUseCase: SendAiMessageUseCase
    @Inject lateinit var aiChatRepository: AiChatRepository
    @Inject lateinit var sendClaudeMessageUseCase: SendClaudeMessageUseCase
    @Inject lateinit var claudeRepository: ClaudeRepository

    private val audioRecorder = AudioRecorder()
    private var currentAudioFile: File? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default)

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _lastRecognizedText = MutableStateFlow<String?>(null)
    val lastRecognizedText: StateFlow<String?> = _lastRecognizedText.asStateFlow()

    private var mediaSession: MediaSessionCompat? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isHoldingMedia = false
    private var isRecordingFromMedia = false
    private val longPressThreshold = 400L

    private val mediaLongPressRunnable = Runnable {
        if (isHoldingMedia) {
            isRecordingFromMedia = true
            AppLogger.i(TAG, "MediaSession: Long press detected — starting recording")
            startRecording()
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
        initMediaSession()
        loadApiKey()
        AppLogger.i(TAG, "Service created, instance set")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = buildNotification()
        startForeground(
            NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )

        AppLogger.i(TAG, "Service started as foreground")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(mediaLongPressRunnable)
        mediaSession?.release()
        mediaSession = null
        audioRecorder.release()
        instance = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        AppLogger.w(TAG, "Service destroyed")
    }

    private fun initMediaSession() {
        try {
            mediaSession = MediaSessionCompat(this, "TellMeApp").apply {
                setCallback(object : MediaSessionCompat.Callback() {
                    override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
                        val ke = mediaButtonIntent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
                        if (ke != null) {
                            handleMediaKeyEvent(ke)
                            return true
                        }
                        return super.onMediaButtonEvent(mediaButtonIntent)
                    }
                })

                val state = PlaybackStateCompat.Builder()
                    .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE)
                    .setState(PlaybackStateCompat.STATE_PLAYING, 0, 0f)
                    .build()
                setPlaybackState(state)
                isActive = true
            }
            AppLogger.i(TAG, "MediaSession initialized and active")
        } catch (e: Exception) {
            AppLogger.e(TAG, "MediaSession init failed: ${e.message}")
        }
    }

    private fun handleMediaKeyEvent(event: KeyEvent) {
        when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                when (event.action) {
                    KeyEvent.ACTION_DOWN -> {
                        if (!isHoldingMedia) {
                            isHoldingMedia = true
                            handler.postDelayed(mediaLongPressRunnable, longPressThreshold)
                            AppLogger.d(TAG, "MediaSession: Volume DOWN, waiting ${longPressThreshold}ms")
                        }
                    }
                    KeyEvent.ACTION_UP -> {
                        handler.removeCallbacks(mediaLongPressRunnable)
                        if (isRecordingFromMedia) {
                            isRecordingFromMedia = false
                            AppLogger.d(TAG, "MediaSession: Volume UP — stopping recording")
                            stopAndRecognize()
                        } else {
                            AppLogger.d(TAG, "MediaSession: Volume UP — short press (ignored)")
                        }
                        isHoldingMedia = false
                    }
                }
            }
        }
    }

    private fun loadApiKey() {
        serviceScope.launch(Dispatchers.IO) {
            val key = preferencesStore.apiKey.first()
            if (!key.isNullOrBlank()) {
                speechRepository.setApiKey(key)
                AppLogger.i(TAG, "API key loaded from DataStore")
            } else {
                AppLogger.w(TAG, "No API key found in DataStore")
            }
        }
    }

    fun startRecording() {
        if (_voiceState.value != VoiceState.IDLE) {
            AppLogger.w(TAG, "startRecording called but state=${_voiceState.value}, ignoring")
            return
        }

        try {
            val audioFile = File(cacheDir, "voice_${System.currentTimeMillis()}.wav")
            currentAudioFile = audioFile
            audioRecorder.start(audioFile)

            _voiceState.value = VoiceState.RECORDING
            _lastRecognizedText.value = null
            AppLogger.i(TAG, "Recording started: ${audioFile.absolutePath}")
        } catch (e: Exception) {
            AppLogger.e(TAG, "Failed to start recording: ${e.message}")
            _voiceState.value = VoiceState.IDLE
        }
    }

    fun stopAndRecognize() {
        if (_voiceState.value != VoiceState.RECORDING) {
            AppLogger.w(TAG, "stopAndRecognize called but state=${_voiceState.value}, ignoring")
            return
        }

        _voiceState.value = VoiceState.PROCESSING
        AppLogger.i(TAG, "Recording stopped, starting recognition")

        serviceScope.launch(Dispatchers.IO) {
            val audioFile = audioRecorder.stop()

            if (audioFile == null || !audioFile.exists() || audioFile.length() <= 44) {
                AppLogger.w(TAG, "Audio file invalid: exists=${audioFile?.exists()}, size=${audioFile?.length()}")
                _voiceState.value = VoiceState.IDLE
                return@launch
            }

            AppLogger.d(TAG, "Audio file size: ${audioFile.length()} bytes")

            if (!speechRepository.hasApiKey()) {
                val savedKey = preferencesStore.apiKey.first()
                if (!savedKey.isNullOrBlank()) {
                    speechRepository.setApiKey(savedKey)
                    AppLogger.i(TAG, "API key loaded from DataStore (lazy)")
                } else {
                    AppLogger.w(TAG, "No API key configured — set API key in Settings")
                    _voiceState.value = VoiceState.IDLE
                    audioFile.delete()
                    return@launch
                }
            }

            val result = recognizeSpeechUseCase(audioFile)
            if (result.isSuccess) {
                AppLogger.i(TAG, "Recognition success: text='${result.text}'")
            } else {
                AppLogger.e(TAG, "Recognition failed: code=${result.errorCode}, msg=${result.errorMessage}")
            }

            if (result.isSuccess && result.text.isNotBlank()) {
                val aiEnabled = preferencesStore.aiEnabled.first()

                if (aiEnabled) {
                    val aiText = processWithAi(result.text)
                    val textToInsert = aiText ?: result.text
                    insertRecognizedText(textToInsert)
                    _lastRecognizedText.value = "AI: $textToInsert"
                } else {
                    insertRecognizedText(result.text)
                    _lastRecognizedText.value = result.text
                }
            }

            _voiceState.value = VoiceState.IDLE
            audioFile.delete()
        }
    }

    private suspend fun processWithAi(text: String): String? {
        _voiceState.value = VoiceState.AI_PROCESSING

        val providerKey = preferencesStore.aiProvider.first()
        val provider = AiProvider.fromKey(providerKey)
        AppLogger.i(TAG, "AI mode enabled, provider=${provider.displayName}, sending text: '$text'")

        return when (provider) {
            AiProvider.CLAUDE -> processWithClaude(text)
            AiProvider.ZAI -> processWithZai(text)
        }
    }

    private suspend fun processWithZai(text: String): String? {
        if (!aiChatRepository.hasApiKey()) {
            val savedKey = preferencesStore.aiApiKey.first()
            if (!savedKey.isNullOrBlank()) {
                aiChatRepository.setApiKey(savedKey)
                AppLogger.i(TAG, "ZAI API key loaded from DataStore")
            } else {
                AppLogger.w(TAG, "No ZAI API key configured — set z.ai key in Settings")
                return null
            }
        }

        val aiResponse = sendAiMessageUseCase(text)
        if (aiResponse != null) {
            AppLogger.i(TAG, "ZAI response: '$aiResponse'")
        } else {
            AppLogger.e(TAG, "ZAI request failed, using original text")
        }
        return aiResponse
    }

    private suspend fun processWithClaude(text: String): String? {
        if (!claudeRepository.hasApiKey()) {
            val savedKey = preferencesStore.claudeApiKey.first()
            if (!savedKey.isNullOrBlank()) {
                claudeRepository.setApiKey(savedKey)
                AppLogger.i(TAG, "Claude API key loaded from DataStore")
            } else {
                AppLogger.w(TAG, "No Claude API key configured — set Claude key in Settings")
                return null
            }
        }

        val aiResponse = sendClaudeMessageUseCase(text)
        if (aiResponse != null) {
            AppLogger.i(TAG, "Claude response: '$aiResponse'")
        } else {
            AppLogger.e(TAG, "Claude request failed, using original text")
        }
        return aiResponse
    }

    private fun insertRecognizedText(text: String) {
        handler.postDelayed({
            val service = VoiceAccessibilityService.getInstance()
            if (service != null) {
                val inserted = service.insertText(text)
                AppLogger.i(TAG, "Text insertion via AccessibilityService: $inserted, text='$text'")
            } else {
                AppLogger.w(TAG, "AccessibilityService not available (instance=null), trying clipboard paste")
                insertViaClipboardFallback(text)
            }
        }, 800)
    }

    private fun insertViaClipboardFallback(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("recognized_text", text))
        AppLogger.i(TAG, "Text copied to clipboard as fallback: '$text'")
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "TellMeApp Voice Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Keeps TellMeApp voice recognition active"
            setShowBadge(false)
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val contentIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this, 0, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, StopServiceReceiver::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TellMeApp")
            .setContentText("Голосовой ввод активен")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(contentPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Выключить",
                stopPendingIntent
            )
            .setOngoing(true)
            .setSilent(true)
            .build()
    }
}
