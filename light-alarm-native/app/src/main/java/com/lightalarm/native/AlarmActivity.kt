package com.lightalarm.nativeapp

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import java.text.SimpleDateFormat
import java.util.*

class AlarmActivity : AppCompatActivity() {
    
    private lateinit var mediaPlayer: MediaPlayer
    private var brightnessAnimator: ValueAnimator? = null
    private var isPreviewMode = false
    private var theme = "Sunrise"
    
    private lateinit var alarmLayout: ConstraintLayout
    private lateinit var currentTimeText: TextView
    private lateinit var stopAlarmButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set flags for lock screen display
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        
        setContentView(R.layout.activity_alarm)
        
        // Get intent extras
        isPreviewMode = intent.getBooleanExtra("preview_mode", false)
        theme = intent.getStringExtra("theme") ?: "Sunrise"
        
        setupUI()
        setThemeBackground()
        startSunriseEffect()
        startAlarmSound()
    }
    
    private fun setupUI() {
        // Get references to views
        alarmLayout = findViewById(R.id.alarm_layout)
        currentTimeText = findViewById(R.id.current_time_text)
        stopAlarmButton = findViewById(R.id.stop_alarm_button)
        
        // Set current time
        val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        currentTimeText.text = currentTime
        
        // Stop button click listener
        stopAlarmButton.setOnClickListener {
            stopAlarm()
            finish()
        }
    }
    
    private fun setThemeBackground() {
        // The background is already set in XML, just get it and start animation
        val animDrawable = alarmLayout.background as AnimationDrawable
        animDrawable.start()
    }
    
    private fun startSunriseEffect() {
        // Animate brightness from 0.0 to 1.0 over 20 minutes (1200 seconds)
        val duration = if (isPreviewMode) 10000L else 1200000L // 10 seconds for preview, 20 minutes for real
        
        brightnessAnimator = ValueAnimator.ofFloat(0.0f, 1.0f).apply {
            this.duration = duration
            addUpdateListener { animation ->
                val brightness = animation.animatedValue as Float
                val layoutParams = window.attributes
                layoutParams.screenBrightness = brightness
                window.attributes = layoutParams
            }
            start()
        }
    }
    
    private fun startAlarmSound() {
        try {
            // Use a bundled sound from res/raw (renamed to Android-safe resource name)
            val soundResourceId = R.raw.classicalarm_digital_alarm_wav
            mediaPlayer = MediaPlayer.create(this, soundResourceId)
            mediaPlayer.isLooping = true
            mediaPlayer.start()
        } catch (e: Exception) {
            // Fallback: try system default alarm sound, then vibrate
            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource("/system/media/audio/alarms/Argon.ogg")
                    prepare()
                    isLooping = true
                    start()
                }
            } catch (_: Exception) {
                try {
                    val vibrator = getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        vibrator.vibrate(android.os.VibrationEffect.createOneShot(1000, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(1000)
                    }
                } catch (_: Exception) {
                    // Ignore
                }
            }
        }
    }
    
    private fun stopAlarm() {
        brightnessAnimator?.cancel()
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopAlarm()
    }
    
    override fun onBackPressed() {
        // Prevent back button from dismissing alarm
        // Only allow dismiss button to close
    }
}