/**
 * @file: MainActivity.kt
 * @description: Main entry point of the application with Compose UI
 * @dependencies: Hilt, TellMeAppTheme, MainScreen
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.TellMeUp.tellmeapp.ui.screen.main.MainScreen
import com.TellMeUp.tellmeapp.ui.theme.TellMeAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TellMeAppTheme(darkTheme = true) {
                MainScreen()
            }
        }
    }
}
