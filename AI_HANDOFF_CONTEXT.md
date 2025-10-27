# Light Alarm App - AI Assistance Request

## Project Overview

**What I'm Building:**
- Native Android alarm app using React + Capacitor
- Gradual light transitions (animated colors) to wake users naturally
- Similar to "Gentle Wakeup: Sun Alarm Clock"
- **CRITICAL:** Must work when phone is locked/screen is off

**Tech Stack:**
- Frontend: React (JavaScript) in WebView
- Backend: Native Android (Java)
- Bridge: Capacitor
- Target: Android only

---

## Current Status

### ✅ What Works
- UI renders correctly (themes, animations, settings)
- Alarm time can be set
- Light simulation shows colors (when app is open)
- Swipe gestures work on wake overlay
- APK builds successfully

### ❌ What's Broken (Current Crisis)
1. **App crashes when trying to save alarm**
2. **Permission modal doesn't appear**
3. **Alarm doesn't trigger reliably when phone is locked**
4. **"Appear on top" permission never requested**

---

## What We Tried Today

### Goal: Add Gentle Wakeup-style Permission Request Flow

**Implementation:**
1. Created `checkAllPermissions()` in JavaScript
2. Added `checkPermissions()` and `requestAllPermissions()` to `AlarmServicePlugin.java`
3. Created `PermissionSetupActivity.java` to guide user through permissions
4. Added permission modal UI in React
5. Modified "Save Alarm" button to check permissions first

