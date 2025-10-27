package com.lightalarm.app;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.content.pm.ApplicationInfo;
import android.widget.Toast;
import com.getcapacitor.BridgeActivity;
import com.getcapacitor.Bridge;

public class MainActivity extends BridgeActivity {
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 100;
    private BroadcastReceiver sunriseUpdateReceiver;
    private boolean receiverRegistered = false;
    private boolean wakeScreenShown = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Enable WebView debugging for troubleshooting
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }
        
        super.onCreate(savedInstanceState);

        
        // Register our custom AlarmService plugin (backup method)
        try {
            registerPlugin(AlarmServicePlugin.class);
            android.util.Log.d("MainActivity", "AlarmServicePlugin registered successfully");
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Failed to register AlarmServicePlugin", e);
        }

        // Check if launched by alarm
        handleAlarmIntent(getIntent());

        // Keep screen on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Check overlay permission for fullscreen over lockscreen
        checkOverlayPermission();

        // Register sunrise progress receiver in onStart() when bridge is ready

        // Note: We don't hide system UI on normal app launch
        // Fullscreen only happens when alarm triggers or sleep mode starts
    }

    @Override
    public void onStart() {
        super.onStart();

        // Register sunrise progress receiver when bridge is ready
        registerSunriseReceiver();

        // Reset wake overlay flag when activity restarts
        wakeScreenShown = false;

    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        android.util.Log.d("MainActivity", "📱 onNewIntent called - updating current intent");
        setIntent(intent); // CRITICAL: Update the current intent so getIntent() returns the new one
        handleAlarmIntent(intent);
    }

    private void handleAlarmIntent(Intent intent) {
        if (intent != null) {
            String alarmTriggered = intent.getStringExtra("alarm_triggered");
            boolean launchFullscreen = intent.getBooleanExtra("launch_fullscreen", false);
            String alarmSound = intent.getStringExtra("alarm_sound");
            boolean fromAlarm = intent.getBooleanExtra("fromAlarm", false);

            android.util.Log.d("MainActivity", "🔍 DEBUG: ALARM INTENT RECEIVED");
            android.util.Log.d("MainActivity", "🔍 DEBUG: Intent: " + intent.toString());
            android.util.Log.d("MainActivity", "🔍 DEBUG: Alarm type: " + alarmTriggered);
            android.util.Log.d("MainActivity", "🔍 DEBUG: Launch fullscreen: " + launchFullscreen);
            android.util.Log.d("MainActivity", "🔍 DEBUG: Alarm sound: " + alarmSound);
            android.util.Log.d("MainActivity", "🔍 DEBUG: Activity launch time: " + new java.util.Date(System.currentTimeMillis()));
            android.util.Log.d("MainActivity", "🔍 DEBUG: Bridge status: " + (getBridge() != null ? "available" : "null"));
            android.util.Log.d("MainActivity", "🔍 DEBUG: WebView status: " + (getBridge() != null && getBridge().getWebView() != null ? "ready" : "not ready"));

            if (launchFullscreen && alarmTriggered != null) {
                android.util.Log.d("MainActivity", "🔍 DEBUG: Launching fullscreen mode for alarm: " + alarmTriggered);

                // Enable fullscreen for alarm
                hideSystemUI();

                // Force fullscreen and dismiss keyguard
                setShowWhenLocked(true);
                setTurnScreenOn(true);

                // Make sure screen stays on and app is fullscreen
                getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
                );

                // Dismiss keyguard (unlock screen)
                android.app.KeyguardManager keyguardManager = (android.app.KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                if (keyguardManager != null) {
                    keyguardManager.requestDismissKeyguard(this, null);
                    android.util.Log.d("MainActivity", "Keyguard dismissal requested");
                } else {
                    android.util.Log.w("MainActivity", "KeyguardManager is null");
                }

                android.util.Log.d("MainActivity", "App launched fullscreen with alarm: " + alarmTriggered);
                
                // Cancel the alarm notification since user has acknowledged it
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(999); // Cancel the alarm notification
                android.util.Log.d("MainActivity", "Alarm notification cancelled");

                // If launched directly from alarm (not via notification), trigger JS event
                if (fromAlarm) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Bridge bridge = getBridge();
                                if (bridge != null && bridge.getWebView() != null) {
                                    bridge.triggerWindowJSEvent("alarmFired");
                                    android.util.Log.d("MainActivity", "alarmFired event dispatched to JavaScript");
                                }
                            } catch (Exception e) {
                                android.util.Log.e("MainActivity", "Failed to send alarmFired event", e);
                            }
                        }
                    }, 500);
                }

                // Notify WebView to present wake overlay (Gentle Wakeup style)
                // Use a delayed dispatch to ensure WebView is ready
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Bridge bridge = getBridge();
                            if (bridge != null && bridge.getWebView() != null) {
                                String jsCode = "try { " +
                                        "if (window.dispatchEvent) { " +
                                        "window.dispatchEvent(new CustomEvent('showWakeScreen', { detail: { type: '" + alarmTriggered + "' } })); " +
                                        "console.log('showWakeScreen event dispatched'); " +
                                        "} " +
                                        "} catch(e) { console.log('Error sending showWakeScreen:', e); }";
                                bridge.eval(jsCode, null);
                                android.util.Log.d("MainActivity", "🔍 DEBUG: showWakeScreen event sent to JavaScript (delayed)");
                            } else {
                                android.util.Log.w("MainActivity", "Bridge not ready to send showWakeScreen event (delayed)");
                            }
                        } catch (Exception e) {
                            android.util.Log.e("MainActivity", "Failed to send showWakeScreen event (delayed)", e);
                        }
                    }
                }, 1000); // 1 second delay to ensure WebView is ready
            } else {
                android.util.Log.w("MainActivity", "Not launching fullscreen - launchFullscreen: " + launchFullscreen + ", alarmTriggered: " + alarmTriggered);
            }
        } else {
            android.util.Log.w("MainActivity", "handleAlarmIntent called with null intent");
        }
    }

    // Note: Removed onWindowFocusChanged to prevent fullscreen on normal app use
    // Fullscreen is only enabled when alarm triggers or sleep mode starts

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
        );
    }

    public void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                
                android.util.Log.d("MainActivity", "Requesting POST_NOTIFICATIONS permission");
                requestPermissions(
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 
                    REQUEST_CODE_POST_NOTIFICATIONS
                );
            } else {
                android.util.Log.d("MainActivity", "POST_NOTIFICATIONS already granted");
            }
        } else {
            android.util.Log.d("MainActivity", "Android < 13, notification permission not required");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                android.util.Log.d("MainActivity", "Notification permission granted by user");
            } else {
                android.util.Log.w("MainActivity", "Notification permission denied by user - alarms may not work");
                Toast.makeText(this, "Notification permission required for alarms to work", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void registerSunriseReceiver() {
        if (receiverRegistered || isFinishing()) {
            android.util.Log.d("MainActivity", "Receiver already registered or activity finishing, skipping");
            return;
        }
        
        sunriseUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int progress = intent.getIntExtra("progress", 0);
                android.util.Log.d("MainActivity", "Received sunrise progress: " + progress);
                
                // Send to JavaScript via bridge
                try {
                    Bridge bridge = getBridge();
                    if (bridge != null && bridge.getWebView() != null) {
                        // Check if WebView is ready with safer JavaScript
                        String jsCode = "try { " +
                            "if (window.dispatchEvent) { " +
                            "window.dispatchEvent(new CustomEvent('sunriseProgress', " +
                            "{ detail: { progress: " + progress + " } })); " +
                            "} " +
                            "} catch(e) { console.log('Error sending sunrise progress:', e); }";
                        bridge.eval(jsCode, null);
                        android.util.Log.d("MainActivity", "🔍 DEBUG: Sent progress to JavaScript: " + progress);

                        // Ensure wake overlay is shown once when progress starts
                        if (!wakeScreenShown) {
                            // Use delayed dispatch to ensure WebView is ready
                            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Bridge delayedBridge = getBridge();
                                        if (delayedBridge != null && delayedBridge.getWebView() != null) {
                                            String showJs = "try { if (window.dispatchEvent) {" +
                                                "window.dispatchEvent(new CustomEvent('showWakeScreen', { detail: { type: 'light' } }));" +
                                                "console.log('showWakeScreen dispatched on first progress');" +
                                                "} } catch(e) { console.log('Error sending showWakeScreen:', e); }";
                                            delayedBridge.eval(showJs, null);
                                            android.util.Log.d("MainActivity", "🔍 DEBUG: showWakeScreen dispatched on first progress (delayed)");
                                        }
                                    } catch (Exception e) {
                                        android.util.Log.e("MainActivity", "Failed to dispatch showWakeScreen on progress", e);
                                    }
                                }
                            }, 500); // 500ms delay
                            wakeScreenShown = true;
                        }
                    } else {
                        android.util.Log.w("MainActivity", "Bridge or WebView not ready, skipping progress update");
                    }
                } catch (Exception e) {
                    android.util.Log.e("MainActivity", "Failed to send progress to JavaScript", e);
                }
            }
        };
        
        try {
            IntentFilter filter = new IntentFilter("com.lightalarm.app.SUNRISE_UPDATE");
            registerReceiver(sunriseUpdateReceiver, filter);
            receiverRegistered = true;
            android.util.Log.d("MainActivity", "Sunrise receiver registered");
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Failed to register sunrise receiver", e);
            receiverRegistered = false;
        }
    }

    // Send light simulation event to JavaScript (like Gentle Wakeup)
    public void sendLightSimulationEvent(String alarmSound) {
        android.util.Log.d("MainActivity", "Sending light simulation event to JavaScript");
        try {
            Bridge bridge = getBridge();
            if (bridge != null && bridge.getWebView() != null) {
                String jsCode = "try { " +
                    "if (window.dispatchEvent) { " +
                    "window.dispatchEvent(new CustomEvent('com.lightalarm.app.DIRECT_LIGHT_SIMULATION', " +
                    "{ detail: { alarmSound: '" + alarmSound + "' } })); " +
                    "} " +
                    "} catch(e) { console.log('Error sending light simulation event:', e); }";
                bridge.eval(jsCode, null);
                android.util.Log.d("MainActivity", "Light simulation event sent to JavaScript");
            } else {
                android.util.Log.w("MainActivity", "Bridge not ready for light simulation event");
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Failed to send light simulation event", e);
        }
    }

    // Send sound alarm event to JavaScript (like Gentle Wakeup)
    public void sendSoundAlarmEvent(String alarmSound) {
        android.util.Log.d("MainActivity", "Sending sound alarm event to JavaScript");
        try {
            Bridge bridge = getBridge();
            if (bridge != null && bridge.getWebView() != null) {
                String jsCode = "try { " +
                    "if (window.dispatchEvent) { " +
                    "window.dispatchEvent(new CustomEvent('com.lightalarm.app.DIRECT_SOUND_ALARM', " +
                    "{ detail: { alarmSound: '" + alarmSound + "' } })); " +
                    "} " +
                    "} catch(e) { console.log('Error sending sound alarm event:', e); }";
                bridge.eval(jsCode, null);
                android.util.Log.d("MainActivity", "Sound alarm event sent to JavaScript");
            } else {
                android.util.Log.w("MainActivity", "Bridge not ready for sound alarm event");
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Failed to send sound alarm event", e);
        }
    }

    private void checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!android.provider.Settings.canDrawOverlays(this)) {
                android.util.Log.w("MainActivity", "Overlay permission not granted");
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }

    @Override
    public void onDestroy() {
        if (sunriseUpdateReceiver != null && receiverRegistered) {
            unregisterReceiver(sunriseUpdateReceiver);
            receiverRegistered = false;
            android.util.Log.d("MainActivity", "Sunrise receiver unregistered");
        }
        super.onDestroy();
    }
}
