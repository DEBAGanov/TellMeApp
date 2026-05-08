/**
 * @file: TellMeApp.kt
 * @description: Application class with Hilt dependency injection setup
 * @dependencies: Hilt
 * @created: 2026-05-08
 */

package com.TellMeUp.tellmeapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TellMeApp : Application()
