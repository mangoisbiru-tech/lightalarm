# Native Android Architecture

## Overview
The Light Alarm App is a **hybrid mobile application** built with React + Capacitor, featuring **custom native Android components** for reliable alarm functionality when the phone is locked or screen is off.

## Architecture Layers

```
┌─────────────────────────────────────────┐
│     React UI Layer (src/App.js)         │
│  - User interface                       │
│  - Animation rendering                  │
│  - State management                     │
└─────────────────┬───────────────────────┘
                  │ Capacitor Bridge
┌─────────────────▼───────────────────────┐
│  Native Android Layer (Java)            │
│  - AlarmReceiver.java                   │
│  - MainActivity.java                    │
│  - AlarmService.java                    │
│  - BootReceiver.java                    │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│  Android System (OS)                    │
│  - AlarmManager                         │
│  - PowerManager (wake locks)            │
│  - NotificationManager                  │
│  - KeyguardManager                      │
└─────────────────────────────────────────┘
```

## Native Components

### 1. AlarmReceiver.java
**Purpose:** Receives alarm broadcasts from Android AlarmManager

**Location:** `android/app/src/main/java/com/lightalarm/app/AlarmReceiver.java`

**Responsibilities:**
- Listen for alarm intent broadcasts
- Acquire wake lock (turn screen on)
- Launch MainActivity in fullscreen mode
- Pass alarm metadata (type, sound, etc.)

**Key Features:**
```java
// Acquire wake lock to wake device
PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
    PowerManager.SCREEN_DIM_WAKE_LOCK
    | PowerManager.ACQUIRE_CAUSES_WAKEUP
    | PowerManager.ON_AFTER_RELEASE,
    "LightAlarm::AlarmWakeLock"
);
wakeLock.acquire(10*60*1000L); // 10 minutes

// Launch MainActivity fullscreen
activityIntent.putExtra("launch_fullscreen", true);
context.startActivity(activityIntent);
```

---

### 2. MainActivity.java
**Purpose:** Main app activity that handles fullscreen launch

**Location:** `android/app/src/main/java/com/lightalarm/app/MainActivity.java`

**Responsibilities:**
- Handle alarm intents from AlarmReceiver
- Set showWhenLocked and turnScreenOn flags
- Dismiss keyguard (unlock screen)
- Keep screen on during alarm
- Hide system UI for true fullscreen

**Key Features:**
```java
if (launchFullscreen && alarmTriggered != null) {
    // Force fullscreen and dismiss keyguard
    setShowWhenLocked(true);
    setTurnScreenOn(true);
    
    // Make sure screen stays on
    getWindow().addFlags(
        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
    );
    
    // Dismiss keyguard (unlock screen)
    KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
    keyguardManager.requestDismissKeyguard(this, null);
}
```

**System UI Hiding:**
```java
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
```

---

### 3. AlarmService.java
**Purpose:** Foreground service for alarm execution

**Location:** `android/app/src/main/java/com/lightalarm/app/AlarmService.java`

**Responsibilities:**
- Run as foreground service (prevent killing)
- Acquire wake lock
- Handle vibration patterns
- Create persistent notification

**Key Features:**
- Uses `START_STICKY` to restart if killed
- 10-minute wake lock duration
- Foreground service prevents Android from killing it

---

### 4. BootReceiver.java
**Purpose:** Restore alarms after device reboot

**Location:** `android/app/src/main/java/com/lightalarm/app/BootReceiver.java`

**Responsibilities:**
- Listen for BOOT_COMPLETED broadcast
- Reschedule alarms from saved state
- Ensure alarms persist across reboots

---

## Permissions (AndroidManifest.xml)

