package com.lightalarm.app;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.ActivityCallback;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.app.NotificationManager;
import android.app.Activity;
import androidx.activity.result.ActivityResult;

import com.getcapacitor.JSArray;

import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONObject;

@CapacitorPlugin(name = "AlarmService")
public class AlarmServicePlugin extends Plugin {
    private static final String TAG = "AlarmServicePlugin";

    private static final String PREFS_NAME = "LightAlarmPrefs";
    private static final String ALARMS_KEY = "saved_alarms";
    private static final String ALARM_TIME_KEY = "alarm_time";
    private static final String ALARM_ENABLED_KEY = "alarm_enabled";
    private static final String ALARM_SOUND_KEY = "alarm_sound";

    @PluginMethod
    public void scheduleAlarm(PluginCall call) {
        Log.d(TAG, "🔔 scheduleAlarm called from JavaScript!");
        try {
            // Check fullscreen intent permission on Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!canUseFullScreenIntent()) {
                    Log.w(TAG, "⚠️ USE_FULL_SCREEN_INTENT permission not granted - fullscreen alarm may not work");
                    // Don't block scheduling, but warn user
                }
            }
            
            // Extract individual alarm parameters
            String alarmId = call.getString("id");
            int hour = call.getInt("hour");
            int minute = call.getInt("minute");
            String amPm = call.getString("amPm");
            String soundResourceName = call.getString("soundResourceName", "classicalarm_digital_alarm");
            String theme = call.getString("theme", "sunrise");
            JSArray repeatDays = call.getArray("repeatDays", new JSArray());

            Log.d(TAG, "📅 ALARM SCHEDULING DETAILS:");
            Log.d(TAG, "   🆔 Alarm ID: " + alarmId);
            Log.d(TAG, "   🕐 Time: " + hour + ":" + String.format("%02d", minute) + " " + amPm);
            Log.d(TAG, "   🎵 Sound: " + soundResourceName);
            Log.d(TAG, "   🎨 Theme: " + theme);
            Log.d(TAG, "   🔄 Repeat days: " + repeatDays.toString());

            // Convert to 24-hour format and calculate alarm time
            int targetHour = hour;
            if (amPm.equals("PM") && hour != 12) {
                targetHour += 12;
            } else if (amPm.equals("AM") && hour == 12) {
                targetHour = 0;
            }

            // Calculate next occurrence of this alarm time
            Calendar alarmCal = Calendar.getInstance();
            alarmCal.set(Calendar.HOUR_OF_DAY, targetHour);
            alarmCal.set(Calendar.MINUTE, minute);
            alarmCal.set(Calendar.SECOND, 0);
            alarmCal.set(Calendar.MILLISECOND, 0);

            // If the time has passed today, set for tomorrow
            if (alarmCal.getTimeInMillis() <= System.currentTimeMillis()) {
                alarmCal.add(Calendar.DAY_OF_MONTH, 1);
            }

            long alarmTime = alarmCal.getTimeInMillis();
            Date alarmDate = new Date(alarmTime);
            Date now = new Date();

            Log.d(TAG, "   🕐 Current time: " + now.toString());
            Log.d(TAG, "   ⏰ Target alarm time: " + alarmDate.toString());
            Log.d(TAG, "   🎯 Alarm timestamp (ms): " + alarmTime);
            Log.d(TAG, "   📊 Time difference: " + (alarmTime - now.getTime()) + "ms (" + ((alarmTime - now.getTime()) / 1000 / 60) + " minutes)");

            // Save alarm to SharedPreferences (multiple alarms support)
            saveAlarmToPrefs(alarmId, hour, minute, amPm, soundResourceName, theme, repeatDays);

            // Call our native AlarmService with new parameters
            AlarmService.scheduleAlarm(getContext(), Long.parseLong(alarmId), targetHour, minute, amPm, soundResourceName, theme);
            
