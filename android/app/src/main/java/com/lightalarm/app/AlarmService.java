package com.lightalarm.app;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

public class AlarmService extends Service {
    private static final String TAG = "AlarmService";
    private static final String CHANNEL_ID = "LIGHT_ALARM_CHANNEL";
    private static final int NOTIFICATION_ID = 1;
    
    private PowerManager.WakeLock wakeLock;
    private Vibrator vibrator;
    private Intent serviceIntent;
    private String soundUrl;
    private android.media.Ringtone alarmRingtone;
    private MediaPlayer mediaPlayer;
    private Handler volumeHandler;
    private float currentVolume = 0.5f;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "AlarmService created");
        
        // Create notification channel
        createNotificationChannel();
        
        // Initialize vibrator
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            Log.d(TAG, "🔥 AlarmService started");

            if (intent != null) {
                String alarmType = intent.getStringExtra("alarm_type");
                this.soundUrl = intent.getStringExtra("alarm_sound"); // Store soundUrl
                Log.d(TAG, "Alarm type: " + alarmType);
                Log.d(TAG, "🎵 Sound URL: " + this.soundUrl);
            }

            // Store the intent for later use
            this.serviceIntent = intent;

        // Acquire wake lock to keep device awake
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "LightAlarm::WakeLock"
        );
        wakeLock.acquire(10*60*1000L /*10 minutes*/);

        // Start foreground service
        startForeground(NOTIFICATION_ID, createNotification());

            // Trigger alarm actions
            triggerAlarmActions();

            return START_STICKY;
        } catch (Exception e) {
            Log.e(TAG, "❌ CRASH in AlarmService onStartCommand", e);
            // Release wake lock if we have one
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
            return START_NOT_STICKY; // Don't restart if crashed
        }
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Light Alarm",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Light Alarm notifications");
            channel.enableVibration(true);
            channel.setShowBadge(true);
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
    
    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Light Alarm Active")
            .setContentText("Your alarm is going off!")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .build();
    }
    
    private void triggerAlarmActions() {
        try {
            Log.d(TAG, "🎯 triggerAlarmActions called");

            // Get the intent that started this service
            Intent intent = this.serviceIntent;
            String alarmType = intent != null ? intent.getStringExtra("alarm_type") : null;

            Log.d(TAG, "Service intent: " + (intent != null ? intent.toString() : "null"));
        Log.d(TAG, "Alarm type from intent: " + alarmType);

            if ("light".equals(alarmType)) {
                Log.d(TAG, "🔆 Triggering 20-minute pre-alarm light sequence");
                triggerLightSequence();
            } else if ("sound".equals(alarmType)) {
                Log.d(TAG, "🔊 Triggering exact time alarm with sound");
                triggerSoundAlarm();
            } else {
                Log.d(TAG, "⚠️ Triggering legacy alarm actions - alarm type not recognized: " + alarmType);
                triggerLegacyAlarm();
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ CRASH in triggerAlarmActions", e);
        }
    }
    
    private void triggerLightSequence() {
        Log.d(TAG, "🌅 triggerLightSequence called - starting light sequence!");

        // Launch app for light sequence (20 min before)
        Intent launchIntent = new Intent(this, MainActivity.class);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                             Intent.FLAG_ACTIVITY_CLEAR_TOP |
                             Intent.FLAG_ACTIVITY_SINGLE_TOP |
                             Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        launchIntent.putExtra("alarm_triggered", "light");
        Log.d(TAG, "🚀 Launching MainActivity for light sequence");
        startActivity(launchIntent);

        // Send broadcast for light sequence
        Intent broadcastIntent = new Intent("com.lightalarm.app.LIGHT_ALARM_TRIGGERED");
        Log.d(TAG, "📡 Sending LIGHT_ALARM_TRIGGERED broadcast");
        sendBroadcast(broadcastIntent);

        Log.d(TAG, "✅ Light sequence triggered successfully");
    }
    
    private void triggerSoundAlarm() {
        Log.d(TAG, "🔊 triggerSoundAlarm called - starting sound alarm!");

        // Vibrate pattern
        long[] vibratePattern = {0, 500, 200, 500, 200, 500, 1000};
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(vibratePattern, 0);
        }

        // Play alarm sound with gradual volume increase
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mediaPlayer.setLooping(true);

            // Try to play from resource name, fallback to default
            if (this.soundUrl != null && !this.soundUrl.isEmpty() && !this.soundUrl.equals("default")) {
                try {
                    // Get the resource ID from its name (String)
                    int resourceId = getResources().getIdentifier(this.soundUrl, "raw", getPackageName());

                    if (resourceId != 0) {
                        Log.d(TAG, "Attempting to play from resource ID: " + resourceId + " for name: " + this.soundUrl);
                        mediaPlayer.setDataSource(getApplicationContext(),
                            android.net.Uri.parse("android.resource://" + getPackageName() + "/" + resourceId));
                    } else {
                        // Fallback if the resource name is invalid
                        Log.w(TAG, "Could not find resource ID for name: " + this.soundUrl + ". Falling back to default.");
                        mediaPlayer.setDataSource(getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
                    }

                } catch (Exception e) {
                    Log.e(TAG, "❌ Failed to set data source from resource name. Falling back to default.", e);
                    mediaPlayer.setDataSource(getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
                }
            } else {
                Log.d(TAG, "No valid sound resource. Playing default alarm sound.");
                mediaPlayer.setDataSource(getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
            }
            
            // Start at 50% volume
            currentVolume = 0.5f;
            mediaPlayer.setVolume(currentVolume, currentVolume);
            
            mediaPlayer.prepare();
            mediaPlayer.start();
            Log.d(TAG, "🔔 Playing alarm sound at 50% volume");
            
            // Gradually increase volume over 60 seconds
            volumeHandler = new Handler(Looper.getMainLooper());
            startVolumeIncrease();
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error playing alarm sound", e);
        }

        // Launch the main app to show full screen alarm
        Intent launchIntent = new Intent(this, MainActivity.class);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                             Intent.FLAG_ACTIVITY_CLEAR_TOP |
                             Intent.FLAG_ACTIVITY_SINGLE_TOP |
                             Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        launchIntent.putExtra("alarm_triggered", "sound");
        startActivity(launchIntent);

        // Send broadcast to main app
        Intent broadcastIntent = new Intent("com.lightalarm.app.SOUND_ALARM_TRIGGERED");
        sendBroadcast(broadcastIntent);
    }
    
    private void startVolumeIncrease() {
        volumeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying() && currentVolume < 1.0f) {
                    currentVolume += 0.015f; // Increase by 1.5% every second
                    if (currentVolume > 1.0f) currentVolume = 1.0f;
                    
                    mediaPlayer.setVolume(currentVolume, currentVolume);
                    Log.d(TAG, "🔊 Volume: " + (int)(currentVolume * 100) + "%");
                    
                    // Continue increasing
                    volumeHandler.postDelayed(this, 1000); // Every 1 second
                }
            }
        }, 1000);
    }
    
    private void triggerLegacyAlarm() {
        // Vibrate pattern: [vibrate 500ms, pause 200ms] x3, then pause 1s, repeat
        long[] vibratePattern = {0, 500, 200, 500, 200, 500, 1000};
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(vibratePattern, 0);
        }
        
        // Launch the main app to show full screen alarm
        Intent launchIntent = new Intent(this, MainActivity.class);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
                             Intent.FLAG_ACTIVITY_CLEAR_TOP |
                             Intent.FLAG_ACTIVITY_SINGLE_TOP |
                             Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        launchIntent.putExtra("alarm_triggered", true);
        startActivity(launchIntent);
        
        // Send broadcast to main app
        Intent broadcastIntent = new Intent("com.lightalarm.app.ALARM_TRIGGERED");
        sendBroadcast(broadcastIntent);
    }
    
    @Override
    public void onDestroy() {
        Log.d(TAG, "AlarmService destroyed");
        
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        
        if (vibrator != null) {
            vibrator.cancel();
        }
        
        if (volumeHandler != null) {
            volumeHandler.removeCallbacksAndMessages(null);
        }
        
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
            Log.d(TAG, "🔇 Stopped and released MediaPlayer");
        }
        
        if (alarmRingtone != null && alarmRingtone.isPlaying()) {
            alarmRingtone.stop();
            Log.d(TAG, "🔇 Stopped alarm ringtone");
        }
        
        super.onDestroy();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    // Static method to schedule dual alarms (20min light + exact time sound)
    public static void scheduleAlarm(Context context, long alarmId, int hour, int minute, String amPm, String soundResourceName, String theme) {
        Log.d(TAG, "🔥 scheduleAlarm called from AlarmServicePlugin!");
        Log.d(TAG, "🆔 Alarm ID: " + alarmId);
        Log.d(TAG, "🕐 Time: " + hour + ":" + String.format("%02d", minute) + " " + amPm);
        Log.d(TAG, "🎵 Sound: " + soundResourceName);
        Log.d(TAG, "🎨 Theme: " + theme);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Convert to 24-hour format and calculate alarm time
        int targetHour = hour;
        if (amPm.equals("PM") && hour != 12) {
            targetHour += 12;
        } else if (amPm.equals("AM") && hour == 12) {
            targetHour = 0;
        }

        // Calculate next occurrence of this alarm time
        java.util.Calendar alarmCal = java.util.Calendar.getInstance();
        alarmCal.set(java.util.Calendar.HOUR_OF_DAY, targetHour);
        alarmCal.set(java.util.Calendar.MINUTE, minute);
        alarmCal.set(java.util.Calendar.SECOND, 0);
        alarmCal.set(java.util.Calendar.MILLISECOND, 0);

        // If the time has passed today, set for tomorrow
        if (alarmCal.getTimeInMillis() <= System.currentTimeMillis()) {
            alarmCal.add(java.util.Calendar.DAY_OF_MONTH, 1);
        }

        long alarmTimeMillis = alarmCal.getTimeInMillis();
        Log.d(TAG, "📅 Alarm time: " + new java.util.Date(alarmTimeMillis));

        // Generate unique request codes based on alarm ID
        int lightRequestCode = (int) (alarmId * 2);
        int soundRequestCode = (int) (alarmId * 2 + 1);

        // 1) Schedule 20-minute pre-alarm for light sequence
        long lightAlarmTime = alarmTimeMillis - (20 * 60 * 1000); // 20 minutes before
        Intent lightIntent = new Intent(context, AlarmReceiver.class);
        lightIntent.setAction("com.lightalarm.app.LIGHT_ALARM");
        lightIntent.putExtra("alarm_type", "light");
        lightIntent.putExtra("alarm_id", alarmId);
        lightIntent.putExtra("alarm_sound", soundResourceName);
        lightIntent.putExtra("alarm_theme", theme);
        
        PendingIntent lightPendingIntent = PendingIntent.getBroadcast(
            context, 
            lightRequestCode, 
            lightIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // 2) Schedule exact time alarm for sound
        Intent soundIntent = new Intent(context, AlarmReceiver.class);
        soundIntent.setAction("com.lightalarm.app.SOUND_ALARM");
        soundIntent.putExtra("alarm_type", "sound");
        soundIntent.putExtra("alarm_id", alarmId);
        soundIntent.putExtra("alarm_sound", soundResourceName);
        soundIntent.putExtra("alarm_theme", theme);
        
        PendingIntent soundPendingIntent = PendingIntent.getBroadcast(
            context, 
            soundRequestCode, 
            soundIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Use exact alarms for precise timing - setExactAndAllowWhileIdle works on all API levels
        // Schedule light alarm (20 min before)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            lightAlarmTime,
            lightPendingIntent
        );
        // Schedule sound alarm (exact time)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alarmTimeMillis,
            soundPendingIntent
        );
        
        Log.d(TAG, "Dual alarms scheduled: Light at " + new java.util.Date(lightAlarmTime) + ", Sound at " + new java.util.Date(alarmTimeMillis));
        Log.d(TAG, "Request codes: Light=" + lightRequestCode + ", Sound=" + soundRequestCode);
    }
    
    // Static method to cancel specific alarm by ID
    public static void cancelAlarm(Context context, long alarmId) {
        Log.d(TAG, "🚫 Cancelling alarm ID: " + alarmId);
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        // Generate unique request codes based on alarm ID
        int lightRequestCode = (int) (alarmId * 2);
        int soundRequestCode = (int) (alarmId * 2 + 1);
        
        // Cancel light alarm
        Intent lightIntent = new Intent(context, AlarmReceiver.class);
        lightIntent.setAction("com.lightalarm.app.LIGHT_ALARM");
        lightIntent.putExtra("alarm_id", alarmId);
        PendingIntent lightPendingIntent = PendingIntent.getBroadcast(
            context, 
            lightRequestCode, 
            lightIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(lightPendingIntent);
        Log.d(TAG, "✅ Light alarm cancelled (request code " + lightRequestCode + ")");
        
        // Cancel sound alarm
        Intent soundIntent = new Intent(context, AlarmReceiver.class);
        soundIntent.setAction("com.lightalarm.app.SOUND_ALARM");
        soundIntent.putExtra("alarm_id", alarmId);
        PendingIntent soundPendingIntent = PendingIntent.getBroadcast(
            context, 
            soundRequestCode, 
            soundIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        alarmManager.cancel(soundPendingIntent);
        Log.d(TAG, "✅ Sound alarm cancelled (request code " + soundRequestCode + ")");
        
        Log.d(TAG, "🎯 Alarm " + alarmId + " cancelled successfully");
    }

    // Static helper: stop sound alarm service
    public static void stopSound(Context context) {
        try {
            context.stopService(new Intent(context, AlarmService.class));
            Log.d(TAG, "AlarmService stop requested by JS bridge");
        } catch (Exception e) {
            Log.e(TAG, "Failed to stop AlarmService", e);
        }
    }

    // Static helper: schedule snooze (sound-only) after X minutes
    public static void scheduleSnoozeSound(Context context, int minutes) {
        try {
            long now = System.currentTimeMillis();
            long snoozeAt = now + minutes * 60L * 1000L;
            Log.d(TAG, "Scheduling snooze at: " + new java.util.Date(snoozeAt));

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent soundIntent = new Intent(context, AlarmReceiver.class);
            soundIntent.setAction("com.lightalarm.app.SOUND_ALARM");
            soundIntent.putExtra("alarm_type", "sound");
            soundIntent.putExtra("alarm_sound", "");

            PendingIntent soundPendingIntent = PendingIntent.getBroadcast(
                context,
                200,
                soundIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                snoozeAt,
                soundPendingIntent
            );
        } catch (Exception e) {
            Log.e(TAG, "Failed to schedule snooze sound", e);
        }
    }
}
