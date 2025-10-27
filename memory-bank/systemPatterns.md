# System Patterns: Light Alarm App

## Architecture Overview

### Monolithic Component Structure
The app is built as a **single large React component** (`LightAlarm`) in `App.js`. While not ideal for large-scale applications, this pattern works for this project's scope and keeps related functionality together.

```
App.js (LightAlarm Component)
├── State Management (useState hooks)
├── Effect Hooks (useEffect for side effects)
├── Helper Functions
├── Sub-Components (defined inline)
│   ├── AnimatedBackground
│   ├── PermissionBanner
│   ├── Alarm Controls
│   ├── Sleep Controls
│   └── Settings Panel
└── Main Render (Tab-based UI)
```

## Key Technical Decisions

### 1. State Management: React Hooks (Not Redux)
**Decision:** Use React's built-in `useState` and `useEffect` hooks  
**Rationale:**
- App state is mostly local (alarm settings, UI state)
- No need for global state management complexity
- Simpler to reason about and debug
- Less boilerplate code

**State Categories:**
- **Mode & Theme:** `currentMode`, `currentTheme`, `isRunning`
- **Time Settings:** `alarmTime`, `sleepTimer`, `preAlarmTime`
- **UI State:** `isFullscreen`, `showSettings`, `showAlarmInfo`
- **Brightness:** `brightness`, `maxBrightness`, `comfortLightBrightness`
- **Audio:** `selectedAlarmSound`, `sleepAudioMode`, `currentAudio`
- **Permissions:** `permissionStatus` object

### 2. Animation Engine: Custom CSS + RequestAnimationFrame
**Decision:** Build custom animation system rather than use animation library  
**Rationale:**
- Fine-grained control over color/brightness transitions
- Performance optimization for battery life
- No dependency on heavy animation libraries
- Custom Nendo-inspired patterns require bespoke implementation

**Pattern:**
```javascript
useEffect(() => {
  const animateFrame = () => {
    // Update animation state
    setAnimationTime(prev => prev + 0.016);
    requestAnimationFrame(animateFrame);
  };
  const frameId = requestAnimationFrame(animateFrame);
  return () => cancelAnimationFrame(frameId);
}, [dependencies]);
```

### 3. Audio: Web Audio API + Tone.js
**Decision:** Use Tone.js for audio playback with Web Audio fallback  
**Rationale:**
- Precise timing control for gradual volume ramp
- Better performance than HTML5 Audio for looping
- Cross-platform compatibility
- Tone.js handles audio context creation/resumption

**Pattern:**
```javascript
// Create audio context
const context = new (window.AudioContext || window.webkitAudioContext)();

// Play with gradual volume increase
const audio = new Audio(soundPath);
audio.volume = 0.1;
const interval = setInterval(() => {
  if (audio.volume < 1.0) {
    audio.volume = Math.min(audio.volume + 0.05, 1.0);
  }
}, 500);
```

### 4. Mobile Integration: Capacitor (Not Cordova)
**Decision:** Use Capacitor for native mobile features  
**Rationale:**
- Modern API design
- Better TypeScript support
- Active development and community
- Easier web → native development flow

**Native Features Used:**
- `AlarmService` - **EXCLUSIVE** native alarm scheduling (NEVER use LocalNotifications)
- `Haptics` - Vibration feedback
- `App` - App lifecycle events

### 5. Styling: Tailwind CSS
**Decision:** Use Tailwind utility classes  
**Rationale:**
- Rapid UI development
- Consistent design system
- Small bundle size with tree-shaking
- Good for responsive design

**Pattern:**
```jsx
<div className="bg-gradient-to-br from-blue-50 to-purple-50 min-h-screen">
  <button className="px-6 py-3 bg-blue-500 text-white rounded-lg">
    Start
  </button>
</div>
```

## Design Patterns in Use

### Component Composition Pattern
Sub-components defined as functions within main component:
```javascript
const AnimatedBackground = ({ color, intensity, animationType }) => {
  // Component logic
  return <div>...</div>;
};
```

**Benefits:**
- Access to parent state without prop drilling
- Colocation of related code
- Easier to refactor later if needed