            JSObject result = new JSObject();
            result.put("success", true);
            result.put("message", "Alarm scheduled successfully");
            result.put("hasFullScreenPermission", canUseFullScreenIntent());
            Log.d(TAG, "✅ Alarm scheduled successfully, resolving call");
            call.resolve(result);
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Error scheduling alarm", e);
            call.reject("Failed to schedule alarm: " + e.getMessage());
        }
    }
    
    // Helper method to save alarm to SharedPreferences (multiple alarms support)
    private void saveAlarmToPrefs(String alarmId, int hour, int minute, String amPm, String soundResourceName, String theme, JSArray repeatDays) {
        try {
            SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String alarmsJson = prefs.getString(ALARMS_KEY, "[]");
            JSONArray alarmsArray = new JSONArray(alarmsJson);
            
            // Create alarm object
            JSONObject alarmObj = new JSONObject();
            alarmObj.put("id", alarmId);
            alarmObj.put("hour", hour);
            alarmObj.put("minute", minute);
            alarmObj.put("amPm", amPm);
            alarmObj.put("soundResourceName", soundResourceName);
            alarmObj.put("theme", theme);
            alarmObj.put("enabled", true);
            
            // Convert JSArray to JSONArray for repeatDays
            JSONArray repeatDaysJson = new JSONArray();
            for (int i = 0; i < repeatDays.length(); i++) {
                repeatDaysJson.put(repeatDays.getString(i));
            }
            alarmObj.put("repeatDays", repeatDaysJson);
            
            // Remove existing alarm with same ID if it exists
            for (int i = 0; i < alarmsArray.length(); i++) {
                JSONObject existingAlarm = alarmsArray.getJSONObject(i);
                if (existingAlarm.getString("id").equals(alarmId)) {
                    alarmsArray.remove(i);
                    break;
                }
            }
            
            // Add new alarm
            alarmsArray.put(alarmObj);
            
            // Save back to SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(ALARMS_KEY, alarmsArray.toString());
            editor.apply();
            
            Log.d(TAG, "💾 Alarm saved to SharedPreferences: " + alarmObj.toString());
        } catch (Exception e) {
            Log.e(TAG, "❌ Error saving alarm to preferences", e);
        }
    }
    
    // Helper method to remove alarm from SharedPreferences
    private void removeAlarmFromPrefs(String alarmId) {
        try {
            SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String alarmsJson = prefs.getString(ALARMS_KEY, "[]");
            JSONArray alarmsArray = new JSONArray(alarmsJson);
            
            // Remove alarm with matching ID
            for (int i = 0; i < alarmsArray.length(); i++) {
                JSONObject alarm = alarmsArray.getJSONObject(i);
                if (alarm.getString("id").equals(alarmId)) {
                    alarmsArray.remove(i);
                    break;
                }
            }
            
            // Save back to SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(ALARMS_KEY, alarmsArray.toString());
            editor.apply();
            
            Log.d(TAG, "🗑️ Alarm removed from SharedPreferences: " + alarmId);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error removing alarm from preferences", e);
        }
    }

    @PluginMethod
    public void checkFullScreenPermission(PluginCall call) {
        try {
            boolean hasPermission = canUseFullScreenIntent();
            
            JSObject result = new JSObject();
            result.put("granted", hasPermission);
            call.resolve(result);
            
            Log.d(TAG, "Fullscreen intent permission: " + (hasPermission ? "GRANTED" : "DENIED"));
        } catch (Exception e) {
            Log.e(TAG, "Error checking fullscreen permission", e);
            call.reject("Failed to check permission: " + e.getMessage());
        }
    }
    
    @PluginMethod
    public void requestFullScreenPermission(PluginCall call) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Open settings page for USE_FULL_SCREEN_INTENT permission
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT);
                intent.setData(Uri.parse("package:" + getContext().getPackageName()));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
                
                JSObject result = new JSObject();
                result.put("success", true);
                result.put("message", "Settings opened - please enable 'Appear on top' permission");
                call.resolve(result);
                
                Log.d(TAG, "Opened fullscreen intent permission settings");
            } else {
                JSObject result = new JSObject();
                result.put("success", true);
                result.put("message", "Permission not required on Android < 13");
                call.resolve(result);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error opening permission settings", e);
            call.reject("Failed to open settings: " + e.getMessage());
        }
    }
    
    @PluginMethod
    public void checkPermissions(PluginCall call) {
        Log.d(TAG, "checkPermissions() called");
        
        JSObject result = new JSObject();
        JSArray missing = new JSArray();
        
        // Check POST_NOTIFICATIONS (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (getContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                missing.put("Notifications");
            }
        }
        
        // Check USE_FULL_SCREEN_INTENT (Android 14+ only - API 34)
        if (Build.VERSION.SDK_INT >= 34) {
            NotificationManager nm = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            try {
                Method m = nm.getClass().getMethod("canUseFullScreenIntent");
                boolean canUse = (boolean) m.invoke(nm);
                if (!canUse) {
                    missing.put("Full Screen Alarms");
                }
            } catch (Exception e) {
                Log.w(TAG, "Could not check full screen intent permission", e);
            }
        }
        
        // Check SYSTEM_ALERT_WINDOW (Display over other apps)
        if (!Settings.canDrawOverlays(getContext())) {
            missing.put("Display Over Other Apps");
        }
        
        // Check battery optimization
        PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
        if (!pm.isIgnoringBatteryOptimizations(getContext().getPackageName())) {
            missing.put("Battery Optimization");
        }
        
        result.put("allGranted", missing.length() == 0);
        result.put("missing", missing);
        call.resolve(result);
    }
    
    @PluginMethod
    public void requestAllPermissions(PluginCall call) {
        Log.d(TAG, "requestAllPermissions() called");
        
        Intent intent = new Intent(getContext(), PermissionSetupActivity.class);
        
        // Use Activity context if available, otherwise use application context with NEW_TASK flag
        if (getActivity() != null) {
            getActivity().startActivity(intent);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
        }
        
        JSObject result = new JSObject();
        result.put("success", true);
        call.resolve(result);
    }
    
    private boolean canUseFullScreenIntent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+ (API 34+)
            android.app.NotificationManager nm = (android.app.NotificationManager) 
                getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            return nm.canUseFullScreenIntent();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 (API 33): Check if permission is in manifest (already declared)
            // On Android 13, it's granted by default if in manifest
            return true;
        } else {
            // Android 12 and below: Always allowed
            return true;
        }
    }
    
    @PluginMethod
    public void getAllAlarms(PluginCall call) {
        try {
            Log.d(TAG, "Getting all saved alarms");
            
            SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String alarmsJson = prefs.getString(ALARMS_KEY, "[]");
            JSONArray alarmsArray = new JSONArray(alarmsJson);
            
            JSArray alarmsJS = new JSArray();
            for (int i = 0; i < alarmsArray.length(); i++) {
                JSONObject alarmJson = alarmsArray.getJSONObject(i);
                JSObject alarmJS = new JSObject();
                alarmJS.put("id", alarmJson.getString("id"));
                alarmJS.put("hour", alarmJson.getInt("hour"));
                alarmJS.put("minute", alarmJson.getInt("minute"));
                alarmJS.put("amPm", alarmJson.getString("amPm"));
                alarmJS.put("soundResourceName", alarmJson.getString("soundResourceName"));
                alarmJS.put("theme", alarmJson.getString("theme"));
                alarmJS.put("enabled", alarmJson.getBoolean("enabled"));
                
                // Convert repeatDays JSONArray to JSArray
                JSONArray repeatDaysJson = alarmJson.getJSONArray("repeatDays");
                JSArray repeatDaysJS = new JSArray();
                for (int j = 0; j < repeatDaysJson.length(); j++) {
                    repeatDaysJS.put(repeatDaysJson.getString(j));
                }
                alarmJS.put("repeatDays", repeatDaysJS);
                
                alarmsJS.put(alarmJS);
            }
            
            JSObject result = new JSObject();
            result.put("alarms", alarmsJS);
            call.resolve(result);
            
            Log.d(TAG, "Returned " + alarmsArray.length() + " saved alarms");
        } catch (Exception e) {
            Log.e(TAG, "Error getting all alarms", e);
            call.reject("Failed to get alarms: " + e.getMessage());
        }
    }

    @PluginMethod
    public void cancelAlarm(PluginCall call) {
        try {
            String alarmId = call.getString("id");
            if (alarmId == null) {
                call.reject("Alarm ID is required");
                return;
            }
            
            Log.d(TAG, "Cancelling alarm: " + alarmId);

            // Remove alarm from SharedPreferences
            removeAlarmFromPrefs(alarmId);

            // Call our native AlarmService to cancel specific alarm
            AlarmService.cancelAlarm(getContext(), Long.parseLong(alarmId));

            JSObject result = new JSObject();
            result.put("success", true);
            result.put("message", "Alarm cancelled successfully");
            call.resolve(result);

        } catch (Exception e) {
            Log.e(TAG, "Error cancelling alarm", e);
            call.reject("Failed to cancel alarm: " + e.getMessage());
        }
    }

    @PluginMethod
    public void getSoundCategories(PluginCall call) {
        try {
            Log.d(TAG, "Getting sound categories");
            SoundManager sm = SoundManager.getInstance(getContext());
            List<String> categories = sm.getCategories();

            JSArray categoriesJS = new JSArray();
            for (String category : categories) {
                categoriesJS.put(category);
            }

            JSObject result = new JSObject();
            result.put("categories", categoriesJS);
            call.resolve(result);

            Log.d(TAG, "Returned " + categories.size() + " sound categories");
        } catch (Exception e) {
            Log.e(TAG, "Error getting sound categories", e);
            call.reject("Failed to get sound categories: " + e.getMessage());
        }
    }

    @PluginMethod
    public void getSoundsForCategory(PluginCall call) {
        try {
            String category = call.getString("category");
            if (category == null) {
                call.reject("Category parameter is required");
                return;
            }

            Log.d(TAG, "Getting sounds for category: " + category);
            SoundManager sm = SoundManager.getInstance(getContext());
            List<Sound> sounds = sm.getSoundsForCategory(category);

            JSArray soundsJS = new JSArray();
            for (Sound sound : sounds) {
                JSObject soundObj = new JSObject();
                soundObj.put("displayName", sound.getDisplayName());
                soundObj.put("resourceName", sound.getResourceName());
                soundsJS.put(soundObj);
            }

            JSObject result = new JSObject();
            result.put("sounds", soundsJS);
            call.resolve(result);

            Log.d(TAG, "Returned " + sounds.size() + " sounds for category: " + category);
        } catch (Exception e) {
            Log.e(TAG, "Error getting sounds for category", e);
            call.reject("Failed to get sounds for category: " + e.getMessage());
        }
    }

    @PluginMethod
    public void openSoundSelection(PluginCall call) {
        // Save the call to use it later when the activity returns a result
        saveCall(call);
        Intent intent = new Intent(getContext(), SoundSelectionActivity.class);
        // Start the activity and specify the callback method for the result
        startActivityForResult(call, intent, "handleSoundSelectionResult");
    }

    @ActivityCallback
    private void handleSoundSelectionResult(PluginCall call, ActivityResult result) {
        if (call == null) {
            return;
        }
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            String soundName = data.getStringExtra("selected_sound_name");
            String resourceName = data.getStringExtra("selected_sound_resource");

            JSObject ret = new JSObject();
            ret.put("displayName", soundName);
            ret.put("resourceName", resourceName);
            call.resolve(ret);
        } else {
            call.reject("No sound selected");
        }
    }
}
