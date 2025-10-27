package com.lightalarm.nativeapp

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.lightalarm.nativeapp.ui.screens.WakeUpPreviewScreen
import com.lightalarm.nativeapp.ui.theme.LightAlarmTheme

class AlarmComposeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set flags for lock screen display
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)

        // Get theme from intent
        val theme = intent.getStringExtra("theme") ?: "Sunrise"

        setContent {
            LightAlarmTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WakeUpPreviewScreen(
                        theme = com.lightalarm.nativeapp.data.LightTheme.valueOf(theme.uppercase()),
                        onNavigateBack = { finish() }
                    )
                }
            }
        }
    }
}

