# Project Brief: Light Alarm App

## Core Identity
A mobile alarm application that uses **gradual light and color transitions** to wake users naturally, simulating sunrise/sunset patterns. The app replaces traditional jarring alarm sounds with a visual, calming wake-up experience backed by customizable audio options.

## Primary Goal
Create a gentle, natural wake-up and sleep experience through:
- **Gradual light transitions** (mimicking sunrise for wake-up, sunset for sleep)
- **Multiple themed color progressions** (Sunrise, Ocean, Aurora, Lavender, etc.)
- **Nendo-inspired animations** (optional visual effects that respond to brightness)
- **Custom alarm sounds** with gradual volume increase
- **Comfort light mode** for all-night ambient lighting
- **Sleep timer** with audio for falling asleep

## Target Platform
- **PRIMARY:** Native Android mobile app (React + Capacitor)
- **Web browser:** DEVELOPMENT ONLY - for rapid iteration and testing
- **Important:** This is NOT a web app. All features must work natively on Android with phone locked/screen off

## Core Requirements

### Must Have
1. **Wake Mode (Alarm):**
   - Set alarm time with repeat days
   - Pre-alarm light ramp (configurable duration: 5-30 min)
   - **Full-screen light display** with themed color progressions
   - **CRITICAL:** App must open fullscreen when alarm triggers (even when phone locked/off)
   - Wake locks to keep screen on during alarm
   - Final alarm sound with stop/snooze options
   - Separate controls for stopping light vs. sound

2. **Sleep Mode:**
   - Set sleep duration (15-120 minutes)
   - Reverse color progression (bright → dark)
   - Optional sleep audio (ambience or nature sounds)
   - Gradual brightness dimming in final 2 minutes
   - Tap-to-exit functionality

3. **Comfort Light:**
   - All-night ambient lighting
   - Extremely dim brightness levels
   - Deep red/amber colors for sleep-friendly illumination
   - Optional dynamic brightness variation
   - Configurable duration

4. **Themes:**
   - Multiple pre-defined themes: Sunrise, Green Grass, Blue Sea, Sephora Blue, Aurora, Pink Ocean, Lavender, All Night
   - Each theme has 5-6 color stops for smooth progression
   - Themed animations (waves, aurora, dappled, crystalline, etc.)

5. **Settings:**
   - Alarm sound selection (classic alarms, ambient, natural, custom music)
   - Animation intensity (off, subtle, medium, dynamic)
   - Maximum brightness level (50%, 70%, 100%)
   - Snooze duration
   - Pre-alarm duration

6. **Permissions:**
   - Local notifications for alarms
   - Battery optimization awareness
   - Exact alarm scheduling (Android 12+)

### Nice to Have
- Wake-on-shake detection
- Weather integration
- Smart alarm (detect sleep cycles)
- Multiple alarms
- Historical data/insights

## Success Criteria
- Users wake up feeling more refreshed than with traditional alarms
- Light transitions are smooth and natural-looking
- App works reliably even when phone is locked
- Battery drain is acceptable for all-night usage
- UI is intuitive and beautiful

## Constraints
- Must work on Android 12+
- Must respect device battery optimization
- File size should remain reasonable (<50MB APK)
- Should work offline (no internet required for core functionality)

## Out of Scope
- iOS version (current phase)
- Cloud sync
- Social features
- Sleep tracking hardware integration