**Result:** 
- ❌ App crashes immediately
- ❌ Permission modal never appears
- ❌ No errors logged (or we don't know how to check)

---

## Architecture

### Alarm Flow (How It Should Work)

```
USER SETS ALARM (e.g., 7:00 AM)
         ↓
JavaScript calls: scheduleNativeAlarm()
         ↓
AlarmServicePlugin.scheduleAlarm() [Java]
         ↓
AlarmService.scheduleAlarm() [Java]
         ↓
AlarmManager schedules TWO alarms:
  1. Light alarm: 6:40 AM (20 min before)
  2. Sound alarm: 7:00 AM (exact time)
         ↓
--- WAIT FOR ALARM TIME ---
         ↓
AlarmReceiver.onReceive() triggered
         ↓
For LIGHT alarm:
  → Start SunriseService (foreground service)
  → SunriseService launches MainActivity fullscreen
  → MainActivity dispatches "showWakeScreen" to JavaScript
  → React shows wake overlay with colored animation
         ↓
For SOUND alarm:
  → AlarmService plays sound (gradual volume 50%→100%)
  → MainActivity shows wake overlay
  → User swipes right to dismiss
```

### Current Problem Point

**Save Alarm Button Click:**
```javascript
onClick={async () => {
  // Check permissions FIRST (like Gentle Wakeup)
  const permCheck = await checkAllPermissions();
  
  if (!permCheck.allGranted) {
    setMissingPermissions(permCheck.missing);
    setShowPermissionModal(true);  // ← THIS NEVER HAPPENS
    return; 
  }
  
  // ... rest of alarm scheduling code
```

**Permission Check Function:**
```javascript
const checkAllPermissions = async () => {
  if (!window.Capacitor || !window.Capacitor.Plugins.AlarmService) {
    return { allGranted: true, missing: [] }; // ← WEB FALLBACK (might be hiding the issue)
  }
  
  try {
    const result = await window.Capacitor.Plugins.AlarmService.checkPermissions();
    return result;
  } catch (error) {
    console.error('Permission check failed:', error);
    return { allGranted: false, missing: ['unknown'] };
  }
};
```

---

## Key Code Files

### 1. AlarmServicePlugin.java (Capacitor Bridge)

**Location:** `android/app/src/main/java/com/lightalarm/app/AlarmServicePlugin.java`

**New Methods Added Today:**
```java
@PluginMethod
public void checkPermissions(PluginCall call) {
    JSObject result = new JSObject();
    JSArray missing = new JSArray();
    
    // Check POST_NOTIFICATIONS (Android 13+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (getContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) 
            != PackageManager.PERMISSION_GRANTED) {
            missing.put("Notifications");
        }
    }
    
    // Check USE_FULL_SCREEN_INTENT (Android 13+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        NotificationManager nm = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (!nm.canUseFullScreenIntent()) {
            missing.put("Full Screen Alarms");
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
    Intent intent = new Intent(getContext(), PermissionSetupActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    getContext().startActivity(intent);
    
    JSObject result = new JSObject();
    result.put("success", true);
    call.resolve(result);
}
```

### 2. PermissionSetupActivity.java (New File)

**Location:** `android/app/src/main/java/com/lightalarm/app/PermissionSetupActivity.java`

**Purpose:** Guide user through each permission screen sequentially

**Key Methods:**
- `requestNotificationPermission()`
- `requestFullScreenPermission()`
- `requestOverlayPermission()`
- `requestBatteryOptimization()`

**Registered in AndroidManifest.xml:**
```xml
<activity
    android:name=".PermissionSetupActivity"
    android:label="Setup Permissions"
    android:theme="@style/Theme.AppCompat.Light"
    android:exported="true" />
```

### 3. Permission Modal UI (React)

**Location:** `src/App.js` (lines 5749-5781)

```javascript
{showPermissionModal && (
  <div className="fixed inset-0 z-[9999] flex items-center justify-center bg-black/80 backdrop-blur-sm">
    <div className="bg-white rounded-3xl p-8 max-w-md mx-4 shadow-2xl">
      <h2 className="text-2xl font-bold text-gray-800 mb-4">Permissions Required</h2>
      <p className="text-gray-600 mb-6">
        To ensure your alarm works reliably, please grant these permissions:
      </p>
      <ul className="space-y-3 mb-8">
        {missingPermissions.map((perm, idx) => (
          <li key={idx} className="flex items-center gap-3 text-gray-700">
            <span className="text-xl">⚠️</span>
            <span className="font-medium">{perm}</span>
          </li>
        ))}
      </ul>
      <button
        onClick={async () => {
          await window.Capacitor.Plugins.AlarmService.requestAllPermissions();
          setShowPermissionModal(false);
        }}
        className="w-full bg-gradient-to-r from-blue-500 to-blue-600 text-white py-4 rounded-xl font-semibold shadow-lg"
      >
        Grant Permissions
      </button>
    </div>
  </div>
)}
```

---

## Required Permissions

**Declared in AndroidManifest.xml:**
- `WAKE_LOCK` - Keep screen on
- `RECEIVE_BOOT_COMPLETED` - Restart alarms after reboot
- `VIBRATE` - Vibration on alarm
- `USE_EXACT_ALARM` - Precise alarm timing
- `SCHEDULE_EXACT_ALARM` - Schedule exact alarms
- `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` - Prevent system from killing alarm
- `DISABLE_KEYGUARD` - Show over lock screen
- `TURN_SCREEN_ON` - Turn screen on when locked
- `SYSTEM_ALERT_WINDOW` - Draw over other apps
- `USE_FULL_SCREEN_INTENT` - Fullscreen notification (Android 13+)
- `POST_NOTIFICATIONS` - Show notifications (Android 13+)
- `FOREGROUND_SERVICE` - Run foreground services
- `FOREGROUND_SERVICE_MEDIA_PLAYBACK` - Media playback service
- `WRITE_SETTINGS` - Control system brightness

---

## Previous Issues We've Fixed

### 1. JavaScript Timing Conflicts (FIXED)
- **Problem:** JavaScript `setInterval` conflicting with native alarms
- **Solution:** Removed ALL JavaScript timing code, 100% native Android for alarm scheduling

### 2. White Screen on Launch (FIXED)
- **Problem:** AnimatedBackground not rendering colors
- **Solution:** Added `baseColorLayer` to all animation types

### 3. Wake Overlay Not Appearing (PARTIALLY FIXED)
- **Problem:** Event dispatch timing issues between native and WebView
- **Solution:** Added delays, backup triggers, and app state checks

### 4. Alarm Triggers When Toggle is OFF (FIXED)
- **Problem:** AlarmManager not properly cancelled
- **Solution:** Explicitly cancel both light and sound PendingIntents

### 5. Grey Screen in Fullscreen Mode (FIXED)
- **Problem:** Wake overlay using stale color state
- **Solution:** Calculate colors independently based on progress

### 6. App Keeps Reopening (FIXED)
- **Problem:** Service START_STICKY restarting automatically
- **Solution:** Changed to START_NOT_STICKY

---

## Current Hypothesis (Why It's Crashing)

### Possible Causes:

**1. Capacitor Plugin Not Properly Registered**
- The new `checkPermissions()` method might not be exposed to JavaScript
- Capacitor might need re-sync or rebuild after adding new methods
- The `@PluginMethod` annotation might not be sufficient

**2. JavaScript Silent Failure**
- `window.Capacitor.Plugins.AlarmService.checkPermissions()` might be undefined
- Error is caught but returns default values (hiding the real issue)
- Web fallback `return { allGranted: true }` prevents modal from showing

**3. Context/Activity Issues**
- `getContext()` in plugin might be null
- `PermissionSetupActivity` launch failing due to context
- Intent flags might be wrong

**4. Android Version Compatibility**
- `NotificationManager.canUseFullScreenIntent()` only exists on API 34+
- Code might crash on Android 13 (API 33)

**5. Missing Imports/Dependencies**
- JSArray might not be imported correctly
- Theme.AppCompat.Light might not be available

---

## Debug Information Needed

### What Would Help Most:

1. **Logcat Output** when app crashes
   - Command: `adb logcat *:E` (errors only)
   - Look for: Stack trace, "AlarmServicePlugin", "PermissionSetupActivity"

2. **Browser Console Log** (Chrome DevTools)
   - Connect phone via USB
   - Chrome → `chrome://inspect`
   - Look for JavaScript errors

3. **Verify Capacitor Plugin Methods**
   - In browser console, check: `window.Capacitor.Plugins.AlarmService`
   - Does `checkPermissions` appear in the method list?

4. **Test Permission Check Manually**
   ```javascript
   // In browser console
   window.Capacitor.Plugins.AlarmService.checkPermissions()
     .then(result => console.log('Success:', result))
     .catch(err => console.error('Error:', err));
   ```

---

## Questions for AI Assistant

### Primary Question:
**"Why would my Capacitor plugin method crash when called from JavaScript, and how do I fix it?"**

### Specific Questions:

1. **Plugin Registration:**
   - Is `@PluginMethod` annotation sufficient for Capacitor to expose methods?
   - Do I need to rebuild/re-sync after adding new methods?
   - Is there a plugin registration step I'm missing?

2. **JavaScript Integration:**
   - Should `window.Capacitor.Plugins.AlarmService.checkPermissions()` work automatically?
   - How do I debug if the method is actually callable from JS?
   - Is my error handling hiding the real issue?

3. **Permission Approach:**
   - Is creating a custom `PermissionSetupActivity` the right approach?
   - Should I use Capacitor's built-in permission APIs instead?
   - How does "Gentle Wakeup" handle this differently?

4. **Android Specifics:**
   - Is my `checkPermissions()` implementation Android-version-safe?
   - Will `NotificationManager.canUseFullScreenIntent()` crash on API 33?
   - Are there compatibility issues I'm missing?

5. **Alternative Approaches:**
   - Should I request permissions one-by-one instead of a flow?
   - Is there a simpler way to check all permissions at once?
   - Should I abandon the modal and use native Android permission dialogs?

---

## What I've Tried (But Failed)

1. ✅ Added `@PluginMethod` annotations → Still crashes
2. ✅ Created `PermissionSetupActivity` → Never launches
3. ✅ Added try-catch in JavaScript → Silently fails
4. ✅ Checked if Capacitor exists → Returns web fallback
5. ✅ Registered activity in AndroidManifest → Still doesn't work
6. ✅ Added all required imports → No change
7. ✅ Built APK multiple times → Same crash

---

## Files to Review

**Key Files (in order of importance):**
1. `android/app/src/main/java/com/lightalarm/app/AlarmServicePlugin.java` - Capacitor bridge
2. `src/App.js` (lines 254-266, 5509-5517, 5749-5781) - Permission check and modal
3. `android/app/src/main/java/com/lightalarm/app/PermissionSetupActivity.java` - Permission flow
4. `android/app/src/main/AndroidManifest.xml` - Permissions and activity registration

**Supporting Files:**
- `android/app/src/main/java/com/lightalarm/app/AlarmService.java` - Alarm sound with gradual volume
- `android/app/src/main/java/com/lightalarm/app/SunriseService.java` - Light simulation service
- `android/app/src/main/java/com/lightalarm/app/AlarmReceiver.java` - Alarm broadcast receiver
- `android/app/src/main/java/com/lightalarm/app/MainActivity.java` - Main activity with wake screen dispatch

---

## Success Criteria

**What "working" looks like:**
1. User clicks "Save Alarm" → Permission modal appears (if permissions missing)
2. User clicks "Grant Permissions" → Flows through each permission screen
3. All permissions granted → Alarm saves successfully
4. 20 minutes before alarm → Screen turns on, shows colored light animation (even when locked)
5. At alarm time → Sound plays (starting at 50%, gradually to 100%)
6. User swipes right → Alarm dismisses, app goes to home page
7. User taps → Alarm snoozes for 9 minutes

**Current reality:**
- None of the above happens
- App crashes on step 1
- No permissions requested
- Alarm doesn't work

---

## Additional Context

### Why This Matters
- User wants a reliable alarm app (like Gentle Wakeup)
- Must work when phone is locked/screen off
- Gradual light + sound for natural waking
- We've been stuck in a fix-break-fix cycle for days

### Development Constraints
- Windows environment (PowerShell, not bash)
- Building APK via `gradlew.bat`
- Testing on physical Android device
- No access to Android Studio IDE (command-line only)

### What We Need
- **Root cause** of the crash
- **Step-by-step fix** that actually works
- **Validation method** to test before building APK
- **Alternative approach** if current method is fundamentally flawed

---

## Please Help With

1. **Diagnose:** Why is the app crashing when we call `checkPermissions()`?
2. **Fix:** What code changes are needed to make the permission flow work?
3. **Verify:** How can we test this works before building another APK?
4. **Prevent:** How do we avoid this "add code, crash, debug" cycle in the future?

---

**Thank you for any insights or solutions you can provide! 🙏**

