/**
 * @file: StopServiceReceiver.kt
 * @description: BroadcastReceiver for stop button in notification
 * @dependencies: VoiceForegroundService
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StopServiceReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_SERVICE_STOPPED = "com.TellMeUp.tellmeapp.SERVICE_STOPPED"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == VoiceForegroundService.ACTION_STOP) {
            VoiceForegroundService.stop(context)
            val broadcast = Intent(ACTION_SERVICE_STOPPED)
            context.sendBroadcast(broadcast)
        }
    }
}
