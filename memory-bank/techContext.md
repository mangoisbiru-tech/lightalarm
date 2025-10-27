# Tech Context: Light Alarm App

## Technology Stack

### Frontend Framework
- **React 19.1.1** - UI library
  - Hooks-based functional components
  - No class components
  - No external state management (Redux, MobX)

### Mobile Framework
- **Capacitor 7.4.3** - Native mobile bridge
  - Web-to-native API wrapper
  - Better than Cordova for modern development
  - Plugins used:
    - `@capacitor/android` - Android platform
    - `@capacitor/app` - App lifecycle
    - `@capacitor/core` - Core APIs
    - `@capacitor/haptics` - Vibration/haptics
    - **Custom AlarmService Plugin** - **EXCLUSIVE** alarm scheduling (NEVER use @capacitor/local-notifications)
  - **CRITICAL:** @capacitor/local-notifications has proven unreliable for alarm functionality
  - **Reason:** Incorrect cancellation behavior, timing issues, and conflicts with native Android alarm system
  - **Solution:** Use custom AlarmService plugin which integrates directly with Android AlarmManager

### Build Tools
- **Create React App 5.0.1** - Build system
  - Webpack bundler (under the hood)
  - Babel transpiler
  - Jest test runner
  - Not ejected (using standard CRA setup)

### Styling
- **TailwindCSS 4.1.13** - Utility-first CSS
  - PostCSS 8.5.6 - CSS processing
  - Autoprefixer 10.4.21 - Vendor prefixes
  - Custom gradient and animation classes

### Audio
- **Tone.js 15.1.22** - Audio synthesis and playback
  - Used for precise timing
  - Gradual volume control
  - Audio context management

### Icons
- **Lucide React 0.544.0** - Icon library
  - Tree-shakeable SVG icons
  - Lightweight alternative to FontAwesome
  - Used throughout UI

### Testing (Available but not actively used)
- **@testing-library/react 16.3.0**
- **@testing-library/jest-dom 6.8.0**
- **@testing-library/user-event 13.5.0**

## Development Environment

### Prerequisites
- **Node.js** (version not specified, likely 16+)
- **npm** - Package manager
- **Java JDK 17+** - Android builds
- **Android Studio** - Android SDK (optional for IDE)
- **Gradle** - Android build system (included)

### Development Setup

#### 1. Clone/Open Project
```bash
cd C:\Users\lau_w\Desktop\light-alarm-app
```

#### 2. Install Dependencies
```bash
npm install
```

