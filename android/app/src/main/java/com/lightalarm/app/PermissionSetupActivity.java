package com.lightalarm.app;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

public class PermissionSetupActivity extends Activity {
    private static final String TAG = "PermissionSetup";
    private static final int REQUEST_NOTIFICATIONS = 1;
    private static final int REQUEST_OVERLAY = 2;
    private static final int REQUEST_FULLSCREEN = 3;
    private static final int REQUEST_BATTERY = 4;
    
    private int currentStep = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Permission setup activity started");
        
        // Start permission flow
        nextPermission();
    }
    
    private void nextPermission() {
        currentStep++;
        Log.d(TAG, "Permission step: " + currentStep);
        
        switch (currentStep) {
            case 1:
                requestNotificationPermission();
                break;
            case 2:
                requestFullScreenPermission();
                break;
            case 3:
                requestOverlayPermission();
                break;
            case 4:
                requestBatteryOptimization();
                break;
            default:
                // All permissions requested
                Log.d(TAG, "All permissions requested, closing activity");
                finish();
                break;
        }
    }
    
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Requesting POST_NOTIFICATIONS");
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATIONS);
            } else {
                Log.d(TAG, "Notifications already granted");
                nextPermission();
            }
        } else {
            // Not needed on Android < 13
            nextPermission();
        }
    }
    
    private void requestFullScreenPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (!nm.canUseFullScreenIntent()) {
                Log.d(TAG, "Opening fullscreen intent settings");
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, REQUEST_FULLSCREEN);
                } catch (Exception e) {
                    Log.e(TAG, "Failed to open fullscreen settings", e);
                    nextPermission();
                }
            } else {
                Log.d(TAG, "Fullscreen already granted");
                nextPermission();
            }
        } else {
            // Not needed on Android < 13
            nextPermission();
        }
    }
    
    private void requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Log.d(TAG, "Opening overlay permission settings");
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_OVERLAY);
            } catch (Exception e) {
                Log.e(TAG, "Failed to open overlay settings", e);
                nextPermission();
            }
        } else {
            Log.d(TAG, "Overlay already granted");
            nextPermission();
        }
    }
    
    private void requestBatteryOptimization() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
            Log.d(TAG, "Opening battery optimization settings");
            try {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_BATTERY);
            } catch (Exception e) {
                Log.e(TAG, "Failed to open battery settings", e);
                nextPermission();
            }
        } else {
            Log.d(TAG, "Battery optimization already disabled");
            nextPermission();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Notification permission granted");
            } else {
                Log.d(TAG, "Notification permission denied");
            }
            nextPermission();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Activity result for request: " + requestCode);
        
        // Move to next permission regardless of result
        nextPermission();
    }
}

