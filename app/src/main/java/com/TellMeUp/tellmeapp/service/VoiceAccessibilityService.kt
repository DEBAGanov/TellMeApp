/**
 * @file: VoiceAccessibilityService.kt
 * @description: AccessibilityService for volume button interception and text insertion
 * @dependencies: VolumeButtonDetector, AudioRecorder
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.TellMeUp.tellmeapp.util.VolumeButtonDetector

class VoiceAccessibilityService : AccessibilityService() {

    companion object {
        const val ACTION_VOICE_TRIGGER = "com.TellMeUp.tellmeapp.VOICE_TRIGGER"
        const val ACTION_INSERT_TEXT = "com.TellMeUp.tellmeapp.INSERT_TEXT"
        const val EXTRA_TEXT = "extra_text"

        private var instance: VoiceAccessibilityService? = null

        fun getInstance(): VoiceAccessibilityService? = instance
    }

    private val volumeDetector = VolumeButtonDetector(
        onDoublePress = { onVoiceTrigger() }
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this

        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = flags or
                    AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onKeyEvent(event: android.view.KeyEvent?): Boolean {
        if (event == null) return super.onKeyEvent(event)

        if (event.keyCode == android.view.KeyEvent.KEYCODE_VOLUME_UP &&
            event.action == android.view.KeyEvent.ACTION_DOWN
        ) {
            volumeDetector.onVolumeUpPressed()
            return true
        }

        return super.onKeyEvent(event)
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    private fun onVoiceTrigger() {
        val intent = Intent(ACTION_VOICE_TRIGGER)
        sendBroadcast(intent)
    }

    fun insertText(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val focusNode = findFocusNode(rootNode)

        val inserted = if (focusNode != null && focusNode.isEditable) {
            insertViaAction(focusNode, text)
        } else {
            insertViaClipboard(text)
        }

        rootNode.recycle()
        return inserted
    }

    private fun findFocusNode(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        return root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
    }

    private fun insertViaAction(node: AccessibilityNodeInfo, text: String): Boolean {
        val args = Bundle()
        args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }

    private fun insertViaClipboard(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false

        try {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("text", text))

            val focusNode = findFocusNode(rootNode)
            if (focusNode != null && focusNode.isEditable) {
                val pasted = focusNode.performAction(AccessibilityNodeInfo.ACTION_PASTE)
                focusNode.recycle()
                return pasted
            }
        } finally {
            rootNode.recycle()
        }

        return false
    }
}