### Critical Alarm Permissions
```xml
<!-- Turn screen on -->
<uses-permission android:name="android.permission.WAKE_LOCK" />

<!-- Unlock screen -->
<uses-permission android:name="android.permission.DISABLE_KEYGUARD" />
<uses-permission android:name="android.permission.TURN_SCREEN_ON" />

<!-- Draw over lockscreen -->
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />

<!-- Schedule exact alarms -->
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />

<!-- Prevent battery optimization -->
<uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />

<!-- Notifications -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.VIBRATE" />

<!-- Restore alarms after reboot -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

### Future Permissions (V2.0)
```xml
<!-- Adaptive wake (motion detection) -->
<uses-permission android:name="android.permission.BODY_SENSORS" />
<uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />
```

---

## Alarm Flow: Phone Locked/Off

### Step-by-Step Process

1. **User Sets Alarm**
   - React UI → Capacitor LocalNotifications plugin
   - Schedules TWO alarms:
     - Light alarm (pre-alarm time, e.g., -10 min)
     - Sound alarm (exact alarm time)

2. **Alarm Time Reached (Phone Locked)**
   - Android AlarmManager triggers broadcast
   - `AlarmReceiver.onReceive()` called

3. **Wake Device**
   - AlarmReceiver acquires wake lock
   - Screen turns on (even if phone is locked)
   - Wake lock lasts 10 minutes

4. **Launch Fullscreen**
   - AlarmReceiver creates Intent for MainActivity
   - Intent includes:
     - `launch_fullscreen: true`
     - `alarm_triggered: "light"` or `"sound"`
     - `alarm_sound: "alarm1.wav"` (etc.)
   - MainActivity launched with NEW_TASK flag

5. **Dismiss Keyguard**
   - MainActivity calls `setShowWhenLocked(true)`
   - MainActivity calls `setTurnScreenOn(true)`
   - KeyguardManager dismisses lock screen
   - Screen stays on with FLAG_KEEP_SCREEN_ON

6. **Show Fullscreen UI**
   - MainActivity hides system UI (nav bar, status bar)
   - React WebView loads
   - JavaScript receives alarm notification
   - Fullscreen animation starts

7. **User Wakes Up**
   - Sees fullscreen color animation
   - Hears alarm sound
   - Taps "Stop Light" or "Stop Alarm"
   - App releases wake lock
   - Returns to normal state

---

## Battery Optimization Challenges

### Doze Mode (Android 6+)
- **Problem:** System ignores alarms during deep sleep
- **Solution:** Request `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`
- **User Action Required:** Must manually exempt app in settings

### Exact Alarms (Android 12+)
- **Problem:** Apps need special permission for exact alarms
- **Solution:** `SCHEDULE_EXACT_ALARM` permission
- **User Action:** Grant when prompted

### Manufacturer Restrictions
- **Problem:** Some manufacturers (Samsung, Xiaomi, Huawei) aggressively kill apps
- **Solutions:**
  - Foreground service with persistent notification
  - Battery optimization exemption
  - User education (app instructions)

---

## Testing Checklist

### Native Alarm Testing
- [ ] Set alarm with phone unlocked → alarm triggers
- [ ] Set alarm, lock phone → alarm wakes device
- [ ] Set alarm, turn screen off → alarm turns screen on
- [ ] Set alarm, put in pocket → alarm wakes device
- [ ] Alarm triggers during Doze mode
- [ ] Alarm triggers after reboot
- [ ] Multiple alarms work correctly
- [ ] Alarm triggers with different alarm sounds
- [ ] Screen stays on for full alarm duration
- [ ] Keyguard dismissed properly
- [ ] System UI hidden (true fullscreen)

### Permission Testing
- [ ] Notification permission granted
- [ ] Exact alarm permission granted (Android 12+)
- [ ] Battery optimization disabled
- [ ] Draw over other apps enabled
- [ ] All permissions survive app updates

---

## Known Limitations

### Current Limitations
1. **No iOS Support** - iOS doesn't allow true background alarms from third-party apps
2. **Manufacturer Variations** - Some Android skins may still kill the app
3. **User Action Required** - Battery optimization exemption needs manual approval
4. **Wake Lock Duration** - Limited to 10 minutes (configurable)

### Future Improvements
- Smart wake lock release (when user interacts)
- Adaptive wake based on motion sensors
- Notification reminder if battery optimization not disabled
- Testing mode to verify alarm will work

---

## Debugging Native Code

### View Logs
```bash
# Real-time logs from device
adb logcat | grep -i "LightAlarm\|AlarmReceiver\|MainActivity"

# Filter by tag
adb logcat AlarmReceiver:D MainActivity:D *:S
```

### Key Log Messages
- `🔔 TRUE NATIVE ALARM RECEIVED!` - AlarmReceiver got broadcast
- `🚀 Launching MainActivity fullscreen` - About to launch app
- `🔥 TRUE NATIVE ALARM TRIGGERED!` - MainActivity received intent
- `✅ App launched fullscreen with alarm` - Fullscreen mode activated

### Test Alarm Manually
```bash
# Trigger alarm broadcast (for testing)
adb shell am broadcast -a com.lightalarm.app.LIGHT_ALARM \
  --es alarm_type "light" \
  --es alarm_sound "alarm1.wav"
```

---

## File Locations

### Native Java Files
```
android/app/src/main/java/com/lightalarm/app/
├── MainActivity.java          # Main activity
├── AlarmReceiver.java         # Alarm broadcast receiver
├── AlarmService.java          # Foreground service
└── BootReceiver.java          # Boot broadcast receiver
```

### Android Configuration
```
android/app/src/main/
├── AndroidManifest.xml        # Permissions, components
└── res/                       # Android resources
```

### React Integration
```
src/App.js (lines 140-178)    # LocalNotifications listener
```

---

## Summary

The Light Alarm App has **robust native Android alarm handling** that works reliably even when the phone is locked or the screen is off. The hybrid architecture combines React's UI capabilities with native Java code for system-level alarm functionality.

**Key Success Factors:**
1. ✅ Custom AlarmReceiver for immediate wake lock acquisition
2. ✅ MainActivity with proper window flags for fullscreen
3. ✅ Comprehensive permissions for all required features
4. ✅ Foreground service to prevent system killing
5. ✅ Boot receiver to restore alarms after reboot

**Testing Priority:** Physical device testing with phone locked is essential to verify real-world reliability.




