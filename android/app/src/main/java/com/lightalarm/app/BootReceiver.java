package com.lightalarm.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";
    private static final String PREFS_NAME = "LightAlarmPrefs";
    private static final String ALARMS_KEY = "saved_alarms";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed, checking for scheduled alarms");
            
            try {
                // Get saved alarms from SharedPreferences
                SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                String alarmsJson = prefs.getString(ALARMS_KEY, "[]");
                JSONArray alarmsArray = new JSONArray(alarmsJson);
                
                if (alarmsArray.length() > 0) {
                    Log.d(TAG, "Rescheduling " + alarmsArray.length() + " alarms after boot");
                    
                    // Reschedule each saved alarm
                    for (int i = 0; i < alarmsArray.length(); i++) {
                        JSONObject alarmJson = alarmsArray.getJSONObject(i);
                        if (alarmJson.getBoolean("enabled")) {
                            long alarmId = Long.parseLong(alarmJson.getString("id"));
                            int hour = alarmJson.getInt("hour");
                            int minute = alarmJson.getInt("minute");
                            String amPm = alarmJson.getString("amPm");
                            String soundResourceName = alarmJson.getString("soundResourceName");
                            String theme = alarmJson.getString("theme");
                            
                            Log.d(TAG, "Rescheduling alarm ID: " + alarmId + " at " + hour + ":" + minute + " " + amPm);
                            AlarmService.scheduleAlarm(context, alarmId, hour, minute, amPm, soundResourceName, theme);
                        }
                    }
                } else {
                    Log.d(TAG, "No alarms to reschedule");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error rescheduling alarms after boot", e);
            }
        }
    }
}

