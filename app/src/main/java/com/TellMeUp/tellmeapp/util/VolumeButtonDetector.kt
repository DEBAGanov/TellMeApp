/**
 * @file: VolumeButtonDetector.kt
 * @description: Detects double-press of Volume Up button within a time window
 * @dependencies: None
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.util

class VolumeButtonDetector(
    private val onDoublePress: () -> Unit,
    private val timeoutMs: Long = 300L
) {
    private var lastPressTime = 0L

    fun onVolumeUpPressed(): Boolean {
        val now = System.currentTimeMillis()
        val elapsed = now - lastPressTime
        lastPressTime = now

        if (elapsed in 1..timeoutMs) {
            lastPressTime = 0L
            onDoublePress()
            return true
        }

        return false
    }
}