#### 3. Run Development Server
```bash
npm start
```
- Opens at http://localhost:3000
- Hot reload enabled
- Browser-based development (Capacitor plugins won't work)

#### 4. Build for Production
```bash
npm run build
```
- Output: `build/` directory
- Minified, optimized bundle
- Ready for Capacitor sync

#### 5. Sync with Capacitor
```bash
npx cap sync android
```
- Copies `build/` to `android/app/src/main/assets/`
- Updates native project configuration

#### 6. Build Android APK
```bash
cd android
powershell.exe -Command ".\gradlew.bat assembleDebug"
```
- Output: `android/app/build/outputs/apk/debug/app-debug.apk`
- Debug build (not signed for Play Store)

### Project Structure
```
light-alarm-app/
├── android/               # Capacitor Android project
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── assets/   # Web app files (synced from build/)
│   │   │   ├── java/     # Native Java code
│   │   │   └── res/      # Android resources
│   │   └── build.gradle
│   └── gradle/           # Gradle wrapper
├── build/                # Production build output
├── node_modules/         # npm dependencies
├── public/               # Static assets
│   ├── index.html
│   ├── favicon.ico
│   ├── manifest.json    # PWA manifest
│   ├── sw.js            # Service worker
│   └── CUSTOM-MUSIC-INSTRUCTIONS.txt
├── src/                  # React source code
│   ├── App.js           # Main application component (59K+ tokens!)
│   ├── App.css          # Component styles
│   ├── index.js         # Entry point
│   ├── index.css        # Global styles
│   └── *.backup*.js     # Backup versions
├── capacitor.config.ts  # Capacitor configuration
├── package.json         # npm dependencies
└── README.md
```

## Technical Constraints

### Platform Limitations

#### Android
- **Minimum SDK:** Likely API 22+ (Android 5.1+)
- **Battery Optimization:** Must request exemption for reliable alarms
- **Exact Alarms:** Android 12+ requires special permission
- **Background Execution:** Limited by Doze mode
- **Fullscreen:** Must request permission on newer Android versions

#### Web Browser (Development)
- No native notifications
- No haptics
- Limited audio autoplay (user interaction required)
- No wake locks
- Different permission model

### Performance Constraints
- **Animations:** Must balance visual appeal with battery life
- **Audio:** Large files increase APK size
- **Fullscreen:** Constant screen-on drains battery
- **Memory:** Animation state must be efficiently managed

### Storage Constraints
- All assets bundled in APK
- No cloud storage (offline-first)
- LocalStorage for settings persistence
- Keep APK size reasonable (<50MB target)

## Dependencies

### Production Dependencies
```json
{
  "@capacitor/android": "^7.4.3",
  "@capacitor/app": "^7.1.0",
  "@capacitor/cli": "^7.4.3",
  "@capacitor/core": "^7.4.3",
  "@capacitor/haptics": "^7.0.2",
  "@capacitor/local-notifications": "^7.0.3",
  "lucide-react": "^0.544.0",
  "react": "^19.1.1",
  "react-dom": "^19.1.1",
  "react-scripts": "5.0.1",
  "tone": "^15.1.22",
  "web-vitals": "^2.1.4"
}
```

### Dev Dependencies
```json
{
  "autoprefixer": "^10.4.21",
  "postcss": "^8.5.6",
  "tailwindcss": "^4.1.13"
}
```

## Configuration Files

### capacitor.config.ts
```typescript
{
  appId: 'com.lightalarm.app',
  appName: 'LightAlarm',
  webDir: 'build',
  android: {
    allowMixedContent: true,
    captureInput: true,
    webContentsDebuggingEnabled: true
  }
}
```

### package.json Scripts
- `npm start` - Development server
- `npm run build` - Production build
- `npm test` - Run tests
- `npm run eject` - Eject from CRA (not recommended)

### Android Gradle
- **Build Tool:** Gradle 8.x
- **Target SDK:** Likely 34+ (Android 14)
- **Build Variants:** Debug, Release

## Browser Compatibility

### Supported
- Chrome 90+ (primary development browser)
- Safari 14+ (iOS WebView in Capacitor)
- Firefox 88+
- Edge 90+

### Required Web APIs
- Web Audio API
- Fullscreen API
- LocalStorage
- RequestAnimationFrame
- CSS Gradients
- CSS Transforms

## Known Technical Issues

### Current
1. **Large App.js File:** 59K+ tokens makes editing slow
2. **No Code Splitting:** Single bundle (could optimize)
3. **Limited Error Boundaries:** Could improve error handling

### Resolved
- ✅ AnimatedBackground white screen (base color layer added)
- ✅ Sleep mode color display (partial - aurora/gradient working)

## Automation & Background Agents

### Git Hooks (Pre-commit Automation)
- **Husky** - Git hooks management
- **Lint-staged** - Run linters only on staged files
- **Pre-commit Hook** - Automatic ESLint + Prettier on commits
- **Configuration:** `.eslintrc.js`, `.prettierrc`, `lint-staged` in package.json

### GitHub Actions Workflows
1. **CI Pipeline** (`ci.yml`)
   - Multi-Node testing (18.x, 20.x)
   - ESLint validation
   - Unit tests with coverage
   - Web build verification
   - Android APK generation
   - Artifact upload

2. **Release Pipeline** (`release.yml`)
   - Triggered by version tags (v*)
   - Production builds
   - APK signing (with secrets)
   - GitHub releases with assets
   - Automated deployment artifacts

3. **Security Pipeline** (`security.yml`)
   - Weekly vulnerability scans
   - Dependency audit
   - Automated security updates
   - npm audit integration

4. **Documentation Pipeline** (`docs.yml`)
   - Daily memory bank validation
   - Stale documentation detection
   - README validation
   - Package.json structure checks

5. **Backup Pipeline** (`backup.yml`)
   - Daily project snapshots
   - 90-day artifact retention
   - Size monitoring
   - Complete project state preservation

### Code Quality Tools
- **ESLint** - JavaScript/TypeScript linting
- **Prettier** - Code formatting
- **Husky** - Git hook management
- **Lint-staged** - Efficient pre-commit checks

### Memory Bank System Integration
- **Automated Status Updates** - Workflows can update progress.md
- **Documentation Validation** - Ensures memory bank completeness
- **Backup Integration** - Memory bank included in daily snapshots
- **Context Preservation** - Complete project knowledge maintained

## Development Workflow Enhancement

### New Automated Workflow
1. **Code Changes** → Auto-format (Prettier) + lint (ESLint)
2. **Git Commit** → Pre-commit hooks validate code quality
3. **Push to GitHub** → CI pipeline tests and builds
4. **Documentation Updates** → Automated validation
5. **Release Tags** → Automated APK generation and release

### Benefits
- **Quality Assurance** - Every commit is validated
- **Consistency** - Automatic formatting and linting
- **Documentation** - Fresh memory bank and README
- **Backup** - Daily project snapshots
- **Security** - Weekly vulnerability monitoring

## Future Technical Considerations

### Potential Improvements
- **Code Splitting:** Break App.js into smaller components
- **TypeScript:** Add type safety (currently using .ts for config only)
- **Testing:** Add test coverage
- **PWA Features:** Better offline support, install prompt
- **Performance:** Lazy load animations, optimize bundle size

### Potential Libraries to Add
- **date-fns** or **dayjs** - Better time manipulation
- **react-spring** - Advanced animations
- **zustand** - Lightweight state management (if needed)
- **react-hook-form** - Form management (if adding more settings)

## Development Workflow

### Typical Development Cycle
1. Make changes to `src/App.js`
2. Auto-reload in browser at localhost:3000
3. Test functionality in browser
4. Build: `npm run build`
5. Sync: `npx cap sync android`
6. Build APK: `cd android && powershell.exe -Command ".\gradlew.bat assembleDebug"`
7. Install APK on device via USB or file transfer
8. Test native features (notifications, haptics, etc.)

### Testing Strategy
- **Browser:** Quick iteration on UI and logic
- **Android Emulator:** Test native features
- **Physical Device:** Final validation, real-world performance

### Debugging
- **Browser DevTools:** Primary debugging environment
- **Chrome Remote Debugging:** For Android WebView
- **Capacitor Logs:** Console output from mobile
- **Android Logcat:** Native Android logs

## Environment Variables
None currently used (could add for API keys if needed)

## Build Artifacts
- `build/` - Web build (gitignored)
- `android/app/build/` - Android build outputs (gitignored)
- `android/app/src/main/assets/` - Synced web files (gitignored)
- `node_modules/` - Dependencies (gitignored)


