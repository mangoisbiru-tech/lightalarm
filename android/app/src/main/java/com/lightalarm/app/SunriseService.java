package com.lightalarm.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import androidx.core.app.NotificationCompat;

public class SunriseService extends Service {
    private static final String TAG = "SunriseService";
    private static final String CHANNEL_ID = "SUNRISE_SERVICE_CHANNEL";
    private static final int NOTIFICATION_ID = 2;
    
    private PowerManager.WakeLock wakeLock;
    private Handler handler;
    private long startTime;
    private String alarmSound;
    private boolean isRunning = false;
    private static final int MAX_RESTART_ATTEMPTS = 3;
    private int restartCount = 0;
    private int originalBrightness = -1; // Store original brightness to restore later
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "SunriseService created");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "🌅 SUNRISE SERVICE STARTED - LIGHT STIMULATION BEGINNING");
        Log.d(TAG, "📅 Service start timestamp: " + System.currentTimeMillis());
        Log.d(TAG, "🕐 Service start time: " + new java.util.Date(System.currentTimeMillis()));
        Log.d(TAG, "🔄 Restart attempt: " + (restartCount + 1) + "/" + MAX_RESTART_ATTEMPTS);

        // Prevent infinite restart loops
        if (restartCount >= MAX_RESTART_ATTEMPTS) {
            Log.e(TAG, "❌ Max restart attempts reached, stopping service");
            stopSelf();
            return START_STICKY;
        }

        if (intent != null) {
            alarmSound = intent.getStringExtra("alarm_sound");
            Log.d(TAG, "🎵 Alarm sound resource: " + alarmSound);
            Log.d(TAG, "📋 Intent extras: " + (intent.getExtras() != null ? intent.getExtras().toString() : "none"));
        } else {
            Log.w(TAG, "⚠️ Intent is null - service started without alarm data");
        }
        
        // Start as foreground service (required for Android 8+) but with silent notification
        createNotificationChannel();
        Notification notification = buildSilentNotification();
        startForeground(NOTIFICATION_ID, notification);
        
        // Acquire wake lock for 20 minutes
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SunriseService::WakeLock");
        wakeLock.acquire(20 * 60 * 1000L);
        Log.d(TAG, "Wake lock acquired for 20 minutes");

        // Store original brightness and start with dim brightness
        originalBrightness = getScreenBrightness();
        Log.d(TAG, "💡 ORIGINAL SCREEN BRIGHTNESS STORED: " + originalBrightness);

        // Set initial dim brightness for light simulation
        setScreenBrightness(10); // Start with very dim (10/255)
        Log.d(TAG, "🌅 INITIAL BRIGHTNESS SET: 10/255 (4% of max)");
        Log.d(TAG, "🎯 LIGHT STIMULATION: Starting 20-minute brightness progression");

        // Launch MainActivity fullscreen
        Log.d(TAG, "🔍 DEBUG: Launching MainActivity fullscreen");
        launchMainActivity();
        
        // Start updating brightness/color every 2 seconds
        startTime = System.currentTimeMillis();
        handler = new Handler(Looper.getMainLooper());
        isRunning = true;
        updateSimulation();
        
        restartCount++; // Increment restart counter
        return START_STICKY; // Restart if killed - ensures alarm reliability
    }
    
    private final Runnable simulationRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isRunning) return;

            long elapsed = System.currentTimeMillis() - startTime;
            int progress = (int) ((elapsed / (20.0 * 60 * 1000)) * 100);
            progress = Math.min(progress, 100);

            Log.d(TAG, "🔍 DEBUG: Sunrise progress: " + progress + "% (elapsed: " + elapsed + "ms)");

            // Calculate brightness based on progress (0-100% = 10-255 brightness)
            int targetBrightness = 10 + (progress * 245 / 100); // 10 to 255 range
            targetBrightness = Math.min(targetBrightness, 255);
            targetBrightness = Math.max(targetBrightness, 10);

            try {
                Log.d(TAG, "🔍 DEBUG: Setting screen brightness to: " + targetBrightness);
                setScreenBrightness(targetBrightness);
                Log.d(TAG, "✅ Screen brightness updated to: " + targetBrightness);
            } catch (Exception e) {
                Log.e(TAG, "❌ Failed to set screen brightness", e);
            }

            if (progress >= 100) {
                Log.d(TAG, "Sunrise complete, triggering sound alarm");
                triggerSoundAlarm();
                stopSelf();
                return;
            }

            broadcastProgress(progress);

            // Re-schedule the same runnable
            handler.postDelayed(this, 2000);
        }
    };

    private void updateSimulation() {
        if (!isRunning) return;
        // Start the simulation loop
        handler.post(simulationRunnable);
    }
    
    private void broadcastProgress(int progress) {
        Intent intent = new Intent("com.lightalarm.app.SUNRISE_UPDATE");
        intent.putExtra("progress", progress);
        sendBroadcast(intent);
        Log.d(TAG, "🔍 DEBUG: Broadcasted progress: " + progress);
    }
    
    private void launchMainActivity() {
        // Acquire temporary wake lock to turn screen on BEFORE launching activity
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock screenWakeLock = pm.newWakeLock(
            PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "SunriseService::ScreenOn"
        );
        screenWakeLock.acquire(5000); // 5 seconds to ensure activity starts
        
        Intent activityIntent = new Intent(this, MainActivity.class);
        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
            | Intent.FLAG_ACTIVITY_CLEAR_TOP
            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        
        activityIntent.putExtra("alarm_triggered", "light");
        activityIntent.putExtra("launch_fullscreen", true);
        activityIntent.putExtra("alarm_sound", alarmSound);
        
        try {
            startActivity(activityIntent);
            Log.d(TAG, "MainActivity launched for sunrise with screen wake");
        } finally {
            // Release after a short delay to ensure activity is visible
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (screenWakeLock.isHeld()) {
                    screenWakeLock.release();
                }
            }, 3000);
        }
    }
    
    private boolean isMainActivityRunning() {
        // Simplified check - avoid deprecated getRunningTasks() which requires special permissions
        // For now, we'll assume MainActivity needs to be launched
        // This prevents crashes from permission issues
        return false;
    }
    
    private void triggerSoundAlarm() {
        // Start AlarmService for sound
        Intent serviceIntent = new Intent(this, AlarmService.class);
        serviceIntent.putExtra("alarm_type", "sound");
        serviceIntent.putExtra("alarm_sound", alarmSound);
        startService(serviceIntent);
        Log.d(TAG, "Sound alarm triggered");
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Sunrise Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Background sunrise simulation");
            channel.setShowBadge(false);
            channel.setLockscreenVisibility(Notification.VISIBILITY_SECRET); // Hidden from lock screen
            channel.setBypassDnd(false); // Don't bypass do not disturb
            channel.enableLights(false); // No LED
            channel.enableVibration(false); // No vibration
            channel.setSound(null, null); // No sound
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    
    private Notification buildSilentNotification() {
        // Create a silent notification that doesn't show to user
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Light Alarm")
            .setContentText("Background service")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setOngoing(true)
            .setSilent(true) // Silent notification
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW) // Low priority = no popup
            .setShowWhen(false) // Don't show timestamp
            .build();
    }
    
    @Override
    public void onDestroy() {
        Log.d(TAG, "SunriseService destroyed");
        isRunning = false;

        // Restore original brightness
        if (originalBrightness != -1) {
            try {
                setScreenBrightness(originalBrightness);
                Log.d(TAG, "Original screen brightness restored: " + originalBrightness);
            } catch (Exception e) {
                Log.e(TAG, "Failed to restore original brightness", e);
            }
        }

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            Log.d(TAG, "Wake lock released");
        }
        stopForeground(true);
        super.onDestroy();
    }

    // Get current screen brightness (0-255)
    private int getScreenBrightness() {
        try {
            return Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Failed to get screen brightness", e);
            return 128; // Default brightness
        }
    }

    // Set screen brightness (0-255)
    private void setScreenBrightness(int brightness) {
        try {
            // Check if we can write settings
            if (Settings.System.canWrite(this)) {
                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
                Log.d(TAG, "Screen brightness set to: " + brightness);
            } else {
                Log.w(TAG, "Cannot write settings - WRITE_SETTINGS permission not granted");
                Log.w(TAG, "Brightness control requires WRITE_SETTINGS permission");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to set screen brightness to: " + brightness, e);
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Static helper: stop sunrise service
    public static void stopSunrise(Context context) {
        try {
            context.stopService(new Intent(context, SunriseService.class));
            Log.d(TAG, "SunriseService stop requested by JS bridge");
        } catch (Exception e) {
            Log.e(TAG, "Failed to stop SunriseService", e);
        }
    }
}