### Interval Management Pattern
Refs used to store interval IDs for cleanup:
```javascript
const intervalRef = useRef(null);

const startAlarm = () => {
  intervalRef.current = setInterval(() => {
    // Update logic
  }, 1000);
};

const stopAlarm = () => {
  if (intervalRef.current) {
    clearInterval(intervalRef.current);
    intervalRef.current = null;
  }
};
```

### Progressive Enhancement Pattern
Capacitor plugins loaded conditionally:
```javascript
// ⚠️ CRITICAL: NEVER use LocalNotifications - use AlarmService plugin exclusively
try {
  if (window.Capacitor) {
    // AlarmService is registered in MainActivity.java
    // LocalNotifications should NEVER be used for alarms
  }
} catch (error) {
  console.log('Capacitor plugins not available in web environment');
}
```

**Benefits:**
- App works in browser for development
- Graceful fallback when native features unavailable
- Easier testing and debugging

### Theme Configuration Pattern
Themes defined as data objects:
```javascript
const themes = {
  sunrise: {
    name: 'Sunrise',
    colors: ['#1a0033', '#4a0e4e', '#ff1493', '#ff69b4', '#ffa500', '#ffeb3b'],
    animation: 'sunrise',
    icon: <Sunrise />
  },
  // ... more themes
};
```

**Benefits:**
- Easy to add new themes
- Consistent structure
- Separates data from logic

### Fullscreen Mode Pattern
```javascript
const toggleFullscreen = () => {
  if (!isFullscreen) {
    document.documentElement.requestFullscreen?.();
  } else {
    document.exitFullscreen?.();
  }
  setIsFullscreen(!isFullscreen);
};
```

## Component Relationships

### Main Component: LightAlarm
- **Owns:** All state and business logic
- **Renders:** Tab-based interface (Wake, Sleep, Settings)
- **Coordinates:** Mode switching, alarm scheduling, audio playback

### Sub-Component: AnimatedBackground
- **Purpose:** Render themed color animations
- **Inputs:** color, intensity, animationType, progress
- **Outputs:** Visual fullscreen display
- **Key Logic:** Color interpolation, animation rendering

### Sub-Component: PermissionBanner
- **Purpose:** Display permission status and request prompts
- **Inputs:** permissionStatus state
- **Outputs:** UI warnings/instructions
- **Trigger:** Permission checks on mount and settings changes

## Data Flow

```
User Input → State Update → Effect Hook → Side Effect
     ↓            ↓             ↓            ↓
  Button      setState()   useEffect()  Interval/API
                               ↓            ↓
                          Cleanup ← Return Function
```

### Example: Alarm Flow
1. User sets alarm time → `setAlarmTime('07:00')`
2. User taps "Save Alarm" → `saveAlarm()` called
3. Effect hook detects alarm state change
4. **Dual alarms scheduled** → `AlarmService.scheduleAlarm()` (20min light + exact sound)
5. At 20min before: AlarmReceiver → AlarmService → light sequence begins
6. At exact time: AlarmReceiver → AlarmService → sound + vibration begins
7. User stops alarm → interval cleared → state reset

## Performance Considerations

### Animation Optimization
- Use `requestAnimationFrame` instead of `setInterval` for smooth 60fps
- Throttle state updates to prevent excessive re-renders
- Use CSS transforms (GPU-accelerated) over position changes

### Battery Optimization
- Warn users when high-intensity animations enabled
- Offer low-power animation modes
- Properly clean up intervals and event listeners
- Respect Android battery optimization settings

### Memory Management
- Always clear intervals/timeouts in cleanup functions
- Release audio contexts when not in use
- Limit number of simultaneous animations
- Use refs for values that don't need to trigger re-renders

## Error Handling Strategy

### Graceful Degradation
- App works without Capacitor plugins (web mode)
- Fallback UI when permissions denied
- Default sounds if custom audio fails to load

### User Feedback
- Permission status banners
- Error messages in UI (not just console)
- Visual indicators for audio/notification issues

### Defensive Programming
- Optional chaining for Capacitor APIs (`?.`)
- Try-catch blocks around native code
- Type checking before operations
- Null checks before accessing nested properties


