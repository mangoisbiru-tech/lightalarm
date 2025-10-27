package com.lightalarm.nativeapp

import android.app.KeyguardManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "AlarmReceiver"
        private const val NOTIFICATION_ID = 999
        private const val CHANNEL_ID = "ALARM_CHANNEL"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "🚨 ALARM TRIGGERED - Native Android Alarm")
        
        // Acquire FULL_WAKE_LOCK to wake screen
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or 
            PowerManager.ACQUIRE_CAUSES_WAKEUP or 
            PowerManager.ON_AFTER_RELEASE,
            "LightAlarm::AlarmWakeLock"
        )
        wakeLock.acquire(60 * 1000L) // 60 seconds
        
        try {
            // Create notification channel
            createNotificationChannel(context)
            
            // Launch fullscreen alarm activity
            launchAlarmActivity(context, intent)
            
            // Create fullscreen intent notification
            createFullscreenNotification(context, intent)
            
        } finally {
            if (wakeLock.isHeld()) {
                wakeLock.release()
            }
        }
    }
    
    private fun launchAlarmActivity(context: Context, intent: Intent) {
        val alarmIntent = Intent(context, AlarmComposeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            putExtra("theme", intent.getStringExtra("theme") ?: "Sunrise")
            putExtra("alarm_time", intent.getLongExtra("alarm_time", System.currentTimeMillis()))
        }

        context.startActivity(alarmIntent)
        Log.d(TAG, "AlarmComposeActivity launched with wake lock")
    }
    
    private fun createFullscreenNotification(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create fullscreen intent
        val fullscreenIntent = Intent(context, AlarmComposeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("theme", intent.getStringExtra("theme") ?: "Sunrise")
        }
        
        val fullscreenPendingIntent = PendingIntent.getActivity(
            context, NOTIFICATION_ID, fullscreenIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Light Alarm")
            .setContentText("Wake up with gentle light")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullscreenPendingIntent, true)
            .setContentIntent(fullscreenPendingIntent)
            .setAutoCancel(true)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()
        
        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
            Log.d(TAG, "Fullscreen notification created")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to create notification: ${e.message}")
        }
    }
    
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Full screen alarm notifications"
                enableLights(true)
                enableVibration(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }
            
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
