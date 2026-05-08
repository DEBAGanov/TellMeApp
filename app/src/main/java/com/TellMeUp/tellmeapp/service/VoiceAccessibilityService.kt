/**
 * @file: VoiceAccessibilityService.kt
 * @description: AccessibilityService for volume button hold-to-record and text insertion
 * @dependencies: None
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class VoiceAccessibilityService : AccessibilityService() {

    companion object {
        const val ACTION_RECORDING_START = "com.TellMeUp.tellmeapp.RECORDING_START"
        const val ACTION_RECORDING_STOP = "com.TellMeUp.tellmeapp.RECORDING_STOP"
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
            sendBroadcast(Intent(ACTION_RECORDING_START))
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this

        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = flags or
                    AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if (event == null) return super.onKeyEvent(event)

        if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            when (event.action) {
                KeyEvent.ACTION_DOWN -> {
                    if (!isHolding) {
                        isHolding = true
                        handler.postDelayed(longPressRunnable, longPressThreshold)
                    }
                    return true
                }
                KeyEvent.ACTION_UP -> {
                    handler.removeCallbacks(longPressRunnable)

                    if (isRecording) {
                        isRecording = false
                        sendBroadcast(Intent(ACTION_RECORDING_STOP))
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
