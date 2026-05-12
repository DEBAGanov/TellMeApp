/**
 * @file: VoiceAccessibilityService.kt
 * @description: AccessibilityService for volume button hold-to-record and text insertion
 * @dependencies: VoiceForegroundService
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import com.TellMeUp.tellmeapp.util.AppLogger
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class VoiceAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "VoiceAccessibility"
        const val ACTION_INSERT_TEXT = "com.TellMeUp.tellmeapp.INSERT_TEXT"
        const val EXTRA_TEXT = "extra_text"

        private var instance: VoiceAccessibilityService? = null

        fun getInstance(): VoiceAccessibilityService? = instance
    }

    private val handler = Handler(Looper.getMainLooper())
    private var isHolding = false
    private var isRecording = false

    private val longPressThreshold = 400L

    private val longPressRunnable = Runnable {
        if (isHolding) {
            isRecording = true
            AppLogger.d(TAG, "Long press detected — starting recording")
            val service = VoiceForegroundService.getInstance()
            if (service != null) {
                service.startRecording()
            } else {
                AppLogger.w(TAG, "VoiceForegroundService not running — cannot start recording")
                isRecording = false
            }
        }
    }

    override fun onServiceConnected() {
        AppLogger.i(TAG, "onServiceConnected called")
        try {
            super.onServiceConnected()
            instance = this

            serviceInfo = serviceInfo.apply {
                eventTypes = AccessibilityEvent.TYPES_ALL_MASK
                feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
                flags = flags or
                        AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                        AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            }

            AppLogger.i(TAG, "Service connected OK, flags=${serviceInfo.flags}")
        } catch (e: Exception) {
            AppLogger.e(TAG, "onServiceConnected error: ${e.message}")
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event == null) return super.onKeyEvent(event)

        AppLogger.d(TAG, "onKeyEvent: keyCode=${event.keyCode} action=${event.action}")

        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP || event.keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            when (event.action) {
                KeyEvent.ACTION_DOWN -> {
                    if (!isHolding) {
                        isHolding = true
                        handler.postDelayed(longPressRunnable, longPressThreshold)
                        AppLogger.d(TAG, "Volume key DOWN — waiting for long press (${longPressThreshold}ms)")
                    }
                    return true
                }
                KeyEvent.ACTION_UP -> {
                    handler.removeCallbacks(longPressRunnable)

                    if (isRecording) {
                        isRecording = false
                        AppLogger.d(TAG, "Volume key UP — stopping recording")
                        VoiceForegroundService.getInstance()?.stopAndRecognize()
                    } else {
                        AppLogger.d(TAG, "Volume key UP — short press (ignored)")
                    }

                    isHolding = false
                    return true
                }
            }
        }

        return super.onKeyEvent(event)
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(longPressRunnable)
        instance = null
        AppLogger.w(TAG, "Service destroyed")
    }

    fun insertText(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: run {
            AppLogger.w(TAG, "insertText: rootInActiveWindow is null")
            return false
        }
        val focusNode = findFocusNode(rootNode)

        val inserted = if (focusNode != null && focusNode.isEditable) {
            AppLogger.d(TAG, "insertText: found editable node, className=${focusNode.className}, textLen=${focusNode.text?.length}")
            insertAtCursor(focusNode, text)
        } else {
            AppLogger.w(TAG, "insertText: no editable focus node, focusNode=$focusNode")
            insertViaClipboard(text)
        }

        focusNode?.recycle()
        rootNode.recycle()
        return inserted
    }

    private fun findFocusNode(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        return root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
    }

    private fun insertAtCursor(node: AccessibilityNodeInfo, text: String): Boolean {
        val currentText = node.text?.toString() ?: ""
        val start = node.textSelectionStart
        val end = node.textSelectionEnd

        val insertPos = if (start >= 0 && start <= currentText.length) start else currentText.length

        val newText = buildString {
            append(currentText.substring(0, insertPos))
            append(text)
            append(currentText.substring(minOf(end, currentText.length)))
        }

        val args = Bundle()
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, newText)
        val result = node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)

        if (result) {
            AppLogger.i(TAG, "insertAtCursor: inserted '$text' at pos $insertPos")
        } else {
            AppLogger.w(TAG, "insertAtCursor: ACTION_SET_TEXT failed, trying clipboard paste")
            return insertViaClipboard(text)
        }
        return true
    }

    private fun insertViaClipboard(text: String): Boolean {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("text", text))

        val rootNode = rootInActiveWindow ?: return false
        try {
            val focusNode = findFocusNode(rootNode)
            if (focusNode != null && focusNode.isEditable) {
                val pasted = focusNode.performAction(AccessibilityNodeInfo.ACTION_PASTE)
                AppLogger.d(TAG, "insertViaClipboard: paste result=$pasted")
                focusNode.recycle()
                return pasted
            }
        } finally {
            rootNode.recycle()
        }
        return false
    }
}
