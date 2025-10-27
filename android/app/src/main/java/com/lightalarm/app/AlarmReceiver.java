package com.lightalarm.app;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";
    private static final String CHANNEL_ID = "ALARM_CHANNEL";
    private static final int NOTIFICATION_ID = 999;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "🚨 ALARM RECEIVER TRIGGERED - BACKGROUND TASK STARTED");
        Log.d(TAG, "📅 Current timestamp: " + System.currentTimeMillis());
        Log.d(TAG, "🕐 Current time: " + new java.util.Date(System.currentTimeMillis()));
        Log.d(TAG, "📋 Intent: " + (intent != null ? intent.toString() : "null"));
        Log.d(TAG, "🎯 Intent action: " + (intent != null ? intent.getAction() : "null"));

        String alarmType = intent.getStringExtra("alarm_type");
        String alarmSound = intent.getStringExtra("alarm_sound");
        long alarmId = intent.getLongExtra("alarm_id", -1);
        String alarmTheme = intent.getStringExtra("alarm_theme");

        // Validate intent extras for security
        if (alarmType != null && !alarmType.matches("^(light|sound)$")) {
            Log.e(TAG, "❌ Invalid alarm_type received: " + alarmType + " - rejecting");
            return; // Reject invalid values
        }

        Log.d(TAG, "🔥 ALARM TYPE: " + alarmType);
        Log.d(TAG, "🆔 ALARM ID: " + alarmId);
        Log.d(TAG, "🎵 ALARM SOUND: " + alarmSound);
        Log.d(TAG, "🎨 ALARM THEME: " + alarmTheme);
        Log.d(TAG, "⏰ ALARM TRIGGERED AT: " + new java.util.Date(System.currentTimeMillis()));

        // Enhanced alarm trigger logging
        java.util.Date triggerTime = new java.util.Date(System.currentTimeMillis());
        Log.d(TAG, "📊 ENHANCED ALARM TRIGGER DETAILS:");
        Log.d(TAG, "   🕐 Trigger timestamp: " + System.currentTimeMillis());
        Log.d(TAG, "   📅 Trigger date/time: " + triggerTime.toString());
        Log.d(TAG, "   🎯 Alarm type: " + alarmType);
        Log.d(TAG, "   🎵 Sound resource: " + (alarmSound != null ? alarmSound : "default"));
        Log.d(TAG, "   📱 Device locale: " + java.util.Locale.getDefault().toString());
        Log.d(TAG, "   🌍 Timezone: " + java.util.TimeZone.getDefault().getDisplayName());

        // Validate that this alarm should actually trigger now
        Log.d(TAG, "🔍 ALARM VALIDATION:");
        Log.d(TAG, "   - Intent extras received: " + (intent != null && intent.getExtras() != null ? intent.getExtras().size() + " extras" : "none"));
        Log.d(TAG, "   - Alarm type validation: " + (alarmType != null && alarmType.matches("^(light|sound)$") ? "VALID" : "INVALID"));

        // Create notification channel first
        createNotificationChannel(context);

        // Acquire wake lock to ensure service starts reliably and wake screen
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK |
            PowerManager.ACQUIRE_CAUSES_WAKEUP |
            PowerManager.ON_AFTER_RELEASE, "alarm:wakelock");
        wakeLock.acquire(60 * 1000L); // 60 seconds
        Log.d(TAG, "Wake lock acquired for alarm: " + alarmType);

        // Direct approach like Gentle Wakeup - handle everything in AlarmReceiver
        try {
            Log.d(TAG, "🔄 STARTING ALARM EXECUTION...");

            // Start background service for 20-min progression (light) or sound (sound)
            if ("light".equals(alarmType)) {
                Log.d(TAG, "🌅 LIGHT ALARM: Starting SunriseService for 20-minute light progression");
                Log.d(TAG, "🎯 ATTEMPTING TO START LIGHT STIMULATION SERVICE");
                startDirectLightSimulation(context, alarmSound, alarmId, alarmTheme);
                Log.d(TAG, "✅ LIGHT STIMULATION SERVICE STARTED");
            } else {
                Log.d(TAG, "🔊 SOUND ALARM: Starting AlarmService for sound alarm");
                startDirectSoundAlarm(context, alarmSound, alarmId, alarmTheme);
            }

            // CRITICAL: Always create fullscreen intent notification
            // This is how Gentle Wakeup works - notification launches fullscreen activity
            Log.d(TAG, "📱 Creating fullscreen intent notification for alarm type: " + alarmType);
        } catch (Exception e) {
            Log.e(TAG, "❌ FAILED TO START ALARM - " + e.getMessage(), e);
            Log.e(TAG, "🔍 Stack trace:", e);
            // Release wake lock if service fails
            if (wakeLock.isHeld()) {
                wakeLock.release();
                Log.d(TAG, "Wake lock released due to service failure");
            }
            return;
        }

        // Create fullscreen intent notification (like default Android alarm)
        Intent fullscreenIntent = new Intent(context, MainActivity.class);
        fullscreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
            | Intent.FLAG_ACTIVITY_SINGLE_TOP
            | Intent.FLAG_ACTIVITY_CLEAR_TOP
            | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        fullscreenIntent.putExtra("alarm_triggered", alarmType);
        fullscreenIntent.putExtra("launch_fullscreen", true);
        fullscreenIntent.putExtra("alarm_sound", alarmSound);
        fullscreenIntent.putExtra("fromAlarm", true);

        PendingIntent fullscreenPendingIntent = PendingIntent.getActivity(
            context, 
            NOTIFICATION_ID, 
            fullscreenIntent, 
            PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        // Check if notification permission is granted
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "POST_NOTIFICATIONS permission not granted - cannot show fullscreen notification");
                Log.e(TAG, "Launching MainActivity directly without notification");
                
                // Launch MainActivity directly without notification as fallback
                launchMainActivityDirectly(context, alarmType, alarmSound);
                return;
            }
        }

        // Create the fullscreen intent notification
        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Light Alarm")
                .setContentText(alarmType.equals("light") ? "Light sequence starting..." : "Alarm time!")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullscreenPendingIntent, true) // Auto-launch if permission granted
                .setContentIntent(fullscreenPendingIntent) // CRITICAL FIX: Launch when user taps notification
                .setAutoCancel(true)
                .setOngoing(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

            notificationManager.notify(NOTIFICATION_ID, builder.build());
            Log.d(TAG, "Fullscreen intent notification created successfully");
            
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException creating notification - permission denied", e);
            Log.e(TAG, "Launching MainActivity directly without notification");
            
            // Fallback: Launch MainActivity directly
            launchMainActivityDirectly(context, alarmType, alarmSound);
        } finally {
            // Always release wake lock after processing
            if (wakeLock.isHeld()) {
                wakeLock.release();
                Log.d(TAG, "Wake lock released after alarm processing");
            }
        }
    }
    
    // Direct light simulation like Gentle Wakeup
    private void startDirectLightSimulation(Context context, String alarmSound, long alarmId, String alarmTheme) {
        Log.d(TAG, "🌅 STARTING DIRECT LIGHT SIMULATION");
        Log.d(TAG, "📋 Alarm sound for light simulation: " + alarmSound);
        Log.d(TAG, "🆔 Alarm ID: " + alarmId);
        Log.d(TAG, "🎨 Alarm theme: " + alarmTheme);

        try {
            Log.d(TAG, "🔧 Creating SunriseService intent...");
            // Start SunriseService to handle the 20-minute light progression
            Intent serviceIntent = new Intent(context, SunriseService.class);
            serviceIntent.putExtra("alarm_sound", alarmSound);
            serviceIntent.putExtra("alarm_id", alarmId);
            serviceIntent.putExtra("alarm_theme", alarmTheme);

            Log.d(TAG, "🚀 Starting SunriseService...");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d(TAG, "📱 Using startForegroundService (Android 8+)");
                context.startForegroundService(serviceIntent);
                Log.d(TAG, "✅ SunriseService started as foreground service");
            } else {
                Log.d(TAG, "📱 Using startService (Android < 8)");
                context.startService(serviceIntent);
                Log.d(TAG, "✅ SunriseService started");
            }

            Log.d(TAG, "🎯 SunriseService will handle 20-minute light progression and launch MainActivity");
            Log.d(TAG, "💡 Light stimulation should now be active");
        } catch (Exception e) {
            Log.e(TAG, "❌ CRITICAL ERROR: Failed to start light simulation service", e);
            Log.e(TAG, "🔍 Error details: " + e.getMessage());
            Log.e(TAG, "📊 Stack trace:", e);
            throw e; // Re-throw to be caught by outer try/catch
        }
    }
    
    // Direct sound alarm like Gentle Wakeup
    private void startDirectSoundAlarm(Context context, String alarmSound, long alarmId, String alarmTheme) {
        Log.d(TAG, "Starting AlarmService for sound alarm");
        Log.d(TAG, "🆔 Alarm ID: " + alarmId);
        Log.d(TAG, "🎨 Alarm theme: " + alarmTheme);
        
        // Start AlarmService to handle the sound alarm
        Intent serviceIntent = new Intent(context, AlarmService.class);
        serviceIntent.putExtra("alarm_type", "sound");
        serviceIntent.putExtra("alarm_sound", alarmSound);
        serviceIntent.putExtra("alarm_id", alarmId);
        serviceIntent.putExtra("alarm_theme", alarmTheme);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
            Log.d(TAG, "AlarmService started as foreground service");
        } else {
            context.startService(serviceIntent);
            Log.d(TAG, "AlarmService started");
        }
        
        Log.d(TAG, "AlarmService will handle sound alarm and launch MainActivity");
    }

    private void launchMainActivityDirectly(Context context, String alarmType, String alarmSound) {
        Intent directLaunch = new Intent(context, MainActivity.class);
        directLaunch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
            | Intent.FLAG_ACTIVITY_SINGLE_TOP
            | Intent.FLAG_ACTIVITY_CLEAR_TOP
            | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        directLaunch.putExtra("alarm_triggered", alarmType);
        directLaunch.putExtra("launch_fullscreen", true);
        directLaunch.putExtra("alarm_sound", alarmSound);
        directLaunch.putExtra("fromAlarm", true);
        context.startActivity(directLaunch);
        Log.d(TAG, "MainActivity launched directly for alarm: " + alarmType);
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Full screen alarm notifications");
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            channel.setBypassDnd(true);

            NotificationManager notificationManager = 
                context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}

