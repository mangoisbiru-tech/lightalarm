# 🌅 Light Alarm App

**Wake up gently with animated sunrise themes** - A hybrid React + Capacitor + Android Native alarm app with beautiful animated backgrounds and gradual light transitions.

[![CI](https://github.com/mangoisbiru-tech/lightalarm/actions/workflows/ci.yml/badge.svg)](https://github.com/mangoisbiru-tech/lightalarm/actions/workflows/ci.yml)
[![Native Android CI](https://github.com/mangoisbiru-tech/lightalarm/actions/workflows/native-android.yml/badge.svg)](https://github.com/mangoisbiru-tech/lightalarm/actions/workflows/native-android.yml)
[![Security](https://github.com/mangoisbiru-tech/lightalarm/actions/workflows/security.yml/badge.svg)](https://github.com/mangoisbiru-tech/lightalarm/actions/workflows/security.yml)

## ✨ Features

- **🌅 Animated Sunrise Themes** - 8 beautiful light themes with smooth animations
- **⏰ Reliable Native Android Alarms** - Uses Android's AlarmManager for bulletproof scheduling
- **🔄 Sleep/Wake Mode Toggle** - Switch between alarm and sleep tracking modes
- **🎨 Customizable Animations** - Multiple animation types (aurora, waves, gradient, etc.)
- **📱 Hybrid Architecture** - React frontend + Capacitor + Native Android backend
- **🔒 Lock Screen Support** - Alarms work even when phone is locked/off
- **🎵 Audio Integration** - Custom alarm sounds and ambient audio

## 🏗️ Architecture

This project uses a **hybrid architecture**:

### Main App (Hybrid)
- **Frontend:** React 19 + TailwindCSS + Framer Motion
- **Mobile Bridge:** Capacitor 7.4.3
- **Backend:** Native Android (Java/Kotlin) services

### Pure Native Android (Alternative)
- **Pure Android Native** app using Jetpack Compose
- **Location:** `light-alarm-native/` directory
- **Purpose:** Testing and development of native Android features

## 🚀 Quick Start

### Prerequisites
- **Node.js** 18+ ([download](https://nodejs.org/))
- **Java JDK** 17+ ([download](https://adoptium.net/))
- **Android Studio** (optional, for IDE support)

### Development Setup

```bash
# Clone the repository
git clone https://github.com/mangoisbiru-tech/lightalarm.git
cd lightalarm

# Install dependencies
npm install

# Start development server
npm start
```

Open [http://localhost:3000](http://localhost:3000) to view in browser.

### Mobile Development

```bash
# Build for production
npm run build

# Sync with Capacitor
npx cap sync android

# Build Android APK
cd android
./gradlew assembleDebug

# Install on device
# APK location: android/app/build/outputs/apk/debug/app-debug.apk
```

## 📁 Project Structure

```
lightalarm/
├── src/                    # React frontend
│   ├── App.js             # Main application (59K+ tokens!)
│   └── components/        # React components
├── android/               # Capacitor Android project
│   ├── app/src/main/     # Android native code
│   └── capacitor.config.ts
├── light-alarm-native/    # Pure Android native app
│   └── app/src/main/     # Kotlin + Jetpack Compose
├── memory-bank/           # Complete project documentation
│   ├── projectbrief.md   # What & why
│   ├── productContext.md # User experience blueprint
│   ├── activeContext.md  # Current work status
│   ├── systemPatterns.md # Architecture guide
│   ├── techContext.md    # Technology reference
│   └── progress.md       # Status tracking
├── .github/workflows/     # Automated workflows
│   ├── ci.yml           # Continuous integration
│   ├── release.yml      # Release automation
│   ├── security.yml     # Security scanning
│   └── backup.yml       # Automated backups
└── public/               # Static assets
```

## 🤖 Background Agents & Automation

This project uses multiple automated systems:

### Git Hooks (Pre-commit)
- **ESLint** - Code quality checks
- **Prettier** - Code formatting
- **Lint-staged** - Run checks only on staged files

### GitHub Actions Workflows
1. **CI Pipeline** - Tests, linting, building for both hybrid and native apps
2. **Release Pipeline** - Automated APK generation and GitHub releases
3. **Security Scanning** - Weekly vulnerability checks and dependency updates
4. **Documentation Validation** - Daily checks for stale documentation
5. **Automated Backups** - Daily project snapshots with 90-day retention

### Memory Bank System
- **Auto-updating documentation** that tracks project state
- **Session-based context** that resets between development sessions
- **Complete knowledge base** for project understanding

## 🎨 Available Themes

1. **Sunrise** 🌅 - Classic yellow → gold → orange → pink → purple → black
2. **Green Grass** 🌱 - Natural green gradients
3. **Blue Sea** 🌊 - Ocean blue → navy transitions
4. **Sephora Blue** 💠 - Cyan → teal effects
5. **Aurora** ✨ - Multi-color aurora dance (Nendo-inspired)
6. **Pink Ocean** 🌸 - Pink gradient themes
7. **Lavender** 💜 - Purple/lavender gradients
8. **All Night** 🌙 - Yellow → dark yellow comfort light

## 🔧 Available Scripts

```bash
npm start          # Start development server
npm run build      # Build for production
npm test           # Run tests
npm run lint       # Run ESLint
npm run lint:fix   # Fix ESLint issues
npm run format     # Format code with Prettier
```

## 📱 Mobile Features

- **Native Android Integration** - Direct access to Android AlarmManager
- **Fullscreen Wake-up** - Displays over lock screen
- **Battery Optimization** - Respects Android battery management
- **Haptic Feedback** - Vibration support
- **Audio Playback** - Custom alarm sounds
- **Screen Brightness Control** - Gradual brightness increase

## 📚 Documentation

Complete project knowledge is maintained in the **Memory Bank**:

- **[Project Brief](memory-bank/projectbrief.md)** - Foundation and goals
- **[Product Context](memory-bank/productContext.md)** - User experience blueprint
- **[Active Context](memory-bank/activeContext.md)** - Current work status
- **[System Patterns](memory-bank/systemPatterns.md)** - Architecture guide
- **[Tech Context](memory-bank/techContext.md)** - Technology reference
- **[Progress Tracking](memory-bank/progress.md)** - Status and milestones

## 🔄 Development Workflow

1. **Web Development** - React in browser (localhost:3000)
2. **Testing** - Browser console + React DevTools
3. **Native Testing** - Build APK and test on device
4. **Documentation** - Update memory bank after changes
5. **Git Hooks** - Automatic code quality checks on commit

## 🛠️ Technologies

- **Frontend:** React 19.1.1, TailwindCSS 4.1.13, Framer Motion
- **Mobile:** Capacitor 7.4.3, Android Native (Java/Kotlin)
- **Audio:** Tone.js 15.1.22
- **Icons:** Lucide React 0.544.0
- **Build:** Create React App, Gradle, GitHub Actions

## 📄 License

This project is private and proprietary.

---

**Built with ❤️ for gentle wake-ups**

---

## 🤝 Contributing

1. **Fork** the repository
2. **Create** a feature branch (`git checkout -b feature/amazing-feature`)
3. **Commit** your changes (`git commit -m 'Add amazing feature'`)
4. **Push** to the branch (`git push origin feature/amazing-feature`)
5. **Open** a Pull Request

### Code Quality
- All commits are automatically checked with ESLint and Prettier
- CI pipeline runs tests and builds on every push
- Documentation must be updated in the memory bank

## 📊 Project Status

- **Development:** Active
- **Version:** 0.1.0
- **Platform:** Android (primary), Web (development)
- **Build Status:** ✅ Functional APKs generated
- **Test Coverage:** 📈 Improving

## 🔗 Links

- **Repository:** https://github.com/mangoisbiru-tech/lightalarm
- **Issues:** https://github.com/mangoisbiru-tech/lightalarm/issues
- **Documentation:** See `memory-bank/` folder
- **APK Builds:** Available in GitHub Actions artifacts
