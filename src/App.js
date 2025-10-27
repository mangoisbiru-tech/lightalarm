import React, { useState, useEffect, useRef } from 'react';
import PropTypes from 'prop-types';
import { Sun, Moon, Play, Pause, RotateCcw, Clock, ChevronLeft, ChevronRight, Settings, Volume2, Lightbulb, Zap, Eye, EyeOff, Cloud, CloudSun, Sunrise, Trash2 } from 'lucide-react';
import { Capacitor } from '@capacitor/core';
import { LocalNotifications } from '@capacitor/local-notifications';
import { NativeAudio } from '@capacitor-community/native-audio';
import { KeepAwake } from '@capacitor-community/keep-awake';
// Custom AlarmService plugin - imported dynamically
const { AlarmService } = Capacitor.Plugins;

const LightAlarm = () => {
  // ========== SOUND DATA STRUCTURE ==========
  const ALARM_SOUNDS = [
    // Ambience Sounds
    { assetId: 'ambiencesound_airport_luggage_wheels_on_bricks', displayName: 'Airport Luggage', category: 'Ambience' },
    { assetId: 'ambiencesound_cafe', displayName: 'Cafe', category: 'Ambience' },
    { assetId: 'ambiencesound_school', displayName: 'School', category: 'Ambience' },
    { assetId: 'ambiencesound_street_basketball', displayName: 'Street Basketball', category: 'Ambience' },
    // Classic Alarms
    { assetId: 'classicalarm_alarm2', displayName: 'Alarm 2', category: 'Classic Alarm' },
    { assetId: 'classicalarm_alarm3', displayName: 'Alarm 3', category: 'Classic Alarm' },
    { assetId: 'classicalarm_digital_alarm', displayName: 'Digital Alarm', category: 'Classic Alarm' },
    { assetId: 'classicalarm_polite_warning', displayName: 'Polite Warning', category: 'Classic Alarm' },
    { assetId: 'classicalarm_ringtone', displayName: 'Ringtone', category: 'Classic Alarm' },
    // Natural Sounds
    { assetId: 'naturalsound_forest_sounds', displayName: 'Forest Sounds', category: 'Natural Sound' },
    { assetId: 'naturalsound_relaxing_sea', displayName: 'Relaxing Sea', category: 'Natural Sound' },
    { assetId: 'naturalsound_underwater', displayName: 'Underwater', category: 'Natural Sound' }
  ];

  // ========== CSS ANIMATIONS ==========
  useEffect(() => {
    // Add custom CSS animations for floating clouds and twinkling stars
    const style = document.createElement('style');
    style.textContent = `
      @keyframes floatLeftToRight {
        0% { transform: translateX(-100%); }
        100% { transform: translateX(100%); }
      }
      
      @keyframes floatRightToLeft {
        0% { transform: translateX(100%); }
        100% { transform: translateX(-100%); }
      }
      
      @keyframes twinkle-1 {
        0%, 100% { opacity: 0.3; transform: scale(1); }
        50% { opacity: 1; transform: scale(1.2); }
      }
      
      @keyframes twinkle-2 {
        0%, 100% { opacity: 0.4; transform: scale(1); }
        25% { opacity: 0.8; transform: scale(1.1); }
        75% { opacity: 0.6; transform: scale(0.9); }
      }
      
      @keyframes twinkle-3 {
        0%, 100% { opacity: 0.5; transform: scale(1); }
        33% { opacity: 1; transform: scale(1.3); }
        66% { opacity: 0.7; transform: scale(0.8); }
      }
      
      @keyframes twinkle-4 {
        0%, 100% { opacity: 0.2; transform: scale(1); }
        40% { opacity: 0.9; transform: scale(1.1); }
        80% { opacity: 0.4; transform: scale(0.9); }
      }
      
      @keyframes twinkle-5 {
        0%, 100% { opacity: 0.6; transform: scale(1); }
        20% { opacity: 1; transform: scale(1.4); }
        60% { opacity: 0.3; transform: scale(0.7); }
      }
      
      .animate-float-left-to-right {
        animation: floatLeftToRight 25s linear infinite;
        animation-delay: 0s;
      }
      
      .animate-float-right-to-left {
        animation: floatRightToLeft 30s linear infinite;
        animation-delay: 5s;
      }
      
      .animate-twinkle-1 {
        animation: twinkle-1 2s ease-in-out infinite;
      }
      
      .animate-twinkle-2 {
        animation: twinkle-2 2.5s ease-in-out infinite;
      }
      
      .animate-twinkle-3 {
        animation: twinkle-3 3s ease-in-out infinite;
      }
      
      .animate-twinkle-4 {
        animation: twinkle-4 2.2s ease-in-out infinite;
      }
      
      .animate-twinkle-5 {
        animation: twinkle-5 2.8s ease-in-out infinite;
      }
    `;
    document.head.appendChild(style);
    
    return () => {
      document.head.removeChild(style);
    };
  }, []);

  // ========== STATE MANAGEMENT ==========
  const [isRunning, setIsRunning] = useState(false);
  const [currentMode, setCurrentMode] = useState('wake');
  const [currentTheme, setCurrentTheme] = useState('sunrise');
  const [progress, setProgress] = useState(0);
  const [brightness, setBrightness] = useState(0.01);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [alarmTime, setAlarmTime] = useState('07:00');
  const [alarmAmPm, setAlarmAmPm] = useState('AM');
  const [sleepTime, setSleepTime] = useState('10:00');
  const [sleepAmPm, setSleepAmPm] = useState('PM');
  const [snoozeMinutes, setSnoozeMinutes] = useState(3);
  const [isSnoozeEnabled, setIsSnoozeEnabled] = useState(true);
  const [repeatDays, setRepeatDays] = useState([]);
  const [isAlarmEnabled, setIsAlarmEnabled] = useState(false);
  const [currentTime, setCurrentTime] = useState(new Date());
  const [sleepTimer, setSleepTimer] = useState(30);
  const [isCustomSleepTimer, setIsCustomSleepTimer] = useState(false);
  const [customSleepTimerValue, setCustomSleepTimerValue] = useState(90);
  const [showSettings, setShowSettings] = useState(false);
  const [preAlarmTime, setPreAlarmTime] = useState(10);


  // ========== COMFORT LIGHT STATE ==========
  const [isComfortLightEnabled, setIsComfortLightEnabled] = useState(false);
  const [showFullscreenUI, setShowFullscreenUI] = useState(true);
  const [showComfortLightSettings, setShowComfortLightSettings] = useState(false);
  const [showSleepTimerPicker, setShowSleepTimerPicker] = useState(false);
  const [showAlarmTimePicker, setShowAlarmTimePicker] = useState(false);
  const [showHourPicker, setShowHourPicker] = useState(false);
  const [showMinutePicker, setShowMinutePicker] = useState(false);
  const [comfortLightBrightness, setComfortLightBrightness] = useState(0.05); // Extremely dim by default
  const [comfortLightColor, setComfortLightColor] = useState('deep-red');
  const [comfortLightDuration, setComfortLightDuration] = useState('all-night');
  const [isComfortLightRunning, setIsComfortLightRunning] = useState(false);
  const comfortLightIntervalRef = useRef(null);
  const [isDynamicBrightness, setIsDynamicBrightness] = useState(false);
  const [brightnessVariation, setBrightnessVariation] = useState(0);
  const dynamicBrightnessRef = useRef(null);

  // ========== NENDO ANIMATION STATE ==========
  const [showBatteryWarning, setShowBatteryWarning] = useState(false);
  const [animationTime, setAnimationTime] = useState(0);
  const animationRef = useRef(null);

  // ========== SLEEP MODE TAP HINT STATE ==========
  const [showSleepTapHint, setShowSleepTapHint] = useState(false);

  // ========== SCREEN SAVER STATE ==========
  const [isScreenSaver, setIsScreenSaver] = useState(false);

  
  // ========== NATIVE SOUND SELECTION STATE ==========
  const [selectedSoundResourceId, setSelectedSoundResourceId] = useState(null);
  const [availableNativeSounds, setAvailableNativeSounds] = useState([]);
  const [previewingSound, setPreviewingSound] = useState(null);
  const previewTimeoutRef = useRef(null);

  // ========== NATIVE SOUND SELECTION FUNCTIONS ==========
  // Load available native sounds on component mount
  useEffect(() => {
  }, []);

  const openNativeSoundSelection = () => {
    // Native sound selection now handled by Capacitor plugins
        setShowSoundSelection(true);
  };

  const handlePlayPreview = async (assetId) => {
    if (!Capacitor.isNativePlatform()) {
      console.log('Sound preview only available on native platform');
      return;
    }

    // Stop any currently playing preview
    if (previewingSound) {
      try {
        await NativeAudio.stop({ assetId: previewingSound });
      } catch (error) {
        console.error('Error stopping previous preview:', error);
      }
      if (previewTimeoutRef.current) {
        clearTimeout(previewTimeoutRef.current);
      }
    }

    // Start new preview
    try {
      await NativeAudio.play({ assetId });
      setPreviewingSound(assetId);
      
      // Stop after 10 seconds
      previewTimeoutRef.current = setTimeout(async () => {
        try {
          await NativeAudio.stop({ assetId });
    } catch (error) {
          console.error('Error stopping preview:', error);
        }
        setPreviewingSound(null);
      }, 10000);
    } catch (error) {
      console.error('Error playing preview:', error);
    }
  };


  // ========== SAVE CONFIRMATION STATE ==========
  const [showSaveConfirm, setShowSaveConfirm] = useState(false);
  const [alarmTimeUntil, setAlarmTimeUntil] = useState({ hours: 0, minutes: 0 });
  const [savedThemeName, setSavedThemeName] = useState('');

  // ========== ALARM LIST STATE ==========
  const [alarms, setAlarms] = useState([
    {
      id: 1,
      time: '07:00',
      amPm: 'AM',
      repeat: 'Daily',
      theme: 'Sunrise',
      sound: { displayName: 'Digital Alarm', resourceName: 'classicalarm_digital_alarm' },
      isEnabled: false
    }
  ]);
  const [showAddAlarm, setShowAddAlarm] = useState(false);
  const [showSoundSelection, setShowSoundSelection] = useState(false);
  const [showThemeSelection, setShowThemeSelection] = useState(false);
  const [editingAlarm, setEditingAlarm] = useState(null);
  
  // New alarm form state
  const [newAlarmHour, setNewAlarmHour] = useState(8);
  const [newAlarmMinute, setNewAlarmMinute] = useState(0);
  const [newAlarmAmPm, setNewAlarmAmPm] = useState('AM');
  const [newAlarmRepeat, setNewAlarmRepeat] = useState('Daily');
  const [newAlarmSelectedDays, setNewAlarmSelectedDays] = useState([]);
  const [newAlarmSound, setNewAlarmSound] = useState('Classic Alarm');
  const [newAlarmTheme, setNewAlarmTheme] = useState('Sunrise');

  // ========== FUNCTION DECLARATIONS (moved up to avoid hoisting issues) ==========

  // ========== TIME CALCULATION HELPER ==========
  function calculateTimeUntil(alarmTime, alarmAmPm) {
      const [alarmHour, alarmMinute] = alarmTime.split(':').map(Number);
      let targetHour = alarmHour;
    if (alarmAmPm === 'PM' && alarmHour !== 12) targetHour += 12;
    else if (alarmAmPm === 'AM' && alarmHour === 12) targetHour = 0;

      const now = new Date();
      const alarmDate = new Date();
      alarmDate.setHours(targetHour, alarmMinute, 0, 0);
    if (alarmDate <= now) alarmDate.setDate(alarmDate.getDate() + 1);
    
    const diffMs = alarmDate.getTime() - now.getTime();
    const hours = Math.floor(diffMs / (1000 * 60 * 60));
    // Fix: Use Math.ceil for minutes to show "1 min" for any duration > 0 seconds
    const minutes = Math.ceil((diffMs % (1000 * 60 * 60)) / (1000 * 60));
    
    return { hours, minutes };
  }

  // ========== CRITICAL ANDROID PERMISSIONS SYSTEM ==========
  async function requestCriticalPermissions() {
    if (!Capacitor.isNativePlatform()) {
      console.log('Skipping native permissions - not on native platform');
      return true; // Return true for web to avoid blocking
    }
    
    console.log('🔒 Requesting critical alarm permissions...');

    try {
      if (Capacitor.isNativePlatform()) {
      const result = await LocalNotifications.requestPermissions();
      if (result.display === 'granted') {
        console.log('✅ Notification permissions granted');
        setPermissionStatus(prev => ({ ...prev, notifications: 'granted' }));
        return true;
      } else {
        console.log('⚠️ Notification permissions denied');
        setPermissionStatus(prev => ({ ...prev, notifications: 'denied' }));
        return false;
        }
      } else {
        console.log('Skipping permission request - not on native platform');
        return true;
      }
    } catch (error) {
      console.error('Error requesting permissions:', error);
      return false;
    }
  }


  // ========== LIVE CLOCK ==========
  useEffect(() => {
    const timer = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  // ========== PRELOAD NATIVE SOUNDS ==========
  useEffect(() => {
    preloadNativeSounds();
  }, []);

  // ========== LOAD SAVED ALARMS ON STARTUP ==========
  useEffect(() => {
    const loadAlarms = async () => {
      try {
        // CRITICAL: Check if AlarmService plugin is available
        console.log('🔍 AlarmService available:', !!Capacitor.Plugins.AlarmService);
        console.log('🔍 Capacitor.Plugins:', Object.keys(Capacitor.Plugins));
        
        if (!Capacitor.Plugins.AlarmService) {
          console.error('❌ AlarmService plugin not available - native bridge broken');
          return;
        }
        
        console.log('📱 Loading saved alarms from native SharedPreferences...');
        const result = await AlarmService.getAllAlarms();
        if (result.alarms && result.alarms.length > 0) {
          console.log('✅ Loaded', result.alarms.length, 'saved alarms from native storage');
          
          // Convert native alarm format to React state format
          const reactAlarms = result.alarms.map(alarm => ({
            id: parseInt(alarm.id),
            time: `${String(alarm.hour).padStart(2, '0')}:${String(alarm.minute).padStart(2, '0')}`,
            amPm: alarm.amPm,
            sound: { 
              displayName: 'Custom Sound', 
              resourceName: alarm.soundResourceName 
            },
            theme: alarm.theme,
            enabled: alarm.enabled
          }));
          
          setAlarms(reactAlarms);
          console.log('🔄 React state updated with native alarms:', reactAlarms);
        } else {
          console.log('📭 No saved alarms found in native storage');
        }
      } catch (error) {
        console.log('⚠️ No saved alarms or error loading:', error);
        // This is normal on first app launch
      }
    };
    
    loadAlarms();
  }, []);

  // ========== ALARM CHECKING SYSTEM ==========
  // Alarm timing now handled 100% by native Android AlarmManager

  // ========== NATIVE ALARM EVENT LISTENER ==========
  // REMOVED: JavaScript-based alarm execution logic
  // Alarms now handled 100% by native Android services for reliability when phone is locked



  // ========== PERMISSION REQUEST ON ALARM ENABLE ==========
  useEffect(() => {
    if (!isAlarmEnabled) return;
    // Only schedule once per enable toggle in Web Sandbox
    try {
      requestCriticalPermissions?.();
    } catch (e) {
      // ignore in web sandbox
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [isAlarmEnabled]);

  // ========== DYNAMIC BRIGHTNESS ANIMATION ==========
  useEffect(() => {
    if (isDynamicBrightness && isRunning && currentMode === 'sleep') {
      let startTime = Date.now();

      dynamicBrightnessRef.current = setInterval(() => {
        const elapsed = Date.now() - startTime;
        const config = getCurrentThemeConfig();
        const animationType = config?.animation || 'gradient';
        const variation = getDynamicBrightnessVariation(animationType, elapsed);
        setBrightnessVariation(variation);
      }, 100); // Update every 100ms for smooth animation

      return () => {
        if (dynamicBrightnessRef.current) {
          clearInterval(dynamicBrightnessRef.current);
          dynamicBrightnessRef.current = null;
        }
        setBrightnessVariation(0);
      }
    } else {
      // Clean up if disabled
      if (dynamicBrightnessRef.current) {
        clearInterval(dynamicBrightnessRef.current);
        dynamicBrightnessRef.current = null;
      }
      setBrightnessVariation(0);
    }
  }, [isDynamicBrightness, isRunning, currentMode]);

  // ========== UPDATE TIME UNTIL WHEN ALARMS CHANGE ==========
  useEffect(() => {
    console.log("Running latest App.js version - Filter Check");
    
    if (alarms.length === 0) {
      setAlarmTimeUntil({ hours: 0, minutes: 0 });
      return;
    }

    // CRITICAL FIX: Only consider ENABLED alarms for time calculation
    console.log('📅 All alarms before filter:', alarms.map(a => ({ id: a.id, enabled: a.enabled, time: a.time })));
    const enabledAlarms = alarms.filter(alarm => alarm.enabled === true);
    console.log('📅 Enabled alarms after filter:', enabledAlarms.map(a => ({ id: a.id, enabled: a.enabled, time: a.time })));
    
    if (enabledAlarms.length === 0) {
      setAlarmTimeUntil({ hours: 0, minutes: 0 });
      console.log('📅 No enabled alarms - hiding time until display');
      return;
    }

    // Find the next enabled alarm (earliest time)
    const now = new Date();
    let nextAlarm = null;
    let earliestTime = Infinity;

    for (const alarm of enabledAlarms) {
      let targetHour = parseInt(alarm.time.split(':')[0]);
      if (alarm.amPm === 'PM' && targetHour !== 12) {
        targetHour += 12;
      } else if (alarm.amPm === 'AM' && targetHour === 12) {
        targetHour = 0;
      }

      const alarmDate = new Date();
      alarmDate.setHours(targetHour, parseInt(alarm.time.split(':')[1]), 0, 0);

      // If alarm time has passed today, set for tomorrow
      if (alarmDate <= now) {
        alarmDate.setDate(alarmDate.getDate() + 1);
      }

      if (alarmDate.getTime() < earliestTime) {
        earliestTime = alarmDate.getTime();
        nextAlarm = alarm;
      }
    }

    if (nextAlarm) {
      const diffMs = earliestTime - now.getTime();
      const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
      // Fix: Use Math.ceil for minutes to show "1 min" for any duration > 0 seconds
      const diffMinutes = Math.ceil((diffMs % (1000 * 60 * 60)) / (1000 * 60));

      setAlarmTimeUntil({ hours: diffHours, minutes: diffMinutes });
      console.log('📅 Updated alarmTimeUntil for next ENABLED alarm:', nextAlarm.time, nextAlarm.amPm, { hours: diffHours, minutes: diffMinutes });
    }
  }, [alarms]);

  // ========== FULLSCREEN SYSTEM ==========
  const enterFullscreen = async () => {
    try {
      if (document.documentElement.requestFullscreen) {
        await document.documentElement.requestFullscreen();
      } else if (document.documentElement.webkitRequestFullscreen) {
        await document.documentElement.webkitRequestFullscreen();
      } else if (document.documentElement.msRequestFullscreen) {
        await document.documentElement.msRequestFullscreen();
      }
      console.log('✅ Entered fullscreen mode');
    } catch (err) {
      console.error('Failed to enter fullscreen:', err);
    }
  }

  const exitFullscreen = async () => {
    try {
      // Only exit if we're actually in browser fullscreen mode
      const isInFullscreen = document.fullscreenElement ||
                            document.webkitFullscreenElement ||
                            document.msFullscreenElement;

      if (!isInFullscreen) {
        console.log('Not in browser fullscreen mode, skipping exitFullscreen');
        return;
      }

      if (document.exitFullscreen) {
        await document.exitFullscreen();
      } else if (document.webkitExitFullscreen) {
        await document.webkitExitFullscreen();
      } else if (document.msExitFullscreen) {
        await document.msExitFullscreen();
      }
      console.log('Exited fullscreen mode');
    } catch (err) {
      console.error('Failed to exit fullscreen:', err);
    }
  }


  // ========== ALARM TRIGGER FUNCTION ==========
  // Functions moved up to avoid hoisting issues

  // ========== NENDO ANIMATION SYSTEM ==========
  useEffect(() => {
    const animate = () => {
      setAnimationTime(prev => prev + 16); // 60fps timing
      animationRef.current = requestAnimationFrame(animate);
    }

    if (isRunning) {
      animationRef.current = requestAnimationFrame(animate);
    }

    return () => {
      if (animationRef.current) {
        cancelAnimationFrame(animationRef.current);
      }
    }
  }, [isRunning]);


  // Sleep mode tap hint - show after 2s, hide after 7s total
  useEffect(() => {
    if (isFullscreen && currentMode === 'sleep') {
      setShowSleepTapHint(false);
      const showTimer = setTimeout(() => setShowSleepTapHint(true), 2000);
      const hideTimer = setTimeout(() => setShowSleepTapHint(false), 7000);
      return () => {
        clearTimeout(showTimer);
        clearTimeout(hideTimer);
      }
    } else {
      setShowSleepTapHint(false);
    }
  }, [isFullscreen, currentMode]);

  // ========== AUDIO SYSTEM ==========
  // Note: AudioContext is created on first user interaction (click test button)
  // This avoids browser autoplay restrictions

  // ========== COMFORT LIGHT CONFIGURATIONS ==========
  const comfortLightColors = {
    'deep-red': {
      name: 'Deep Red',
      description: 'Safest for natural sleep cycles',
      scientific: '630-660nm • Minimal melatonin disruption',
      color: '#990000',
      gradient: 'linear-gradient(135deg, #660000 0%, #990000 50%, #660000 100%)',
      defaultBrightness: 0.05, // Extremely dim by default
      recommended: true
    },
    'amber': {
      name: 'Amber',
      description: 'Mimics natural firelight',
      scientific: '580-595nm • Candlelight spectrum',
      color: '#cc6600',
      gradient: 'linear-gradient(135deg, #994400 0%, #cc6600 50%, #994400 100%)',
      defaultBrightness: 0.08,
      recommended: false
    },
    'warm-orange': {
      name: 'Warm Orange',
      description: 'Gentle evening transition',
      scientific: '585-620nm • Sunset warmth',
      color: '#cc4400',
      gradient: 'linear-gradient(135deg, #993300 0%, #cc4400 50%, #993300 100%)',
      defaultBrightness: 0.1,
      recommended: false
    }
  }

  const comfortLightDurations = {
    '30min': {
      name: '30 minutes',
      description: 'Quick fall-asleep assistance',
      duration: 30 * 60 * 1000,
      icon: '😴'
    },
    '1hr': {
      name: '1 hour',
      description: 'Standard sleep timer',
      duration: 60 * 60 * 1000,
      icon: '🕐'
    },
    '2hr': {
      name: '2 hours',
      description: 'Extended comfort period',
      duration: 2 * 60 * 60 * 1000,
      icon: '🕑'
    },
    'all-night': {
      name: 'All night (until sunrise)',
      description: 'Seamless transition to sunrise alarm',
      duration: -1,
      icon: '🌙',
      recommended: true
    }
  }


  // ========== THEME CONFIGURATIONS ==========
  const wakeThemesPage1 = {
    sunrise: {
      name: 'Sunrise',
      colors: ['#000000', '#1a0a2e', '#4a1a6a', '#ff0080', '#ff6b35', '#ffa500', '#ffd700', '#fffacd'],
      duration: 20,
      animation: 'sunrise'
    },
    greengrass: {
      name: 'Green Grass',
      colors: ['#000000', '#0d1b0d', '#1a3a1a', '#228b22', '#32cd32', '#7fff00', '#adff2f', '#f0fff0'],
      duration: 20,
      animation: 'dappled'
    },
    bluesea: {
      name: 'Blue Sea',
      colors: ['#000000', '#000033', '#003366', '#0066cc', '#1e90ff', '#00bfff', '#87ceeb', '#e0f7ff'],
      duration: 20,
      animation: 'waves'
    },
    sephorablue: {
      name: 'Sephora Blue',
      colors: ['#000000', '#001a1a', '#003333', '#008080', '#00ced1', '#40e0d0', '#7fffd4', '#e0ffff'],
      duration: 20,
      animation: 'crystalline'
    },
    pastelgreen: {
      name: 'Pastel Green',
      colors: ['#000000', '#1a331a', '#336633', '#589058', '#85cc85', '#a3f5a3', '#c1ffc1', '#f0fff0'],
      duration: 20,
      animation: 'gradient'
    }
  }

  const wakeThemesPage2 = {
    aurora: {
      name: 'Aurora',
      colors: ['#000000', '#4a1a6a', '#00ff66', '#ff69b4', '#0099ff', '#e0ffff'],
      duration: 20,
      animation: 'aurora'
    },
    pinkoceane: {
      name: 'Pink Ocean',
      colors: ['#000000', '#1a0a1a', '#4a1a6a', '#c71585', '#ff1493', '#ff69b4', '#ffb6c1', '#fff0f5'],
      duration: 20,
      animation: 'waves'
    },
    lavender: {
      name: 'Lavender',
      colors: ['#000000', '#1a1a2e', '#2e2e5a', '#483d8b', '#6a5acd', '#7b68ee', '#9370db', '#f8f8ff'],
      duration: 20,
      animation: 'spiral'
    }
  }

  const sleepThemesPage1 = {
    sunrise: {
      name: 'Sunrise',
      colors: ['#ffff88', '#ffd700', '#ff8c42', '#ff69b4', '#4a1a6a', '#000000'],
      duration: sleepTimer,
      animation: 'sunrise'
    },
    greengrass: {
      name: 'Green Grass',
      colors: ['#7fff00', '#32cd32', '#00ff00', '#00cc00', '#228b22', '#1a3a1a', '#000000'],
      duration: sleepTimer,
      animation: 'dappled'
    },
    bluesea: {
      name: 'Blue Sea',
      colors: ['#00d4ff', '#00bfff', '#1e90ff', '#0080ff', '#0066cc', '#003366', '#000000'],
      duration: sleepTimer,
      animation: 'waves'
    },
    sephorablue: {
      name: 'Deep Space',
      colors: ['#4d5eff', '#3d4eff', '#2d3eff', '#1d2eff', '#1a1eff', '#1614e6', '#130ecc', '#0F0AB3', '#0F02C4', '#0C02B0', '#09029C', '#060288'],
      duration: sleepTimer,
      animation: 'twinkle'
    }
  }

  const sleepThemesPage2 = {
    aurora: {
      name: 'Aurora',
      colors: ['#ffffff', '#e0ffff', '#ffb6ff', '#ff69b4', '#00ffff', '#b19cd9', '#ff80ff', '#80ffff', '#ffc0ff', '#c0ffff', '#ff99ff', '#99ffff', '#e0b0ff', '#b0e0ff'],
      duration: sleepTimer,
      animation: 'aurora'
    },
    pinkoceane: {
      name: 'Pink Ocean',
      colors: ['#ffcccb', '#ffb6c1', '#ff69b4', '#ff1493', '#4a1a6a', '#000000'],
      duration: sleepTimer,
      animation: 'waves'
    },
    lavenderfields: {
      name: 'Lavender Fields',
      colors: ['#d966ff', '#cc5cff', '#bf52ff', '#b348ff', '#a63eff', '#9934ff', '#8c2aff', '#8020ff', '#7316ff', '#660cff', '#5c05e6', '#5200cc'],
      duration: sleepTimer,
      animation: 'spiral'
    },
    neonsnake: {
      name: 'Neon Snake',
      colors: ['#000000'],
      duration: sleepTimer,
      animation: 'snake'
    },
    neonbg: {
      name: 'Neon BG',
      colors: ['#000000'],
      duration: sleepTimer,
      animation: 'neonbg'
    },
    pastelgreen: {
      name: 'Pastel Green',
      colors: ['#c1ffc1', '#b2f9b2', '#a3f5a3', '#94e094', '#85cc85', '#76b876', '#67a467', '#589058'],
      duration: sleepTimer,
      animation: 'gradient'
    }
  }

  // ========== THEME LOGIC ==========
  const getCurrentThemes = () => {
    if (currentMode === 'wake') {
      return {...wakeThemesPage1, ...wakeThemesPage2};
    } else {
      return {...sleepThemesPage1, ...sleepThemesPage2};
    }
  }

  const themes = getCurrentThemes();

  const getCurrentThemeConfig = () => {
    if (!themes || Object.keys(themes).length === 0) {
      console.warn('⚠️ No themes available, using default');
      return {
        name: 'Default',
        colors: ['#000000', '#ffffff'],
        duration: 20,
        animation: 'gradient'
      }
    }

    const theme = themes[currentTheme];
    console.log(`🎨 Getting theme config - currentTheme: "${currentTheme}", mode: ${currentMode}, theme exists:`, !!theme);

    if (!theme || !theme.name) {
      console.warn(`⚠️ Theme "${currentTheme}" not found, using first available theme`);
      const firstKey = Object.keys(themes)[0];
      const firstTheme = themes[firstKey];
      console.log(`🎨 First theme: "${firstKey}"`, firstTheme);
      if (!firstTheme || !firstTheme.name) {
        return {
          name: 'Default',
          colors: ['#000000', '#ffffff'],
          duration: 20,
          animation: 'gradient'
        }
      }
      return firstTheme;
    }
    console.log(`🎨 Using theme "${currentTheme}":`, theme);
    return theme;
  }

  // ========== COLOR ALGORITHMS ==========
  const interpolateColor = (color1, color2, factor) => {
    if (!color1 || !color2) return '#000000';

    const hex1 = color1.replace('#', '');
    const hex2 = color2.replace('#', '');

    const r1 = parseInt(hex1.substr(0, 2), 16) || 0;
    const g1 = parseInt(hex1.substr(2, 2), 16) || 0;
    const b1 = parseInt(hex1.substr(4, 2), 16) || 0;

    const r2 = parseInt(hex2.substr(0, 2), 16) || 0;
    const g2 = parseInt(hex2.substr(2, 2), 16) || 0;
    const b2 = parseInt(hex2.substr(4, 2), 16) || 0;

    const r = Math.round(r1 + (r2 - r1) * factor);
    const g = Math.round(g1 + (g2 - g1) * factor);
    const b = Math.round(b1 + (b2 - b1) * factor);

    return `rgb(${r}, ${g}, ${b})`;
  }

  const getCurrentColor = () => {
    const config = getCurrentThemeConfig();
    if (!config || !config.colors || !Array.isArray(config.colors) || config.colors.length === 0) {
      return '#000000';
    }

    const { colors } = config;

    // SLEEP MODE: Start bright, end dark
    if (currentMode === 'sleep') {
      if (progress === 0) return colors[0]; // Start with first color (bright)
      if (progress >= 100) return colors[colors.length - 1]; // End with last color (dark)

      const segmentSize = 100 / (colors.length - 1);
      const segmentIndex = Math.floor(progress / segmentSize);
      const segmentProgress = (progress % segmentSize) / segmentSize;

      const startColor = colors[Math.min(segmentIndex, colors.length - 2)];
      const endColor = colors[Math.min(segmentIndex + 1, colors.length - 1)];

      return interpolateColor(startColor, endColor, segmentProgress);
    }

    // WAKE MODE: Normal color progression (start dark, end bright)
    // Show end color in preview when not running
    if (progress === 0 && !isRunning) return colors[colors.length - 1];
    if (progress === 0) return colors[0];
    if (progress >= 100) return colors[colors.length - 1];

    const segmentSize = 100 / (colors.length - 1);
    const segmentIndex = Math.floor(progress / segmentSize);
    const segmentProgress = (progress % segmentSize) / segmentSize;

    const startColor = colors[Math.min(segmentIndex, colors.length - 2)];
    const endColor = colors[Math.min(segmentIndex + 1, colors.length - 1)];

    return interpolateColor(startColor, endColor, segmentProgress);
  }

  // ========== NENDO ANIMATION HELPERS ==========
  const getAnimationMultiplier = () => {
    return 0.6; // Hardcoded to 'medium' level
  }

  const getBreathingPattern = (timeMs) => {
    const frequency = 0.05; // 0.05Hz = gentle breathing
    const baseIntensity = Math.sin(timeMs * frequency * Math.PI * 2) * 0.5 + 0.5;
    return 0.8 + (baseIntensity * 0.2 * getAnimationMultiplier()); // 0.8-1.0 range
  }

  const getWavePattern = (timeMs, xPos = 0, yPos = 0) => {
    const waveSpeed = 0.002;
    const waveLength = 200;
    return Math.sin((timeMs * waveSpeed) + (xPos / waveLength)) * getAnimationMultiplier();
  }

  const getRipplePattern = (timeMs, centerX = 50, centerY = 50, radius = 100) => {
    const rippleSpeed = 0.003;
    const rippleFreq = 0.1;
    return Math.sin(timeMs * rippleSpeed - radius * rippleFreq) * getAnimationMultiplier();
  }

  const getAuroraFlow = (timeMs, verticalPos = 0) => {
    const flowSpeed = 0.001;
    const curtainWave = Math.sin(timeMs * flowSpeed + verticalPos * 0.01);
    return curtainWave * getAnimationMultiplier();
  }

  const getDappledLight = (timeMs, leafPattern = 1) => {
    const swaySpeed = 0.0008;
    const lightShift = Math.sin(timeMs * swaySpeed + leafPattern);
    return lightShift * getAnimationMultiplier() * 0.15; // Subtle dappling
  }

  // ========== AUTOMATIC BRIGHTNESS CONTROL ==========
  const [originalBrightness, setOriginalBrightness] = useState(null);

  const setSystemBrightness = async (level) => {
    try {
      // Method 1: Modern Screen Brightness API (experimental)
      if ('screen' in navigator && 'brightness' in navigator.screen) {
        await navigator.screen.brightness.set(level);
        console.log(`✅ Brightness set to ${Math.round(level * 100)}%`);
        return true;
      }

      // Method 2: PWA Brightness Control
      if ('wakeLock' in navigator && 'brightness' in navigator) {
        await navigator.brightness.set(level);
        console.log(`✅ PWA brightness set to ${Math.round(level * 100)}%`);
        return true;
      }

      // Method 3: Screen Wake Lock with CSS filter override
      if ('wakeLock' in navigator) {
        // Use CSS to simulate brightness by controlling the overlay opacity
        const brightnessLevel = Math.max(0.1, level); // Prevent completely black
        document.documentElement.style.setProperty('--screen-brightness', brightnessLevel);

        // Apply brightness filter to the whole screen
        document.body.style.filter = `brightness(${brightnessLevel})`;
        console.log(`✅ CSS brightness simulation: ${Math.round(level * 100)}%`);
        return true;
      }

      console.log('⚠️ No brightness control method available');
      return false;
    } catch (error) {
      console.log('⚠️ Brightness control error:', error);
      return false;
    }
  }

  const requestBrightnessControl = async () => {
    try {
      // Try to get current brightness first
      if ('screen' in navigator && 'brightness' in navigator.screen) {
        const current = await navigator.screen.brightness.get();
        setOriginalBrightness(current);
        console.log(`📱 Current brightness: ${Math.round(current * 100)}%`);
      }

      // Test if we can actually control brightness
      const canControl = await setSystemBrightness(1.0);

      if (canControl) {
        console.log('🔥 AUTOMATIC BRIGHTNESS CONTROL ACTIVE!');
        return true;
      } else {
        // Fallback: Show manual instructions
        const userConfirm = window.confirm(
          "🔆 ENABLE AUTOMATIC BRIGHTNESS\n\n" +
          "This app needs to control your screen brightness automatically.\n\n" +
          "Please:\n" +
          "1. Allow brightness control when prompted\n" +
          "2. Or manually set brightness to MAX\n" +
          "3. Turn OFF auto-brightness in settings\n\n" +
          "This makes the light therapy actually work!\n\n" +
          "Ready?"
        );
        return userConfirm;
      }
    } catch (error) {
      console.log('⚠️ Brightness setup error:', error);
      return false;
    }
  }

  const restoreBrightness = async () => {
    try {
      if (originalBrightness !== null) {
        await setSystemBrightness(originalBrightness);
        console.log('✅ Brightness restored');
      } else {
        // Reset to normal
        await setSystemBrightness(0.8);
        document.body.style.filter = '';
      }
    } catch (error) {
      console.log('⚠️ Could not restore brightness');
      document.body.style.filter = '';
    }
  }

  // ========== BRIGHTNESS ALGORITHMS ==========
  const calculateBrightness = () => {
    if (currentMode === 'wake') {
      if (progress <= 50) {
        return (progress / 50) * 0.7; // 0% → 70%
      } else if (progress < 100) {
        return 0.7 + ((progress - 50) / 50) * 0.3; // 70% → 100%
      } else {
        return 1.0; // Stay at 100%
      }
    } else {
      if (isComfortLightEnabled) {
        return 1.0; // All-night comfort light stays at 100%
      }

      const totalMinutes = sleepTimer;
      const dimStartPercentage = Math.max(0, ((totalMinutes - 2) / totalMinutes) * 100);

      // Start at 100% brightness
      if (progress < dimStartPercentage) {
        return 1.0; // Stay bright at 100%
      } else {
        const dimProgress = (progress - dimStartPercentage) / (100 - dimStartPercentage);
        return Math.max(0.01, 1.0 - dimProgress); // 100% → 0%
      }
    }
  }

  // ========== DYNAMIC BRIGHTNESS VARIATION ==========
  const getDynamicBrightnessVariation = (animationType, time) => {
    if (!isDynamicBrightness || currentMode !== 'sleep') return 0;

    // Only apply when brightness is above 20% to avoid issues in very dim phases
    const baseBrightness = calculateBrightness();
    if (baseBrightness < 0.2) return 0;

    const t = time / 1000; // Convert to seconds

    switch (animationType) {
      case 'aurora':
      case 'waves':
        // WAVE EFFECT: ±20% variation with slower rhythm
        return Math.sin(t * 0.8) * 0.20;

      case 'twinkle':
      case 'dappled':
        // Twinkling effect: random-like pattern ±15%
        return Math.sin(t * 0.5) * Math.cos(t * 1.2) * 0.15;

      case 'spiral':
      case 'crystalline':
        // Spiral pulse: ±12% variation
        return Math.sin(t * 0.6) * 0.12;

      case 'sunrise':
      case 'sunset':
        // Breathing rhythm: ±10%
        return Math.sin(t * 0.4) * 0.10;

      case 'gradient':
        // Gentle shift: ±8%
        return Math.sin(t * 0.3) * 0.08;

      default:
        // Default pulse: ±10%
        return Math.sin(t * 0.5) * 0.10;
    }
  }

  // ========== REAL-TIME BRIGHTNESS CONTROL ==========
  const updateSystemBrightness = async () => {
    if (!isRunning) return;

    const targetBrightness = calculateBrightness();

    // Apply automatic brightness control to actual phone screen
    await setSystemBrightness(targetBrightness);

    console.log(`🔆 System brightness: ${Math.round(targetBrightness * 100)}% (Mode: ${currentMode}, Progress: ${Math.round(progress)}%)`);
  }

  // ========== AUDIO FUNCTIONS ==========











  // ========== NATIVE AUDIO FUNCTIONS ==========
  
  // Preload native sounds from res/raw directory
  async function preloadNativeSounds() {
    if (!Capacitor.isNativePlatform()) {
      console.log('Skipping native audio preload - not on native platform');
      return;
    }

    const soundFiles = [
      'ambiencesound_airport_luggage_wheels_on_bricks',
      'ambiencesound_cafe',
      'ambiencesound_school',
      'ambiencesound_street_basketball',
      'classicalarm_alarm2',
      'classicalarm_alarm3',
      'classicalarm_digital_alarm',
      'classicalarm_polite_warning',
      'classicalarm_ringtone',
      'naturalsound_forest_sounds',
      'naturalsound_relaxing_sea',
      'naturalsound_underwater'
    ];
    
    for (const sound of soundFiles) {
      try {
        await NativeAudio.preload({ assetId: sound, audioChannelNum: 1 });
        console.log(`✅ Successfully preloaded ${sound}`);
      } catch (error) {
        console.error(`❌ Failed to preload ${sound}:`, error);
      }
    }
  }

  // REMOVED: scheduleNativeAlarm function
  // Alarms now handled 100% by native Android AlarmManager for reliability when phone is locked

  // Play alarm sound using native audio
  async function playAlarmSound(soundResourceId) {
    if (!Capacitor.isNativePlatform()) {
      console.log('Skipping native audio playback - not on native platform');
      return;
    }
    
    try {
      await NativeAudio.play({ assetId: soundResourceId || 'classicalarm_bell_ringing', time: 0 });
      
      // Gradually increase volume over time
      for (let i = 30; i <= 100; i += 5) {
        await new Promise(resolve => setTimeout(resolve, 1000));
        await NativeAudio.setVolume({ 
          assetId: soundResourceId || 'classicalarm_bell_ringing', 
          volume: i / 100 
        });
      }
    } catch (error) {
      console.error('Error playing native audio:', error);
    }
  }

  // Stop alarm sound
  async function stopAlarmSound(soundResourceId) {
    if (!Capacitor.isNativePlatform()) {
      console.log('Skipping native audio stop - not on native platform');
        return;
    }
    
    try {
      await NativeAudio.stop({ assetId: soundResourceId || 'classicalarm_bell_ringing' });
    } catch (error) {
      console.error('Error stopping native audio:', error);
    }
  }



  // ========== COMFORT LIGHT FUNCTIONS ==========
  const startComfortLight = async () => {
    if (isComfortLightRunning) return;

    // Enable automatic brightness control
    await requestBrightnessControl();

    // Set comfort light brightness (dim for sleep)
    await setSystemBrightness(0.15); // Very dim for comfort light

    // Go fullscreen for comfort light
    await enterFullscreen();
    setIsFullscreen(true);

    setIsComfortLightRunning(true);

    const duration = comfortLightDurations[comfortLightDuration].duration;

    if (duration > 0) {
      // Timer mode - fade out after specified duration
      comfortLightIntervalRef.current = setTimeout(() => {
        fadeOutComfortLight();
      }, duration);
    }
    // For all-night mode, it stays on until manually stopped or sunrise alarm starts

    console.log('🕯️ Comfort Light started with automatic brightness control');
  }

  const fadeOutComfortLight = () => {
    let currentBrightness = comfortLightBrightness;
    const fadeInterval = setInterval(() => {
      currentBrightness -= 0.01;
      if (currentBrightness <= 0) {
        clearInterval(fadeInterval);
        stopComfortLight();
      } else {
        setComfortLightBrightness(currentBrightness);
      }
    }, 100);
  }

  const stopComfortLight = async () => {
    setIsComfortLightRunning(false);
    if (comfortLightIntervalRef.current) {
      clearTimeout(comfortLightIntervalRef.current);
    }
    // Reset brightness to original setting
    setComfortLightBrightness(0.1);

    // Restore system brightness and exit fullscreen
    await restoreBrightness();
    await exitFullscreen();
    setIsFullscreen(false);

    console.log('🕯️ Comfort Light stopped, brightness restored');
  }

  const getCurrentComfortLightColor = () => {
    const colorConfig = comfortLightColors[comfortLightColor];
    return {
      backgroundColor: colorConfig.color,
      background: colorConfig.gradient
    }
  }

  // ========== ANIMATED BACKGROUND COMPONENT ==========
  const AnimatedBackground = ({ animation, color, brightness }) => {
    // Ensure progress is valid to prevent NaN errors
    const safeProgress = isNaN(progress) || progress === null || progress === undefined ? 0 : progress;

    // Apply dynamic brightness variation if enabled
    const finalBrightness = brightness + (isDynamicBrightness ? brightnessVariation : 0);

    // For sleep mode at full brightness, use saturate to enhance colors
    const baseStyle = {
      filter: currentMode === 'sleep' && finalBrightness >= 0.9
        ? `brightness(${finalBrightness}) saturate(1.5)`
        : `brightness(${finalBrightness})`,
      transition: 'filter 0.1s ease-out' // Faster transition for dynamic brightness
    }

    // Base color layer - always present
    const baseColorLayer = (
      <div
        className="absolute inset-0"
        style={{
          backgroundColor: color,
          opacity: 1.0
        }}
      />
    );

    if (animation === 'aurora') {
      const progressIntensity = 1 + (safeProgress / 100) * 0.4;
      const auroraFlow1 = getAuroraFlow(animationTime, 0);
      const auroraFlow2 = getAuroraFlow(animationTime + 2000, 50);
      const auroraFlow3 = getAuroraFlow(animationTime + 4000, 100);

      // Color shifting based on time
      const colorShift = (animationTime * 0.0005) % 1;
      const hueShift1 = Math.floor(120 + colorShift * 60); // Green to Cyan range
      const hueShift2 = Math.floor(280 + colorShift * 80); // Purple to Magenta range
      const hueShift3 = Math.floor(200 + colorShift * 40); // Blue range

      return (
        <div className="absolute inset-0 overflow-hidden" style={baseStyle}>
          {baseColorLayer}
          {/* Magnetic Dance Curtains - Nendo Inspired */}
          <div
            className="absolute inset-0"
            style={{
              opacity: 0.7 * progressIntensity,
              background: `
                linear-gradient(${120 + auroraFlow1 * 30}deg, transparent 20%, hsla(${hueShift1}, 100%, 60%, 0.4) 40%, transparent 60%),
                linear-gradient(${60 + auroraFlow2 * 20}deg, transparent 30%, hsla(${hueShift2}, 80%, 70%, 0.3) 50%, transparent 70%),
                linear-gradient(${180 + auroraFlow3 * 15}deg, transparent 10%, hsla(${hueShift3}, 90%, 65%, 0.3) 30%, transparent 50%)
              `,
              animation: 'magneticDance 12s ease-in-out infinite',
            }}
          />

          {/* Vertical Curtains with Unpredictable Movement */}
          <div
            className="absolute inset-0"
            style={{
              opacity: 0.5 * progressIntensity,
              background: `
                linear-gradient(0deg, transparent 0%, hsla(${hueShift2}, 90%, 65%, 0.3) 30%, transparent 70%),
                linear-gradient(0deg, transparent 20%, hsla(${hueShift3}, 80%, 70%, 0.25) 50%, transparent 80%)
              `,
              animation: 'verticalCurtains 8s ease-in-out infinite',
            }}
          />

          {/* Star Sparkles - Random Flickers */}
          {progressIntensity > 1.2 && (
            <>
              <div
                className="absolute w-1 h-1 bg-white/80 rounded-full"
                style={{
                  left: '15%',
                  top: '20%',
                  animation: 'starSparkle 3s ease-in-out infinite',
                  animationDelay: '0s'
                }}
              />
              <div
                className="absolute w-0.5 h-0.5 bg-cyan-200/70 rounded-full"
                style={{
                  left: '70%',
                  top: '35%',
                  animation: 'starSparkle 4s ease-in-out infinite',
                  animationDelay: '1s'
                }}
              />
              <div
                className="absolute w-1.5 h-1.5 bg-purple-200/60 rounded-full"
                style={{
                  left: '45%',
                  top: '65%',
                  animation: 'starSparkle 2.5s ease-in-out infinite',
                  animationDelay: '2s'
                }}
              />
              <div
                className="absolute w-0.5 h-0.5 bg-green-200/80 rounded-full"
                style={{
                  left: '85%',
                  top: '15%',
                  animation: 'starSparkle 3.5s ease-in-out infinite',
                  animationDelay: '0.5s'
                }}
              />
            </>
          )}

          {/* Shifting Color Bands */}
          <div
            className="absolute inset-0"
            style={{
              opacity: 0.5 * progressIntensity,
              background: `
                linear-gradient(${45 + auroraFlow2 * 25}deg, hsla(${hueShift1 + 40}, 100%, 60%, 0.2) 0%, transparent 40%, hsla(${hueShift2 + 20}, 100%, 70%, 0.2) 100%),
                radial-gradient(ellipse at ${30 + auroraFlow1 * 10}% ${70 + auroraFlow2 * 10}%, hsla(${hueShift3}, 80%, 80%, 0.3), transparent 60%)
              `,
              animation: 'auroraFlow2 20s ease-in-out infinite reverse',
              transform: `translateY(${-auroraFlow2 * 30}px)`,
            }}
          />

          {/* Aurora Curtain Waves */}
          <div
            className="absolute inset-0"
            style={{
              opacity: 0.4 * getAnimationMultiplier(),
              background: `
                linear-gradient(${90 + auroraFlow3 * 40}deg, transparent 0%, hsla(${hueShift1 + 60}, 90%, 75%, 0.25) 30%, transparent 60%),
                linear-gradient(${270 + auroraFlow1 * 35}deg, transparent 20%, hsla(${hueShift2 + 40}, 95%, 65%, 0.2) 50%, transparent 80%)
              `,
              transform: `translateY(${auroraFlow3 * 50}px) translateX(${auroraFlow1 * 20}px)`,
              animation: 'auroraCurtain 25s ease-in-out infinite',
            }}
          />

          {/* Gentle Aurora Flickers */}
          <div
            className="absolute inset-0"
            style={{
              opacity: 0.2 + (Math.sin(animationTime * 0.003) * 0.1) * getAnimationMultiplier(),
              background: `
                radial-gradient(ellipse at 20% 30%, hsla(${hueShift1}, 100%, 80%, 0.2), transparent 40%),
                radial-gradient(ellipse at 80% 70%, hsla(${hueShift2}, 100%, 75%, 0.15), transparent 35%),
                radial-gradient(ellipse at 50% 50%, hsla(${hueShift3}, 100%, 85%, 0.1), transparent 30%)
              `,
              animation: 'auroraFlicker 18s ease-in-out infinite',
            }}
          />

          <div className="absolute inset-0 -z-10" style={{ background: color }} />
        </div>
      );
    }

    if (animation === 'waves') {
      const progressIntensity = 1 + (safeProgress / 100) * 0.3;
      const waveOffset1 = getWavePattern(animationTime, 0, 0);
      const waveOffset2 = getWavePattern(animationTime + 1500, 50, 0);
      const ripple1 = getRipplePattern(animationTime, 50, 50, 200);
      const ripple2 = getRipplePattern(animationTime + 3000, 30, 70, 150);

      return (
        <div className="absolute inset-0 overflow-hidden" style={baseStyle}>
          {baseColorLayer}
          {/* Horizontal Wave Movement - Primary */}
          <div
            className="absolute inset-0"
            style={{
              opacity: 0.6 * progressIntensity,
              background: `
                radial-gradient(ellipse 150% 100% at ${50 + waveOffset1 * 20}% 100%, rgba(233, 30, 99, 0.4), transparent 70%),
                radial-gradient(ellipse 120% 80% at ${30 + waveOffset2 * 15}% 90%, rgba(74, 20, 140, 0.3), transparent 60%)
              `,
              animation: 'oceanWave1 12s ease-in-out infinite',
              transform: `translateX(${waveOffset1 * 30}px)`,
            }}
          />

          {/* Horizontal Wave Movement - Secondary */}
          <div
            className="absolute inset-0"
            style={{
              opacity: 0.4 * progressIntensity,
              background: `
                radial-gradient(ellipse 100% 50% at ${70 + waveOffset2 * 10}% 80%, rgba(248, 187, 217, 0.5), transparent 80%),
                radial-gradient(circle at ${40 + waveOffset1 * 12}% 60%, rgba(233, 30, 99, 0.3), transparent 50%)
              `,
              animation: 'oceanWave2 8s ease-in-out infinite reverse',
              transform: `translateX(${-waveOffset2 * 25}px)`,
            }}
          />

          {/* Central Ripple Effects */}
          <div
            className="absolute inset-0"
            style={{
              opacity: 0.3 * getAnimationMultiplier(),
              background: `radial-gradient(circle at 50% 50%, rgba(233, 30, 99, 0.25), transparent 40%)`,
              transform: `scale(${1 + ripple1 * 0.4})`,
              animation: 'centerRipple1 10s ease-in-out infinite',
            }}
          />

          {/* Secondary Ripple Effects */}
          <div
            className="absolute inset-0"
            style={{
              opacity: 0.2 * getAnimationMultiplier(),
              background: `radial-gradient(circle at 30% 70%, rgba(74, 20, 140, 0.2), transparent 35%)`,
              transform: `scale(${1 + ripple2 * 0.3})`,
              animation: 'centerRipple2 14s ease-in-out infinite',
            }}
          />

          {/* Ocean Rhythm Breathing */}
          <div
            className="absolute inset-0"
            style={{
              opacity: 0.15 * getBreathingPattern(animationTime * 0.7), // Slower ocean rhythm
              background: `
                linear-gradient(180deg, transparent 0%, rgba(233, 30, 99, 0.2) 50%, transparent 100%),
                linear-gradient(90deg, transparent 0%, rgba(74, 20, 140, 0.15) 50%, transparent 100%)
              `,
              animation: 'oceanBreath 16s ease-in-out infinite',
            }}
          />

        </div>
      );
    }

    if (animation === 'sunset') {
      const progressIntensity = 1 + (safeProgress / 100) * 0.4;
      const sway1 = getDappledLight(animationTime, 1);
      const sway2 = getDappledLight(animationTime + 1000, 2);
      const sway3 = getDappledLight(animationTime + 2000, 3);
      const leafPattern1 = getDappledLight(animationTime * 1.2, 4);
      const leafPattern2 = getDappledLight(animationTime * 0.8, 5);

      return (
        <div className="absolute inset-0 overflow-hidden" style={baseStyle}>
          {baseColorLayer}
          {/* Main Sunset Glow with Swaying */}
          <div
            className="absolute inset-0"
            style={{
              opacity: 0.6 * progressIntensity,
              background: `radial-gradient(ellipse 80% 40% at ${50 + sway1 * 15}% 30%, rgba(255, 165, 0, 0.4), transparent 80%)`,
              animation: 'sunGlow 25s ease-in-out infinite',
              transform: `translateX(${sway1 * 25}px)`,
            }}
          />

          {/* Dappled Light Effect - Layer 1 */}
          <div
            className="absolute inset-0"
            style={{
              opacity: 0.4 * getAnimationMultiplier(),
              background: `
                radial-gradient(ellipse at ${20 + leafPattern1 * 10}% ${30 + sway1 * 8}%, rgba(255, 204, 2, 0.3), transparent 25%),
                radial-gradient(ellipse at ${60 + leafPattern2 * 12}% ${50 + sway2 * 6}%, rgba(255, 165, 0, 0.25), transparent 20%),
                radial-gradient(ellipse at ${40 + leafPattern1 * 8}% ${70 + sway3 * 10}%, rgba(255, 99, 71, 0.2), transparent 18%),
                radial-gradient(ellipse at ${80 + leafPattern2 * 6}% ${40 + sway1 * 7}%, rgba(255, 204, 2, 0.15), transparent 15%)
              `,
              animation: 'dappledLight1 20s ease-in-out infinite',
              transform: `translateY(${sway2 * 15}px)`,
              filter: `blur(${0.5 + getAnimationMultiplier() * 0.5}px)`,
            }}
          />

          {/* Dappled Light Effect - Layer 2 */}
          <div
            className="absolute inset-0"
            style={{
              opacity: 0.3 * getAnimationMultiplier(),
              background: `
                radial-gradient(ellipse at ${15 + leafPattern2 * 14}% ${60 + sway3 * 9}%, rgba(255, 165, 0, 0.2), transparent 22%),
                radial-gradient(ellipse at ${70 + leafPattern1 * 11}% ${25 + sway1 * 5}%, rgba(255, 204, 2, 0.18), transparent 19%),
                radial-gradient(ellipse at ${45 + leafPattern2 * 7}% ${80 + sway2 * 8}%, rgba(255, 99, 71, 0.15), transparent 16%)
              `,
              animation: 'dappledLight2 24s ease-in-out infinite reverse',
              transform: `translateX(${-sway3 * 12}px) translateY(${sway1 * 8}px)`,
              filter: `blur(${0.8 + getAnimationMultiplier() * 0.3}px)`,
            }}
          />

          {/* Swaying Branch Shadows */}
          <div
            className="absolute inset-0"
            style={{
              opacity: 0.15 * getAnimationMultiplier(),
              background: `
                linear-gradient(${45 + sway1 * 20}deg, rgba(139, 69, 19, 0.1) 0%, transparent 30%),
                linear-gradient(${-30 + sway2 * 15}deg, transparent 40%, rgba(101, 67, 33, 0.08) 70%, transparent 100%),
                linear-gradient(${60 + sway3 * 12}deg, rgba(160, 82, 45, 0.06) 20%, transparent 50%)
              `,
              animation: 'branchShadow 30s ease-in-out infinite',
              transform: `rotate(${sway1 * 3}deg)`,
            }}
          />

          {/* Sunlight Through Leaves */}
          <div
            className="absolute inset-0"
            style={{
              opacity: 0.5 * progressIntensity,
              background: `
                linear-gradient(${45 + sway2 * 10}deg, rgba(255, 165, 0, 0.3) 0%, transparent 40%),
                linear-gradient(${-45 + sway1 * 8}deg, transparent 60%, rgba(255, 99, 71, 0.2) 100%),
                radial-gradient(circle at ${50 + sway3 * 5}% 20%, rgba(255, 204, 2, 0.4), transparent 60%)
              `,
              animation: 'sunRays 18s ease-in-out infinite',
              transform: `translateY(${sway2 * 10}px)`,
            }}
          />
        </div>
      );
    }

    if (animation === 'sunrise') {
      const breathingIntensity = getBreathingPattern(animationTime);
      const progressIntensity = 1 + (safeProgress / 100) * 0.5; // Intensify as alarm progresses

      return (
        <div className="absolute inset-0 overflow-hidden" style={baseStyle}>
          {baseColorLayer}
          {/* Solar Breathing Main Sun - Nendo Inspired */}
          <div
            className="absolute inset-0"
            style={{
              opacity: 0.7 * breathingIntensity * progressIntensity,
              background: `radial-gradient(ellipse 70% 35% at 50% 85%, rgba(255, 204, 2, 0.8), transparent 70%)`,
              animation: 'solarBreathing 6s ease-in-out infinite',
            }}
          />

          {/* Light Rays Fanning Out */}
          <div
            className="absolute inset-0"
            style={{
              opacity: 0.4 * progressIntensity,
              background: `
                linear-gradient(45deg, transparent 40%, rgba(255, 165, 0, 0.3) 50%, transparent 60%),
                linear-gradient(135deg, transparent 30%, rgba(255, 204, 2, 0.2) 40%, transparent 50%),
                linear-gradient(90deg, transparent 35%, rgba(255, 138, 91, 0.25) 45%, transparent 55%)
              `,
              animation: 'sunRays 8s ease-in-out infinite',
              transformOrigin: '50% 85%'
            }}
          />

          {/* Circular Wave Expansion - Wave 1 */}
          <div
            className="absolute inset-0"
            style={{
              opacity: 0.3 * getAnimationMultiplier(),
              background: `radial-gradient(circle at 50% 80%, rgba(255, 138, 91, 0.4), transparent 40%)`,
              transform: `scale(${1 + getRipplePattern(animationTime, 50, 80, 150) * 0.3})`,
              animation: 'sunRipple1 8s ease-in-out infinite',
            }}
          />

          {/* Circular Wave Expansion - Wave 2 */}
          <div
            className="absolute inset-0"
            style={{
              opacity: 0.25 * getAnimationMultiplier(),
              background: `radial-gradient(circle at 50% 80%, rgba(255, 165, 0, 0.3), transparent 55%)`,
              transform: `scale(${1 + getRipplePattern(animationTime + 2000, 50, 80, 200) * 0.4})`,
              animation: 'sunRipple2 12s ease-in-out infinite',
            }}
          />

          {/* Dust Motes Rising - Visible in Golden Phase */}
          {progressIntensity > 1.3 && (
            <>
              <div
                className="absolute w-2 h-2 bg-yellow-200/60 rounded-full"
                style={{
                  left: '20%',
                  animation: 'dustMotes 15s linear infinite',
                  animationDelay: '0s'
                }}
              />
              <div
                className="absolute w-1 h-1 bg-orange-200/40 rounded-full"
                style={{
                  left: '60%',
                  animation: 'dustMotes 18s linear infinite',
                  animationDelay: '3s'
                }}
              />
              <div
                className="absolute w-1.5 h-1.5 bg-yellow-100/50 rounded-full"
                style={{
                  left: '80%',
                  animation: 'dustMotes 12s linear infinite',
                  animationDelay: '6s'
                }}
              />
              <div
                className="absolute w-1 h-1 bg-amber-200/35 rounded-full"
                style={{
                  left: '35%',
                  animation: 'dustMotes 20s linear infinite',
                  animationDelay: '9s'
                }}
              />
            </>
          )}

          {/* Floating Light Particles */}
          <div
            className="absolute inset-0"
            style={{
              opacity: 0.3 * getAnimationMultiplier() * (progress / 100),
              background: `
                radial-gradient(circle at 20% 30%, rgba(255, 204, 2, 0.3), transparent 15%),
                radial-gradient(circle at 80% 40%, rgba(255, 138, 91, 0.25), transparent 12%),
                radial-gradient(circle at 60% 70%, rgba(255, 165, 0, 0.2), transparent 10%)
              `,
              animation: 'floatingParticles 15s ease-in-out infinite',
            }}
          />

          {/* Linear Sunrise Gradient */}
          <div
            className="absolute inset-0 opacity-30"
            style={{
              background: `linear-gradient(0deg, rgba(255, 138, 91, 0.3) 0%, transparent 50%)`,
              animation: 'sunrise 20s ease-in-out infinite',
            }}
          />
        </div>
      );
    }

    if (animation === 'dappled') {
      const progressIntensity = 1 + (safeProgress / 100) * 0.4;
      return (
        <div className="absolute inset-0 overflow-hidden" style={baseStyle}>
          {baseColorLayer}
          {/* Dappled Light Patches - Moving like wind through leaves */}
          <div
            className="absolute inset-0"
            style={{
              opacity: 0.4 * progressIntensity,
              background: `
                radial-gradient(ellipse 100px 60px at 20% 30%, rgba(76, 175, 80, 0.3), transparent 60%),
                radial-gradient(ellipse 80px 120px at 70% 20%, rgba(139, 195, 74, 0.25), transparent 70%),
                radial-gradient(ellipse 120px 80px at 45% 60%, rgba(102, 187, 106, 0.35), transparent 65%),
                radial-gradient(ellipse 90px 90px at 15% 80%, rgba(76, 175, 80, 0.2), transparent 50%)
              `,
              animation: 'dappledLight 8s ease-in-out infinite',
            }}
          />

          {/* Leaf Shadows - Moving Shadows */}
          <div
            className="absolute inset-0"
            style={{
              opacity: 0.3 * progressIntensity,
              background: `
                linear-gradient(45deg, transparent 40%, rgba(27, 94, 32, 0.2) 50%, transparent 60%),
                linear-gradient(135deg, transparent 30%, rgba(46, 125, 50, 0.15) 40%, transparent 50%),
                linear-gradient(90deg, transparent 35%, rgba(27, 94, 32, 0.1) 45%, transparent 55%)
              `,
              animation: 'leafShadows 6s ease-in-out infinite',
            }}
          />

          {/* Pollen Drifting Horizontally */}
          {progressIntensity > 1.2 && (
            <>
              <div
                className="absolute w-1 h-1 bg-yellow-200/50 rounded-full"
                style={{
                  top: '25%',
                  animation: 'pollenDrift 20s linear infinite',
                  animationDelay: '0s'
                }}
              />
              <div
                className="absolute w-1.5 h-1.5 bg-lime-200/40 rounded-full"
                style={{
                  top: '45%',
                  animation: 'pollenDrift 25s linear infinite',
                  animationDelay: '5s'
                }}
              />
              <div
                className="absolute w-0.5 h-0.5 bg-green-200/60 rounded-full"
                style={{
                  top: '65%',
                  animation: 'pollenDrift 18s linear infinite',
                  animationDelay: '10s'
                }}
              />
            </>
          )}

          {/* Gentle Swaying Motion */}
          <div
            className="absolute inset-0"
            style={{
              opacity: 0.2 * progressIntensity,
              background: `
                radial-gradient(ellipse 150px 100px at 30% 40%, rgba(129, 199, 132, 0.3), transparent 80%),
                radial-gradient(ellipse 200px 80px at 80% 70%, rgba(165, 214, 167, 0.25), transparent 75%)
              `,
              animation: 'leafShadows 10s ease-in-out infinite reverse',
            }}
          />
        </div>
      );
    }
    if (animation === 'crystalline') {
      return (
        <div className="absolute inset-0 overflow-hidden" style={baseStyle}>
          {baseColorLayer}
          <div
            className="absolute inset-0"
            style={{
              background: `radial-gradient(circle at 50% 50%, transparent 30%, ${color} 70%)`,
              animation: 'crystallize 20s ease-in-out infinite',
              transform: `rotate(${progress * 3.6}deg)`,
            }}
          />
        </div>
      );
    }
    if (animation === 'twinkle') {
      const progressIntensity = 1 + (safeProgress / 100) * 0.4;
      return (
        <div className="absolute inset-0 overflow-hidden" style={baseStyle}>
          {baseColorLayer}
          {/* Twinkling Stars Layer 1 - Large Stars */}
          <div
            className="absolute inset-0"
            style={{
              opacity: 0.8 * progressIntensity,
              background: `
                radial-gradient(circle 3px at 15% 20%, white, transparent 60%),
                radial-gradient(circle 2px at 85% 15%, white, transparent 50%),
                radial-gradient(circle 3px at 45% 35%, white, transparent 60%),
                radial-gradient(circle 2px at 70% 50%, white, transparent 50%),
                radial-gradient(circle 3px at 25% 65%, white, transparent 60%),
                radial-gradient(circle 2px at 90% 75%, white, transparent 50%),
                radial-gradient(circle 3px at 60% 85%, white, transparent 60%)
              `,
              animation: 'starTwinkle 3s ease-in-out infinite',
            }}
          />
          {/* Twinkling Stars Layer 2 - Medium Stars */}
          <div
            className="absolute inset-0"
            style={{
              opacity: 0.6 * progressIntensity,
              background: `
                radial-gradient(circle 2px at 30% 10%, rgba(255,255,255,0.8), transparent 50%),
                radial-gradient(circle 2px at 55% 25%, rgba(255,255,255,0.8), transparent 50%),
                radial-gradient(circle 2px at 20% 45%, rgba(255,255,255,0.8), transparent 50%),
                radial-gradient(circle 2px at 75% 40%, rgba(255,255,255,0.8), transparent 50%),
                radial-gradient(circle 2px at 40% 70%, rgba(255,255,255,0.8), transparent 50%),
                radial-gradient(circle 2px at 80% 60%, rgba(255,255,255,0.8), transparent 50%)
              `,
              animation: 'starTwinkle 4s ease-in-out infinite',
              animationDelay: '1s'
            }}
          />
          {/* Small Twinkling Stars - Distant Stars */}
          <div
            className="absolute inset-0"
            style={{
              opacity: 0.4 * progressIntensity,
              background: `
                radial-gradient(circle 1px at 10% 30%, rgba(255,255,255,0.6), transparent 40%),
                radial-gradient(circle 1px at 50% 15%, rgba(255,255,255,0.6), transparent 40%),
                radial-gradient(circle 1px at 35% 55%, rgba(255,255,255,0.6), transparent 40%),
                radial-gradient(circle 1px at 65% 70%, rgba(255,255,255,0.6), transparent 40%),
                radial-gradient(circle 1px at 22% 80%, rgba(255,255,255,0.6), transparent 40%),
                radial-gradient(circle 1px at 88% 25%, rgba(255,255,255,0.6), transparent 40%),
                radial-gradient(circle 1px at 95% 90%, rgba(255,255,255,0.6), transparent 40%)
              `,
              animation: 'starTwinkle 5s ease-in-out infinite',
              animationDelay: '2s'
            }}
          />
          {/* Meteor Streak */}
          {progressIntensity > 1.2 && (
            <div
              className="absolute w-1 h-20 bg-gradient-to-b from-white/80 to-transparent rounded-full"
              style={{
                top: '10%',
                left: '80%',
                transform: 'rotate(45deg)',
                animation: 'meteorFall 8s ease-in infinite',
                animationDelay: '3s'
              }}
            />
          )}
        </div>
      );
    }
    if (animation === 'spiral') {
      return (
        <div className="absolute inset-0 overflow-hidden" style={baseStyle}>
          {baseColorLayer}
          <div
            className="absolute inset-0"
            style={{
              background: `radial-gradient(ellipse at center, transparent 20%, ${color} 80%)`,
              animation: 'spiral 24s linear infinite',
              transform: `rotate(${progress * 7.2}deg)`,
            }}
          />
        </div>
      );
    }

    if (animation === 'snake') {
      // Neon Snake animation - electric green snake moving on black background
      const snakeLength = 25; // Number of segments
      const segmentSize = 20; // Size of each segment in pixels

      // Calculate snake path using sine wave
      const time = animationTime / 1000; // Convert to seconds
      const snakeSegments = [];

      for (let i = 0; i < snakeLength; i++) {
        const offset = i * 0.3; // Space between segments
        const x = 50 + Math.sin(time * 0.8 + offset) * 40; // Horizontal sine wave (%)
        const y = (time * 8 + offset * 3) % 100; // Vertical movement (%)

        // Fade out tail segments
        const opacity = 1 - (i / snakeLength) * 0.7;

        // Glow intensity - head glows more
        const glowSize = i === 0 ? 40 : 20 - (i / snakeLength) * 10;

        snakeSegments.push(
          <div
            key={i}
            className="absolute"
            style={{
              left: `${x}%`,
              top: `${y}%`,
              width: `${segmentSize}px`,
              height: `${segmentSize}px`,
              transform: 'translate(-50%, -50%)',
              background: `radial-gradient(circle, #39ff14 0%, #00ff00 50%, transparent 100%)`,
              opacity: opacity,
              boxShadow: `0 0 ${glowSize}px #39ff14, 0 0 ${glowSize * 1.5}px #00ff00`,
              borderRadius: '50%',
              filter: 'blur(0.5px)',
            }}
          />
        );
      }

      return (
        <div className="absolute inset-0 overflow-hidden" style={{ ...baseStyle, background: '#000000' }}>
          {snakeSegments}
        </div>
      );
    }

    if (animation === 'neonbg') {
      // Neon Background - full screen neon light waves and glow
      const time = animationTime / 1000;

      // Multiple color waves moving across the screen
      const wave1X = 50 + Math.sin(time * 0.3) * 30;
      const wave1Y = 50 + Math.cos(time * 0.25) * 30;
      const pulse1 = 0.6 + Math.sin(time * 0.8) * 0.4;

      const wave2X = 50 + Math.sin(time * 0.4 + 2) * 35;
      const wave2Y = 50 + Math.cos(time * 0.35 + 2) * 35;
      const pulse2 = 0.6 + Math.sin(time * 0.9 + 1) * 0.4;

      const wave3X = 50 + Math.sin(time * 0.35 + 4) * 40;
      const wave3Y = 50 + Math.cos(time * 0.3 + 4) * 40;
      const pulse3 = 0.6 + Math.sin(time * 0.7 + 2) * 0.4;

      const wave4X = 50 + Math.sin(time * 0.45 + 1) * 25;
      const wave4Y = 50 + Math.cos(time * 0.4 + 1) * 25;
      const pulse4 = 0.6 + Math.sin(time * 1.0 + 3) * 0.4;

      return (
        <div className="absolute inset-0 overflow-hidden" style={{ ...baseStyle, background: '#000000' }}>
          {/* Hot Pink Wave */}
          <div
            className="absolute inset-0"
            style={{
              background: `radial-gradient(circle at ${wave1X}% ${wave1Y}%, rgba(255, 0, 110, ${pulse1 * 0.8}), transparent 50%)`,
              filter: 'blur(80px)',
            }}
          />

          {/* Electric Blue Wave */}
          <div
            className="absolute inset-0"
            style={{
              background: `radial-gradient(circle at ${wave2X}% ${wave2Y}%, rgba(58, 134, 255, ${pulse2 * 0.7}), transparent 50%)`,
              filter: 'blur(90px)',
            }}
          />

          {/* Cyan Wave */}
          <div
            className="absolute inset-0"
            style={{
              background: `radial-gradient(circle at ${wave3X}% ${wave3Y}%, rgba(0, 245, 255, ${pulse3 * 0.6}), transparent 50%)`,
              filter: 'blur(100px)',
            }}
          />

          {/* Purple Wave */}
          <div
            className="absolute inset-0"
            style={{
              background: `radial-gradient(circle at ${wave4X}% ${wave4Y}%, rgba(191, 0, 255, ${pulse4 * 0.7}), transparent 50%)`,
              filter: 'blur(85px)',
            }}
          />

          {/* Neon Green Accent */}
          <div
            className="absolute inset-0"
            style={{
              background: `radial-gradient(circle at ${100 - wave1X}% ${100 - wave1Y}%, rgba(57, 255, 20, ${pulse1 * 0.5}), transparent 45%)`,
              filter: 'blur(110px)',
            }}
          />

          {/* Orange Accent */}
          <div
            className="absolute inset-0"
            style={{
              background: `radial-gradient(circle at ${100 - wave2X}% ${100 - wave2Y}%, rgba(255, 111, 0, ${pulse2 * 0.6}), transparent 45%)`,
              filter: 'blur(95px)',
            }}
          />

          {/* Overall glow overlay */}
          <div
            className="absolute inset-0"
            style={{
              background: `radial-gradient(circle at 50% 50%, rgba(255, 255, 255, 0.05), transparent 70%)`,
              opacity: pulse1,
            }}
          />
        </div>
      );
    }

    return (
      <div
        className="absolute inset-0 transition-all duration-1000 ease-out"
        style={{ background: color, ...baseStyle }}
      />
    );
  }

  // PropTypes for AnimatedBackground component
  AnimatedBackground.propTypes = {
    animation: PropTypes.string,
    color: PropTypes.string,
    brightness: PropTypes.number,
  }

  // ========== SIMULATION LOGIC ==========
  // Simple color preview test - just shows color transition, no fullscreen
  const startColorPreview = () => {
    if (isRunning) return;

    setIsRunning(true);
    const totalDuration = 20000; // 20 seconds
    const interval = 100;
    let elapsed = 0;

    const progressInterval = setInterval(() => {
      elapsed += interval;
      const newProgress = Math.min((elapsed / totalDuration) * 100, 100);
      setProgress(newProgress);
      // Map progress from 0-100% to brightness 50-100% (mimicking the 10-minute to alarm time brightness curve)
      setBrightness(0.5 + (newProgress / 100) * 0.5);

      if (newProgress >= 100) {
        clearInterval(progressInterval);
        setIsRunning(false);
      }
    }, interval);
  }

  const startPreview = async (customDuration = null) => {
    if (isRunning) return;

    // Keep screen awake on native platform
    if (Capacitor.isNativePlatform()) {
      await KeepAwake.keepAwake();
    }

    if (currentMode === 'sleep') {
      // Enter browser fullscreen to hide notifications
      await enterFullscreen();
      setIsFullscreen(true);
      setIsRunning(true);

      const config = getCurrentThemeConfig();
      const totalDuration = sleepTimer * 60 * 1000;
      const interval = 100;
      let elapsed = 0;

      const sleepInterval = setInterval(() => {
        elapsed += interval;
        const newProgress = Math.min((elapsed / totalDuration) * 100, 100);
        setProgress(newProgress);

        if (newProgress >= 100) {
          // Sleep timer completed - transition to screen saver mode
          setIsRunning(false);
          setIsScreenSaver(true);
          clearInterval(sleepInterval);
          setProgress(0);
          setBrightness(0.01);
        } else {
          // Update brightness (will be overridden by comfort light if enabled)
          if (!isComfortLightEnabled) {
            setBrightness(calculateBrightness());
          }
        }
      }, interval);

      return;
    }

    // Wake mode - ALWAYS enter fullscreen for real alarm
    await enterFullscreen();
    setIsFullscreen(true);
    setIsRunning(true);

    const config = getCurrentThemeConfig();
    // Use custom duration (20 minutes for alarm) or theme duration (for demo)
    const totalDuration = customDuration || (config.duration * 1000);
    const interval = 100;

    let elapsed = 0;

    const simulationInterval = setInterval(() => {
      elapsed += interval;
      const newProgress = Math.min((elapsed / totalDuration) * 100, 100);
      setProgress(newProgress);

      // If using custom duration (alarm mode), brightness reaches 100% at 50% progress (10 min)
      if (customDuration) {
        if (newProgress >= 50) {
          setBrightness(1.0); // Full brightness after 10 minutes
        } else {
          // 0-50% progress maps to 0-100% brightness
          const brightnessProgress = newProgress * 2; // Double the progress for brightness
          setBrightness(brightnessProgress / 100);
        }
      } else {
        // Demo mode - use normal brightness calculation
        if (newProgress >= 100) {
          setBrightness(1.0);
        } else {
          setBrightness(calculateBrightness());
        }
      }
    }, interval);
  }

  const stopSimulation = async () => {
    setIsRunning(false);
    setIsFullscreen(false);
    setIsScreenSaver(false);
    // Clear any running intervals
    // Note: Intervals are now managed locally within their respective functions
    
    // Allow screen to sleep on native platform
    if (Capacitor.isNativePlatform()) {
      await KeepAwake.allowSleep();
    }
    
    // Exit browser fullscreen
    await exitFullscreen();
  }

  // Stop simulation with confirmation when alarm light is running
  const stopSimulationWithConfirm = async () => {
    // If alarm is enabled, show confirmation
    if (isAlarmEnabled) {
      const confirmed = window.confirm('Are you sure you want to turn off the light? The alarm will still ring at the scheduled time.');
      if (!confirmed) {
        return; // Don't stop if user cancels
      }
      // User confirmed - stop light but keep alarm enabled
      await stopSimulation();
      // Alarm stays enabled for sound to play later
    } else {
      // Normal stop (not alarm mode, or alarm sound already playing)
      await stopSimulation();
    }
  }

  const resetSimulation = () => {
    stopSimulation();
    setProgress(0);
    setBrightness(currentMode === 'wake' ? 0.01 : 1.0);
  }

  const handleWakeTouch = () => {
    if (currentMode === 'wake' && isRunning && progress >= 100) {
      stopSimulation();
    }
  }

  // ========== EFFECTS ==========
  useEffect(() => {
    setBrightness(calculateBrightness());
  }, [progress, currentMode]);

  useEffect(() => {
    resetSimulation();
    const currentThemes = getCurrentThemes();
    if (!currentThemes[currentTheme]) {
      const firstTheme = Object.keys(currentThemes)[0];
      if (firstTheme && firstTheme !== currentTheme) {
        setCurrentTheme(firstTheme);
      }
    }
  }, [currentMode]);

  const currentColor = getCurrentColor();
  const config = getCurrentThemeConfig();

  // ========== COMFORT LIGHT SETTINGS SCREEN ==========
  if (showComfortLightSettings) {
    return (
      <div className="min-h-screen flex flex-col relative overflow-hidden">
        <div
          className="absolute inset-0"
          style={{ background: 'linear-gradient(135deg, #ff6600 0%, #cc0000 50%, #660000 100%)' }}
        />

        <div className="relative z-10 min-h-screen flex flex-col text-white">
          <div className="p-6">
            <div className="flex items-center justify-between mb-6">
              <div className="flex items-center space-x-3">
                <Lightbulb className="w-8 h-8" />
                <h1 className="text-2xl font-bold">Comfort Light</h1>
              </div>
              <button
                onClick={() => setShowComfortLightSettings(false)}
                className="w-10 h-10 rounded-full bg-white/20 flex items-center justify-center hover:bg-white/30 transition-all"
              >
                ✕
              </button>
            </div>

            {/* Enable/Disable Toggle */}
            <div className="mb-6 p-4 bg-white/10 rounded-lg">
              <div className="flex items-center justify-between mb-2">
                <h2 className="text-lg font-semibold">Enable Comfort Light</h2>
                <button
                  onClick={() => {
                    setIsComfortLightEnabled(!isComfortLightEnabled);
                    if (isComfortLightEnabled && isComfortLightRunning) {
                      stopComfortLight();
                    }
                  }}
                  className={`w-12 h-6 rounded-full transition-all ${
                    isComfortLightEnabled ? 'bg-green-500' : 'bg-white/20'
                  }`}
                >
                  <div className={`w-5 h-5 bg-white rounded-full transition-transform ${
                    isComfortLightEnabled ? 'translate-x-6' : 'translate-x-0.5'
                  }`} />
                </button>
              </div>
              <p className="text-sm opacity-70">
                {"Sleep-friendly red light that doesn't disrupt melatonin production"}
              </p>
            </div>

            {isComfortLightEnabled && (
              <>
                {/* Brightness Control */}
                <div className="mb-6">
                  <h3 className="text-lg font-semibold mb-4">Brightness</h3>
                  <div className="p-4 bg-white/10 rounded-lg">
                    <div className="mb-3">
                      <div className="flex justify-between items-center mb-2">
                        <span className="text-sm">Very Dim</span>
                        <span className="text-sm">Bright</span>
                      </div>
                      <input
                        type="range"
                        min="0.05"
                        max="0.8"
                        step="0.05"
                        value={comfortLightBrightness}
                        onChange={(e) => setComfortLightBrightness(parseFloat(e.target.value))}
                        className="w-full h-2 bg-white/20 rounded-lg appearance-none cursor-pointer slider"
                      />
                    </div>
                    <p className="text-xs opacity-60">
                      Default is extremely dim for maximum sleep comfort
                    </p>
                  </div>
                </div>

                {/* Color Selection */}
                <div className="mb-6">
                  <h3 className="text-lg font-semibold mb-4">Color Tone</h3>
                  <div className="space-y-3">
                    {Object.entries(comfortLightColors).map(([key, color]) => (
                      <button
                        key={key}
                        onClick={() => setComfortLightColor(key)}
                        className={`w-full p-4 rounded-lg transition-all ${
                          comfortLightColor === key
                            ? 'bg-white/20 border-2 border-white/50'
                            : 'bg-white/10 hover:bg-white/15'
                        }`}
                      >
                        <div className="flex items-center space-x-4">
                          <div
                            className="w-12 h-12 rounded-full"
                            style={{ background: color.gradient }}
                          />
                          <div className="text-left">
                            <h4 className="font-medium">{color.name}</h4>
                            <p className="text-sm opacity-70">{color.description}</p>
                          </div>
                          {comfortLightColor === key && (
                            <div className="ml-auto w-6 h-6 rounded-full bg-green-500 flex items-center justify-center">
                              ✓
                            </div>
                          )}
                        </div>
                      </button>
                    ))}
                  </div>
                </div>

                {/* Scientific Info */}
                <div className="p-4 bg-blue-500/20 rounded-lg">
                  <h4 className="font-semibold mb-2">🧬 The Science</h4>
                  <p className="text-sm opacity-80">
                    Red and amber light wavelengths have minimal impact on melatonin production,
                    helping you maintain natural sleep cycles while providing gentle illumination for comfort and safety.
                  </p>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    );
  }

  // ========== SETTINGS SCREEN ==========
  if (showSettings) {
    return (
      <div className="min-h-screen flex flex-col relative overflow-hidden">
        <div
          className="absolute inset-0"
          style={{ background: '#ff6b35' }}
        />

        <div className="relative z-10 min-h-screen flex flex-col" style={{ color: '#422d22' }}>
          <div className="p-6">
            <div className="flex items-center justify-between mb-6">
              <div className="flex items-center space-x-3">
                <Settings className="w-8 h-8" />
                <h1 className="text-2xl font-bold">Settings</h1>
              </div>
              <button
                onClick={() => setShowSettings(false)}
                className="w-10 h-10 rounded-full flex items-center justify-center transition-all"
                style={{ backgroundColor: '#ffa500' }}
              >
                ✕
              </button>
            </div>


            {/* Wake-Up Sequence Info */}
            <div className="mb-6">
              <h2 className="text-lg font-semibold mb-4 flex items-center space-x-2">
                <Clock className="w-5 h-5" />
                <span>Wake-Up Sequence</span>
              </h2>

              <div className="p-4 rounded-lg border" style={{ backgroundColor: 'rgba(255, 248, 225, 0.8)', borderColor: '#ffa500' }}>
                <div className="space-y-3">
                  <div className="flex items-center space-x-3">
                    <div className="w-10 h-10 bg-orange-500/30 rounded-full flex items-center justify-center">
                      <span className="text-xl">🌅</span>
                    </div>
                    <div className="flex-1">
                      <h3 className="text-sm font-semibold" style={{ color: '#422d22' }}>Light Starts</h3>
                      <p className="text-xs" style={{ color: '#422d22', opacity: 0.7 }}>20 minutes before alarm - gradual sunrise simulation</p>
                    </div>
                  </div>

                  <div className="flex items-center space-x-3">
                    <div className="w-10 h-10 bg-green-500/30 rounded-full flex items-center justify-center">
                      <span className="text-xl">🔊</span>
                    </div>
                    <div className="flex-1">
                      <h3 className="text-sm font-semibold" style={{ color: '#422d22' }}>Sound Starts</h3>
                      <p className="text-xs" style={{ color: '#422d22', opacity: 0.7 }}>30 seconds before alarm - gentle volume increase (30% → 100%)</p>
                    </div>
                  </div>

                  <div className="text-xs p-3 rounded mt-3" style={{ color: '#422d22', opacity: 0.7, backgroundColor: 'rgba(255, 248, 225, 0.5)' }}>
                    <strong>💡 Tip:</strong> This timing is optimized for natural, gentle wake-ups. Your screen will brighten like a sunrise, then sound gently fades in.
                  </div>
                </div>
              </div>
            </div>

            {/* Notice Board */}
            <div className="mb-6">
              <h2 className="text-lg font-semibold mb-4 flex items-center space-x-2">
                <span className="text-2xl">📢</span>
                <span>Notice Board</span>
              </h2>
              <div className="p-4 rounded-lg border" style={{ backgroundColor: 'rgba(255, 248, 225, 0.8)', borderColor: '#ffa500' }}>
                <p className="text-sm" style={{ color: '#422d22' }}>
                  <strong>🎵 New music/theme stay tuned.</strong>
                </p>
                  </div>
                </div>

            {/* Feedback Section */}
            <div className="mb-6">
              <h2 className="text-lg font-semibold mb-4 flex items-center space-x-2">
                <span className="text-2xl">💬</span>
                <span>Feedback</span>
              </h2>
              <div className="p-4 rounded-lg border" style={{ backgroundColor: 'rgba(255, 248, 225, 0.8)', borderColor: '#ffa500' }}>
                <p className="text-sm mb-3" style={{ color: '#422d22' }}>
                  Help us improve the app by sharing your thoughts and suggestions.
                </p>
                <a 
                  href="mailto:lightalarmmy@gmail.com"
                  className="inline-flex items-center px-4 py-2 rounded-lg text-sm font-medium transition-all border"
                  style={{ 
                    backgroundColor: '#ffa500', 
                    color: '#422d22',
                    borderColor: '#ffa500'
                  }}
                >
                  📧 Send Feedback
                </a>
                  </div>
                </div>

                </div>
        </div>
      </div>
    );
  }

  // ========== FULLSCREEN COMFORT LIGHT MODE ==========
  if (isComfortLightRunning && !isRunning) {
    const comfortConfig = comfortLightColors[comfortLightColor];

    return (
      <div
        className="fixed inset-0 flex items-center justify-center z-50 overflow-hidden"
        onClick={() => setIsComfortLightRunning(false)}
        style={{
          background: comfortConfig.color,
          filter: `brightness(${comfortLightBrightness * 5})` // Scale up for visibility
        }}
      >
        {/* Comfort Light Overlay */}
        <div
          className="absolute inset-0"
          style={{
            background: comfortConfig.gradient,
            opacity: comfortLightBrightness * 3, // Very dim
            filter: 'blur(40px)'
          }}
        />

        {/* Breathing Animation for Comfort Light */}
        {(
          <div
            className="absolute inset-0"
            style={{
              background: `radial-gradient(circle at center, ${comfortConfig.color}40, transparent 60%)`,
              opacity: getBreathingPattern(animationTime) * comfortLightBrightness * 2,
              transform: `scale(${0.8 + getBreathingPattern(animationTime) * 0.2})`,
            }}
          />
        )}

        {/* Comfort Light Info Overlay */}
        <div className="absolute top-4 left-4 right-4 text-center">
          <div className="text-white/70 text-sm">
            🕯️ Comfort Light • {comfortConfig.name}
          </div>
          <div className="text-white/50 text-xs mt-1">
            {comfortLightDuration === 'all-night'
              ? 'Running until sunrise alarm'
              : `${comfortLightDurations[comfortLightDuration].name} timer`
            }
          </div>
          <div className="text-white/40 text-xs mt-2">
            Tap anywhere to stop
          </div>
        </div>

        {/* Seamless Transition Indicator */}
        {comfortLightDuration === 'all-night' && isAlarmEnabled && (
          <div className="absolute bottom-4 left-4 right-4 text-center">
            <div className="text-white/50 text-xs">
              ✨ Will seamlessly transition to sunrise alarm at {alarmTime} {alarmAmPm}
            </div>
          </div>
        )}
      </div>
    );
  }

  // Fullscreen sleep mode
  if (isFullscreen && currentMode === 'sleep') {
    const config = getCurrentThemeConfig();

    // For comfort light, use candle flicker animation
    if (isComfortLightEnabled) {
      const comfortConfig = comfortLightColors[comfortLightColor];

      return (
        <div
          className="fixed inset-0 flex items-center justify-center z-50 overflow-hidden"
          onClick={stopSimulationWithConfirm}
          style={{
            backgroundColor: '#000000',
            filter: `brightness(${comfortLightBrightness * 5})`
          }}
        >
          {/* Ambient Glow - Outer Layer */}
          <div
            className="absolute inset-0"
            style={{
              background: `radial-gradient(ellipse 60% 80% at 50% 50%, ${comfortConfig.color}20, transparent 60%)`,
              animation: 'candleGlow 5s ease-in-out infinite'
            }}
          />

          {/* Main Flame - Center */}
          <div
            className="absolute"
            style={{
              bottom: '30%',
              left: '50%',
              transform: 'translateX(-50%)',
              width: '40vw',
              height: '60vh',
              background: `radial-gradient(ellipse at 50% 100%, ${comfortConfig.color}ff 0%, ${comfortConfig.color}dd 20%, ${comfortConfig.color}88 40%, ${comfortConfig.color}44 60%, transparent 80%)`,
              borderRadius: '50% 50% 50% 50% / 60% 60% 40% 40%',
              filter: 'blur(40px)',
              animation: 'candleFlame1 3s ease-in-out infinite'
            }}
          />

          {/* Secondary Flame Layer */}
          <div
            className="absolute"
            style={{
              bottom: '30%',
              left: '50%',
              transform: 'translateX(-50%)',
              width: '30vw',
              height: '50vh',
              background: `radial-gradient(ellipse at 50% 100%, ${comfortConfig.color}ff 0%, ${comfortConfig.color}aa 30%, ${comfortConfig.color}55 60%, transparent 80%)`,
              borderRadius: '50% 50% 50% 50% / 65% 65% 35% 35%',
              filter: 'blur(30px)',
              animation: 'candleFlame2 2.5s ease-in-out infinite'
            }}
          />

          {/* Flicker Layer */}
          <div
            className="absolute inset-0"
            style={{
              background: `radial-gradient(circle at 50% 50%, ${comfortConfig.color}30, transparent 50%)`,
              animation: 'candleFlicker 1.5s ease-in-out infinite'
            }}
          />

          <div className="text-center text-white/30 z-10">
            <Moon className="w-12 h-12 mx-auto mb-3 opacity-20" />
            <p className="text-sm mb-1 opacity-30">Sleep Mode - {sleepTimer} min</p>
            <p className="text-xs opacity-20">Tap to stop</p>
          </div>
        </div>
      );
    }

    // For themed sleep mode, use animated background
    // Apply dynamic brightness variation if enabled
    const finalBrightness = brightness + (isDynamicBrightness ? brightnessVariation : 0);

    return (
      <div
        className="fixed inset-0 z-50 overflow-hidden"
        onClick={stopSimulationWithConfirm}
        style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          width: '100vw',
          height: '100vh',
          margin: 0,
          padding: 0,
        }}
      >
        <AnimatedBackground
          animation={config.animation}
          color={currentColor}
          brightness={finalBrightness}
        />
        {/* Moon icon - very subtle */}
        <div className="absolute inset-0 flex items-center justify-center">
          <div className="text-center text-white/30 z-10">
            <Moon className="w-12 h-12 mx-auto mb-3 opacity-20" />
            <p className="text-sm mb-1 opacity-30">
              Sleep Mode - {sleepTimer} minutes
              {isDynamicBrightness && <span className="text-xs opacity-30 ml-2">✨</span>}
            </p>
            <p className="text-xs opacity-20">Tap to stop</p>
          </div>
        </div>
      </div>
    );
  }

  // ========== SCREEN SAVER MODE (after sleep timer ends) ==========
  if (isScreenSaver && isFullscreen) {
    const handleScreenSaverTap = () => {
      setIsScreenSaver(false);
      setIsFullscreen(false);
      exitFullscreen();
    }

    // Pure black screen saver - no animations, saves battery
    return (
      <div
        className="fixed inset-0 z-50 overflow-hidden"
        onClick={handleScreenSaverTap}
        style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          width: '100vw',
          height: '100vh',
          margin: 0,
          padding: 0,
          backgroundColor: '#000000', // Pure black - screen appears OFF
        }}
      >
        <div className="absolute inset-0 flex items-center justify-center">
          <div className="text-center text-white/30 z-10">
            <Moon className="w-12 h-12 mx-auto mb-3 opacity-20" />
            <p className="text-xs opacity-20">Tap to exit</p>
          </div>
        </div>
      </div>
    );
  }

  // ========== FULLSCREEN WAKE MODE (ALARM) ==========
  if (isFullscreen && currentMode === 'wake') {
    const handleFullscreenExit = (e) => {
      e.preventDefault();
      e.stopPropagation();
      console.log('Wake mode fullscreen clicked - stopping simulation');
      stopSimulation();
    }

    return (
      <div
        className="fixed inset-0 flex items-center justify-center z-50 overflow-hidden"
        onClick={handleFullscreenExit}
        onTouchStart={handleFullscreenExit}
        onTouchEnd={handleFullscreenExit}
        style={{ cursor: 'pointer', touchAction: 'manipulation' }}
      >
        <div style={{ pointerEvents: 'none' }}>
          <AnimatedBackground
            animation={config.animation}
            color={currentColor}
            brightness={brightness}
          />
        </div>

        <div className="absolute top-8 left-8 right-8 z-10" style={{ pointerEvents: 'none' }}>
          <div className="w-full h-2 bg-white/20 rounded-full">
            <div
              className="h-full bg-white/80 rounded-full transition-all duration-100"
              style={{ width: `${progress}%` }}
            />
          </div>
        </div>

        <div className="text-center text-white z-10">
          <Sun className="w-20 h-20 mx-auto mb-6 opacity-90" />
          <p className="text-2xl mb-4 font-semibold">Good Morning!</p>
          <p className="text-lg mb-2">{currentTime.toLocaleTimeString('en-US', {
            hour: '2-digit',
            minute: '2-digit',
            hour12: true
          })}</p>
          <p className="text-sm opacity-80 mb-8">{Math.round(progress)}% complete</p>
        </div>

        {/* Stop Alarm Button */}
        <div className="absolute bottom-8 left-1/2 transform -translate-x-1/2 z-20">
          <button
            onClick={stopSimulationWithConfirm}
            className="bg-white/20 hover:bg-white/30 text-white px-8 py-4 rounded-full text-lg font-semibold backdrop-blur-sm border border-white/30 transition-all duration-200"
          >
            Stop Alarm
          </button>
        </div>

        {/* Wake lock indicator */}
        <div className="absolute top-4 right-4 z-20">
          <div className="bg-white/10 backdrop-blur-sm rounded-full px-3 py-1">
            <span className="text-white/70 text-xs">Screen Active</span>
          </div>
        </div>
      </div>
    );
  }

  // ========== DEBUG & FALLBACK ==========
  console.log('🔍 Render state:', { isFullscreen, currentMode, isRunning, progress: Math.round(progress), brightness });

  // Fallback: If fullscreen but no specific mode matched, provide exit button
  if (isFullscreen) {
    console.warn('⚠️ Fullscreen is true but no mode matched! Showing fallback exit.');
    return (
      <div
        className="fixed inset-0 bg-black z-[9999] flex items-center justify-center"
        onClick={async () => {
          console.log('🔴 FALLBACK EXIT CLICKED');
          await stopSimulation();
        }}
      >
        <div className="text-white text-center">
          <p className="text-2xl mb-4">Fullscreen Active</p>
          <p className="text-sm opacity-60">Mode: {currentMode} | Running: {isRunning ? 'Yes' : 'No'}</p>
          <p className="text-sm opacity-60 mt-2">Tap anywhere to exit</p>
        </div>
      </div>
    );
  }

  // ========== MAIN APP INTERFACE ==========
  return (
    <div className="min-h-screen flex flex-col relative overflow-hidden">
      {/* CSS Animations */}
      <style dangerouslySetInnerHTML={{
        __html: `
          @keyframes auroraFlow1 {
            0%, 100% { transform: translateY(0) skewX(0deg); opacity: 0.7; }
            25% { transform: translateY(-20px) skewX(2deg); opacity: 0.9; }
            50% { transform: translateY(10px) skewX(-1deg); opacity: 0.6; }
            75% { transform: translateY(-10px) skewX(1deg); opacity: 0.8; }
          }
          @keyframes auroraFlow2 {
            0%, 100% { transform: translateX(0) scaleY(1); opacity: 0.5; }
            33% { transform: translateX(15px) scaleY(1.1); opacity: 0.7; }
            66% { transform: translateX(-10px) scaleY(0.9); opacity: 0.4; }
          }
          @keyframes oceanWave1 {
            0%, 100% { transform: translateY(0) scale(1); }
            50% { transform: translateY(-15px) scale(1.05); }
          }
          @keyframes oceanWave2 {
            0%, 100% { transform: translateX(0) scaleX(1); }
            50% { transform: translateX(20px) scaleX(1.1); }
          }
          @keyframes sunRays {
            0%, 100% { transform: rotate(0deg) scale(1); opacity: 0.5; }
            50% { transform: rotate(5deg) scale(1.05); opacity: 0.7; }
          }
          @keyframes sunGlow {
            0%, 100% { transform: scale(1); opacity: 0.6; }
            50% { transform: scale(1.1); opacity: 0.8; }
          }
          @keyframes sunrise {
            0%, 100% { transform: translateY(20px) scale(0.9); opacity: 0.4; }
            50% { transform: translateY(-10px) scale(1.1); opacity: 0.7; }
          }

          /* Nendo-Inspired Animation Keyframes */

          /* Sunrise: Solar Breathing & Light Rays */
          @keyframes solarBreathing {
            0%, 100% { transform: scale(0.95) translateY(5px); opacity: 0.4; }
            33% { transform: scale(1.05) translateY(-2px); opacity: 0.7; }
            66% { transform: scale(1.02) translateY(0px); opacity: 0.6; }
          }
          @keyframes sunRays {
            0%, 100% { transform: rotate(0deg) scaleX(0.8); opacity: 0.3; }
            50% { transform: rotate(3deg) scaleX(1.2); opacity: 0.6; }
          }
          @keyframes dustMotes {
            0%, 100% { transform: translateY(100vh) translateX(0px) rotate(0deg); opacity: 0; }
            20% { opacity: 0.4; }
            80% { opacity: 0.2; }
            100% { transform: translateY(-20vh) translateX(15px) rotate(360deg); opacity: 0; }
          }

          /* Green Grass: Dappled Light & Leaf Shadows */
          @keyframes dappledLight {
            0%, 100% { transform: translateX(0px) scale(1); opacity: 0.3; }
            25% { transform: translateX(10px) scale(1.1); opacity: 0.5; }
            50% { transform: translateX(-5px) scale(0.9); opacity: 0.4; }
            75% { transform: translateX(8px) scale(1.05); opacity: 0.6; }
          }
          @keyframes leafShadows {
            0%, 100% { transform: translateX(0px) skewX(0deg); opacity: 0.2; }
            50% { transform: translateX(-8px) skewX(2deg); opacity: 0.4; }
          }
          @keyframes pollenDrift {
            0%, 100% { transform: translateX(-20px) translateY(0px) rotate(0deg); opacity: 0; }
            20% { opacity: 0.6; }
            80% { opacity: 0.3; }
            100% { transform: translateX(120vw) translateY(-10px) rotate(180deg); opacity: 0; }
          }

          /* Candle Flame Animation */
          @keyframes candleFlame1 {
            0% {
              transform: translateY(0px) scaleY(1) scaleX(1);
              opacity: 0.8;
              border-radius: 50% 50% 50% 50% / 60% 60% 40% 40%;
            }
            25% {
              transform: translateY(-3px) scaleY(1.08) scaleX(0.95);
              opacity: 0.9;
              border-radius: 50% 50% 50% 50% / 70% 70% 30% 30%;
            }
            50% {
              transform: translateY(-6px) scaleY(1.15) scaleX(0.92);
              opacity: 0.85;
              border-radius: 45% 55% 50% 50% / 65% 65% 35% 35%;
            }
            75% {
              transform: translateY(-2px) scaleY(1.05) scaleX(0.97);
              opacity: 0.88;
              border-radius: 55% 45% 50% 50% / 68% 68% 32% 32%;
            }
            100% {
              transform: translateY(0px) scaleY(1) scaleX(1);
              opacity: 0.8;
              border-radius: 50% 50% 50% 50% / 60% 60% 40% 40%;
            }
          }
          @keyframes candleFlame2 {
            0% {
              transform: translateY(0px) translateX(0px) scaleY(1) scaleX(1);
              opacity: 0.6;
            }
            20% {
              transform: translateY(-4px) translateX(-2px) scaleY(1.1) scaleX(0.9);
              opacity: 0.7;
            }
            40% {
              transform: translateY(-7px) translateX(2px) scaleY(1.18) scaleX(0.88);
              opacity: 0.65;
            }
            60% {
              transform: translateY(-5px) translateX(-1px) scaleY(1.12) scaleX(0.92);
              opacity: 0.68;
            }
            80% {
              transform: translateY(-2px) translateX(1px) scaleY(1.05) scaleX(0.96);
              opacity: 0.62;
            }
            100% {
              transform: translateY(0px) translateX(0px) scaleY(1) scaleX(1);
              opacity: 0.6;
            }
          }
          @keyframes candleFlicker {
            0%, 100% { opacity: 1; filter: brightness(1); }
            10% { opacity: 0.95; filter: brightness(0.95); }
            20% { opacity: 1; filter: brightness(1.02); }
            30% { opacity: 0.92; filter: brightness(0.92); }
            40% { opacity: 0.98; filter: brightness(0.98); }
            50% { opacity: 0.94; filter: brightness(0.94); }
            60% { opacity: 1; filter: brightness(1); }
            70% { opacity: 0.96; filter: brightness(0.96); }
            80% { opacity: 0.93; filter: brightness(0.93); }
            90% { opacity: 0.99; filter: brightness(0.99); }
          }
          @keyframes candleGlow {
            0%, 100% {
              transform: scale(1);
              opacity: 0.4;
              filter: blur(80px);
            }
            33% {
              transform: scale(1.1);
              opacity: 0.5;
              filter: blur(90px);
            }
            66% {
              transform: scale(0.95);
              opacity: 0.35;
              filter: blur(75px);
            }
          }

          /* Blue Sea: Caustic Ripples & Wave Layers */
          @keyframes causticRipples {
            0%, 100% { transform: scale(1) rotate(0deg); opacity: 0.4; }
            50% { transform: scale(1.3) rotate(5deg); opacity: 0.7; }
          }
          @keyframes waveLayer1 {
            0%, 100% { transform: translateX(0px) scaleY(1); opacity: 0.3; }
            50% { transform: translateX(30px) scaleY(1.1); opacity: 0.5; }
          }
          @keyframes waveLayer2 {
            0%, 100% { transform: translateX(0px) scaleY(0.9); opacity: 0.2; }
            50% { transform: translateX(-20px) scaleY(1.05); opacity: 0.4; }
          }
          @keyframes bubbleRise {
            0%, 100% { transform: translateY(100vh) scale(0.5); opacity: 0; }
            50% { opacity: 0.6; }
            100% { transform: translateY(-20vh) scale(1.2); opacity: 0; }
          }

          /* Aurora: Magnetic Dance & Vertical Curtains */
          @keyframes magneticDance {
            0%, 100% { transform: translateY(0px) translateX(0px) skewY(0deg); opacity: 0.4; }
            25% { transform: translateY(-30px) translateX(20px) skewY(2deg); opacity: 0.7; }
            50% { transform: translateY(10px) translateX(-15px) skewY(-1deg); opacity: 0.5; }
            75% { transform: translateY(-10px) translateX(25px) skewY(1.5deg); opacity: 0.6; }
          }
          @keyframes verticalCurtains {
            0%, 100% { transform: translateY(0px) scaleX(1); opacity: 0.3; }
            33% { transform: translateY(-20px) scaleX(1.1); opacity: 0.6; }
            66% { transform: translateY(15px) scaleX(0.9); opacity: 0.4; }
          }
          @keyframes starSparkle {
            0%, 100% { opacity: 0; transform: scale(0.5); }
            50% { opacity: 0.8; transform: scale(1.2); }
          }

          /* Pink Ocean: Bioluminescence & Tidal Breathing */
          @keyframes bioluminescence {
            0%, 100% { opacity: 0; transform: scale(0.8); }
            25% { opacity: 0.7; transform: scale(1.3); }
            75% { opacity: 0.2; transform: scale(1.1); }
          }
          @keyframes tidalBreathing {
            0%, 100% { transform: scale(1); opacity: 0.4; }
            50% { transform: scale(1.05); opacity: 0.6; }
          }
          @keyframes jellyfishFloat {
            0%, 100% { transform: translateY(0px) rotate(0deg); opacity: 0.3; }
            50% { transform: translateY(-15px) rotate(5deg); opacity: 0.6; }
          }
          @keyframes circularRipple {
            0%, 100% { transform: scale(0.7); opacity: 0.5; }
            50% { transform: scale(1.4); opacity: 0.1; }
          }

          /* Lavender: Dream Clouds & Spiral Galaxy */
          @keyframes dreamClouds {
            0%, 100% { transform: translateX(0px) scale(1); opacity: 0.3; }
            50% { transform: translateX(20px) scale(1.1); opacity: 0.5; }
          }
          @keyframes spiralGalaxy {
            0%, 100% { transform: rotate(0deg) scale(1); opacity: 0.4; }
            50% { transform: rotate(180deg) scale(1.1); opacity: 0.6; }
          }
          @keyframes stardust {
            0%, 100% { transform: translateY(0px) rotate(0deg); opacity: 0.6; }
            50% { transform: translateY(10px) rotate(180deg); opacity: 0.2; }
          }
          @keyframes gradientWaves {
            0%, 100% { transform: scale(1) translateX(0px); opacity: 0.3; }
            50% { transform: scale(1.1) translateX(10px); opacity: 0.5; }
          }
          @keyframes dappledLight1 {
            0%, 100% { transform: translateY(0) translateX(0); opacity: 0.4; }
            25% { transform: translateY(-5px) translateX(3px); opacity: 0.6; }
            50% { transform: translateY(2px) translateX(-2px); opacity: 0.3; }
            75% { transform: translateY(-3px) translateX(1px); opacity: 0.5; }
          }
          @keyframes dappledLight2 {
            0%, 100% { transform: translateY(0) translateX(0); opacity: 0.3; }
            33% { transform: translateY(-3px) translateX(-4px); opacity: 0.5; }
            66% { transform: translateY(4px) translateX(2px); opacity: 0.2; }
          }
          @keyframes branchShadow {
            0%, 100% { transform: rotate(0deg) skewX(0deg); opacity: 0.15; }
            25% { transform: rotate(1deg) skewX(0.5deg); opacity: 0.2; }
            50% { transform: rotate(-0.5deg) skewX(-0.3deg); opacity: 0.1; }
            75% { transform: rotate(0.8deg) skewX(0.2deg); opacity: 0.18; }
          }
        `
      }} />

      {/* Animated Background */}
      <AnimatedBackground
        animation={isRunning ? config.animation : 'gradient'}
        color={isRunning ? currentColor : 'linear-gradient(135deg, #00d2ff 0%, #3a7bd5 50%, #ff6b35 100%)'}
        brightness={isRunning ? brightness : 1}
      />

      {/* Comfort Light Overlay */}
      {isComfortLightRunning && (
        <div
          className="fixed inset-0 z-20 transition-all duration-1000 ease-out"
          style={{
            ...getCurrentComfortLightColor(),
            opacity: comfortLightBrightness,
            mixBlendMode: 'overlay'
          }}
        />
      )}

      {/* Main Content */}
      <div className="relative z-10 min-h-screen flex flex-col pb-24"
           style={{
             background: !isRunning ? (currentMode === 'wake' ? 'linear-gradient(to bottom, #e0f2fe 0%, #bae6fd 80%, #fef3c7 90%, #fed7aa 100%)' : '#DDC5BB') : 'transparent'
           }}
           onClick={handleWakeTouch}>

        {/* Header */}
        <div className="p-6 pl-4">
          {currentMode === 'wake' ? (
            <div className="relative mb-8 overflow-hidden rounded-3xl bg-gradient-to-b from-blue-300 via-cyan-200 to-orange-200 p-5 border-2 border-white/70 backdrop-blur-sm shadow-lg">
              {/* Floating Clouds Animation */}
              <div className="absolute inset-0 overflow-hidden">
                <div className="absolute top-4 w-6 h-4 bg-white/30 rounded-full animate-float-left-to-right">
                  <div className="w-3 h-3 bg-white/40 rounded-full absolute -top-1 left-1"></div>
                  <div className="w-4 h-3 bg-white/30 rounded-full absolute -top-0.5 left-2"></div>
                  <div className="w-3 h-3 bg-white/40 rounded-full absolute -top-1 right-1"></div>
                </div>
                <div className="absolute top-6 w-8 h-5 bg-white/25 rounded-full animate-float-right-to-left">
                  <div className="w-3 h-3 bg-white/35 rounded-full absolute -top-1 left-1"></div>
                  <div className="w-5 h-4 bg-white/25 rounded-full absolute -top-0.5 left-1.5"></div>
                  <div className="w-3 h-3 bg-white/35 rounded-full absolute -top-1 right-1"></div>
                </div>
              </div>
              
              <div className="relative flex items-center justify-between mb-2">
                <div className="flex items-center gap-3">
                  <div className="w-12 h-12 bg-gradient-to-br from-yellow-300 to-orange-400 rounded-full flex items-center justify-center border-3 border-white shadow-lg">
                    <Sun className="w-6 h-6 text-white" />
                  </div>
                  <div>
                    <h1 className="text-xl font-semibold text-slate-800 drop-shadow-sm">LightAlarm</h1>
                    <p className="text-xs text-slate-700">Wake Up Gently</p>
                  </div>
                </div>
                <div className="flex gap-2">
                  <button
                    onClick={() => setShowSettings(true)}
                    className="w-10 h-10 bg-white/30 backdrop-blur-md rounded-full flex items-center justify-center border border-white/40 hover:bg-white/40 transition-all"
                  >
                    <Settings className="w-4 h-4 text-slate-800" />
                  </button>
                </div>
              </div>

              {/* Date Display */}
              <div className="relative text-xs text-slate-700 flex items-center justify-between">
                <span>{currentTime.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' })}</span>
                <span>{currentTime.toLocaleTimeString('en-US', { hour12: true, hour: 'numeric', minute: '2-digit' })}</span>
              </div>
            </div>
          ) : (
            <div className="relative mb-8 overflow-hidden rounded-3xl p-5 border-2 border-white/70 backdrop-blur-sm shadow-lg" style={{ background: '#121212' }}>
              {/* Twinkling Stars Animation */}
              <div className="absolute inset-0 overflow-hidden">
                <div className="absolute top-3 right-8 w-1 h-1 bg-white/80 rounded-full animate-twinkle-1"></div>
                <div className="absolute top-5 right-12 w-1 h-1 bg-white/60 rounded-full animate-twinkle-2"></div>
                <div className="absolute top-2 right-16 w-1 h-1 bg-white/70 rounded-full animate-twinkle-3"></div>
                <div className="absolute top-6 right-6 w-1 h-1 bg-white/50 rounded-full animate-twinkle-4"></div>
                <div className="absolute top-4 right-20 w-1 h-1 bg-white/90 rounded-full animate-twinkle-5"></div>
              </div>
              
              <div className="relative flex items-center justify-between mb-2">
                <div className="flex items-center gap-3">
                  <div className="w-12 h-12 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-full flex items-center justify-center border-3 border-white shadow-lg">
                    <Moon className="w-6 h-6 text-white" />
                </div>
                <div>
                    <h1 className="text-xl font-semibold text-gray-800 drop-shadow-sm">LightAlarm</h1>
                    <p className="text-xs text-gray-700">Sleep Peacefully</p>
                </div>
              </div>
                <div className="flex gap-2">
                <button
                  onClick={() => setShowComfortLightSettings(true)}
                    className={`w-10 h-10 bg-white/30 backdrop-blur-md rounded-full flex items-center justify-center border border-white/40 hover:bg-white/40 transition-all ${
                      isComfortLightEnabled ? 'bg-amber-500/40' : ''
                  }`}
                  title="Comfort Light"
                >
                    <Lightbulb className="w-4 h-4 text-white" />
                </button>
                <button
                  onClick={() => setShowSettings(true)}
                    className="w-10 h-10 bg-white/30 backdrop-blur-md rounded-full flex items-center justify-center border border-white/40 hover:bg-white/40 transition-all"
                    title="Settings"
                >
                    <Settings className="w-4 h-4 text-white" />
                </button>
                </div>
              </div>

              {/* Date Display */}
              <div className="relative text-xs text-gray-700 flex items-center justify-between">
                <span>{currentTime.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' })}</span>
                <span>{currentTime.toLocaleTimeString('en-US', { hour12: true, hour: 'numeric', minute: '2-digit' })}</span>
              </div>
            </div>
          )}

          {/* Mode Toggle - Sky Cloud Design */}
          <div className="grid grid-cols-2 gap-4 mb-6">
            {/* Wake Mode - Sunrise colors */}
            <div className="relative group" onClick={() => setCurrentMode('wake')}>
              <div className={`absolute inset-0 rounded-[2rem] blur-sm opacity-50 ${currentMode === 'wake' ? 'bg-gradient-to-b from-orange-300 to-amber-200' : 'bg-transparent'}`}></div>
              <div className={`relative bg-gradient-to-b from-orange-300 via-amber-200 to-yellow-200 rounded-[2rem] p-6 border-2 backdrop-blur-xl overflow-hidden shadow-md cursor-pointer transition-all ${currentMode === 'wake' ? 'border-white/60 scale-105' : 'border-white/30 opacity-70'}`}>
                <div className="absolute top-2 right-4 w-10 h-6 bg-white/40 backdrop-blur-sm rounded-full"></div>
                <div className="absolute top-3 right-2 w-8 h-5 bg-white/50 backdrop-blur-sm rounded-full"></div>
                <div className="relative z-10">
                  <Sunrise className="w-8 h-8 text-white mb-2 drop-shadow-md" />
                  <span className="font-semibold text-white drop-shadow-sm">Wake</span>
                </div>
              </div>
            </div>

            {/* Sleep Mode - Night sky */}
            <div className="relative" onClick={() => setCurrentMode('sleep')}>
              <div className={`bg-gradient-to-b from-indigo-400 to-blue-500 rounded-[2rem] p-6 border-2 backdrop-blur-xl overflow-hidden cursor-pointer transition-all ${currentMode === 'sleep' ? 'border-white/60 scale-105' : 'border-white/30 opacity-70'}`}>
                <div className="absolute top-2 right-4 w-10 h-6 bg-white/40 backdrop-blur-sm rounded-full"></div>
                <div className="absolute top-3 right-2 w-8 h-5 bg-white/50 backdrop-blur-sm rounded-full"></div>
                <Moon className="w-8 h-8 text-white mb-2 relative z-10" />
                <span className="font-semibold text-white relative z-10">Sleep</span>
              </div>
            </div>
          </div>

          {/* Sleep Mode Settings */}
          {currentMode === 'sleep' && (
            <div className="mb-6 space-y-4">
              {/* Candle Light + Dynamic Brightness Combined */}
              <div className="p-3 bg-white/10 rounded-lg border border-white/30">
                {/* Candle Light Toggle */}
                <div className="flex items-center justify-between mb-2">
                  <div className="flex items-center space-x-2">
                    <span className="text-lg">🕯️</span>
                    <div>
                      <span className="text-xs font-medium">Candle Light</span>
                      <div className="text-xs opacity-60">Sleep-friendly comfort</div>
                    </div>
                  </div>
                  <button
                    onClick={() => setIsComfortLightEnabled(!isComfortLightEnabled)}
                    className={`w-10 h-5 rounded-full transition-all ${
                      isComfortLightEnabled ? 'bg-red-500' : 'bg-white/20'
                    }`}
                  >
                    <div className={`w-4 h-4 bg-white rounded-full transition-transform ${
                      isComfortLightEnabled ? 'translate-x-5' : 'translate-x-0.5'
                    }`} />
                  </button>
                </div>

                {/* Dynamic Brightness Toggle */}
                <div className="flex items-center justify-between pt-2 border-t border-white/10">
                  <div className="flex items-center space-x-2">
                    <span className="text-lg">✨</span>
                    <div>
                      <span className="text-xs font-medium">Dynamic Brightness</span>
                      <div className="text-xs opacity-60">Animated variations</div>
                    </div>
                  </div>
                  <button
                    onClick={() => setIsDynamicBrightness(!isDynamicBrightness)}
                    className={`w-10 h-5 rounded-full transition-all ${
                      isDynamicBrightness ? 'bg-purple-500' : 'bg-white/20'
                    }`}
                  >
                    <div className={`w-4 h-4 bg-white rounded-full transition-transform ${
                      isDynamicBrightness ? 'translate-x-5' : 'translate-x-0.5'
                    }`} />
                  </button>
                </div>
                {isDynamicBrightness && (
                  <div className="text-xs opacity-60 mt-1 bg-yellow-500/10 border border-yellow-500/30 rounded px-2 py-0.5">
                    ⚠️ Uses more battery
                  </div>
                )}

                {isComfortLightEnabled && (
                  <div className="space-y-3 pt-2 border-t border-white/10">
                    {/* Quick Settings */}
                    <div className="flex items-center justify-between">
                      <span className="text-xs opacity-70">Quick Settings:</span>
                      <button
                        onClick={() => setShowComfortLightSettings(!showComfortLightSettings)}
                        className="text-xs bg-white/10 px-3 py-1 rounded-full hover:bg-white/20 transition-all"
                      >
                        {showComfortLightSettings ? 'Hide' : 'Customize'}
                      </button>
                    </div>

                    {/* Current Settings Display */}
                    <div className="flex items-center justify-between text-sm">
                      <div className="flex items-center space-x-2">
                        <div
                          className="w-4 h-4 rounded-full"
                          style={{ background: comfortLightColors[comfortLightColor].gradient }}
                        />
                        <span>{comfortLightColors[comfortLightColor].name}</span>
                      </div>
                      <span className="text-xs opacity-70">
                        {comfortLightColors[comfortLightColor].scientific}
                      </span>
                    </div>

                    {showComfortLightSettings && (
                      <div className="space-y-4 pt-3 border-t border-white/10">
                        {/* Brightness Slider */}
                        <div>
                          <div className="flex items-center justify-between mb-2">
                            <span className="text-xs font-medium">Brightness</span>
                            <span className="text-xs opacity-70">{Math.round(comfortLightBrightness * 100)}%</span>
                          </div>

                          {/* Live Brightness Preview */}
                          <div className="mb-3 p-4 bg-white/5 rounded-lg border">
                            <div className="text-xs opacity-70 mb-2 text-center">Live Preview</div>
                            <div
                              className="w-full h-8 rounded transition-all duration-300"
                              style={{
                                background: comfortLightColors[comfortLightColor].gradient,
                                opacity: comfortLightBrightness * 3,
                                filter: `brightness(${comfortLightBrightness * 5})`,
                                boxShadow: `0 0 ${comfortLightBrightness * 100}px ${comfortLightColors[comfortLightColor].color}40`
                              }}
                            />
                            <div className="text-xs opacity-50 text-center mt-1">
                              {comfortLightBrightness < 0.05 ? '😴 Perfect for sleep' :
                               comfortLightBrightness < 0.1 ? '🌙 Good for nighttime' :
                               '⚠️ May be too bright for sleep'}
                            </div>
                          </div>

                          <input
                            type="range"
                            min="0.01"
                            max="0.2"
                            step="0.01"
                            value={comfortLightBrightness}
                            onChange={(e) => setComfortLightBrightness(parseFloat(e.target.value))}
                            className="w-full h-2 bg-white/10 rounded-lg appearance-none cursor-pointer"
                            style={{
                              WebkitAppearance: 'none',
                              background: `linear-gradient(to right, #ef4444 0%, #ef4444 ${(comfortLightBrightness - 0.01) / (0.2 - 0.01) * 100}%, rgba(255,255,255,0.1) ${(comfortLightBrightness - 0.01) / (0.2 - 0.01) * 100}%, rgba(255,255,255,0.1) 100%)`
                            }}
                          />
                          <div className="text-xs opacity-60 mt-1">Extremely dim recommended for sleep</div>
                        </div>

                        {/* Color Selection */}
                        <div>
                          <span className="text-xs font-medium block mb-2">Sleep-Safe Colors</span>
                          <div className="grid grid-cols-1 gap-2">
                            {Object.entries(comfortLightColors).map(([key, color]) => (
                              <button
                                key={key}
                                onClick={() => setComfortLightColor(key)}
                                className={`flex items-center space-x-3 p-2 rounded transition-all ${
                                  comfortLightColor === key
                                    ? 'bg-white/20 border border-white/30'
                                    : 'bg-white/5 hover:bg-white/10'
                                }`}
                              >
                                <div
                                  className="w-6 h-6 rounded-full flex-shrink-0"
                                  style={{ background: color.gradient }}
                                />
                                <div className="text-left flex-1">
                                  <div className="text-xs font-medium flex items-center space-x-2">
                                    <span>{color.name}</span>
                                    {color.recommended && (
                                      <span className="text-xs bg-green-500/20 text-green-300 px-2 py-0.5 rounded-full">
                                        Recommended
                                      </span>
                                    )}
                                  </div>
                                  <div className="text-xs opacity-60">{color.description}</div>
                                </div>
                              </button>
                            ))}
                          </div>
                        </div>
                      </div>
                    )}
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Sleep Timer Settings */}
          {currentMode === 'sleep' && !isComfortLightEnabled && (
            <div className="p-3 bg-white/10 rounded-lg border border-white/30">
              <span className="text-xs mb-2 block text-gray-800 font-medium">Sleep Timer</span>
              <div className="grid grid-cols-2 gap-1.5">
                <button
                  onClick={() => {
                    setSleepTimer(30);
                    setIsCustomSleepTimer(false);
                  }}
                  className={`py-2 rounded-lg transition-all ${
                    sleepTimer === 30 && !isCustomSleepTimer
                      ? 'bg-blue-500/80 shadow-lg'
                      : 'bg-white/10 hover:bg-white/20'
                  }`}
                >
                  <span className="text-xs font-medium text-gray-800">30 min</span>
                </button>
                <button
                  onClick={() => {
                    setSleepTimer(60);
                    setIsCustomSleepTimer(false);
                  }}
                  className={`py-2 rounded-lg transition-all ${
                    sleepTimer === 60 && !isCustomSleepTimer
                      ? 'bg-blue-500/80 shadow-lg'
                      : 'bg-white/10 hover:bg-white/20'
                  }`}
                >
                  <span className="text-xs font-medium text-gray-800">1 hour</span>
                </button>
                <button
                  onClick={() => {
                    setSleepTimer(120);
                    setIsCustomSleepTimer(false);
                  }}
                  className={`py-2 rounded-lg transition-all ${
                    sleepTimer === 120 && !isCustomSleepTimer
                      ? 'bg-blue-500/80 shadow-lg'
                      : 'bg-white/10 hover:bg-white/20'
                  }`}
                >
                  <span className="text-xs font-medium text-gray-800">2 hours</span>
                </button>
                <button
                  onClick={() => {
                    setIsCustomSleepTimer(true);
                    setSleepTimer(customSleepTimerValue);
                  }}
                  className={`py-2 rounded-lg transition-all ${
                    isCustomSleepTimer
                      ? 'bg-blue-500/80 shadow-lg'
                      : 'bg-white/10 hover:bg-white/20'
                  }`}
                >
                  <span className="text-xs font-medium text-gray-800">Custom</span>
                </button>
              </div>
              
              {/* Custom Sleep Timer Slider */}
              {isCustomSleepTimer && (
                <div className="mt-4 p-3 bg-white/5 rounded-lg">
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-xs text-gray-700">Custom Duration</span>
                    <span className="text-xs text-gray-800 font-medium">
                      {Math.floor(customSleepTimerValue / 60) > 0 
                        ? `${Math.floor(customSleepTimerValue / 60)} hour${Math.floor(customSleepTimerValue / 60) !== 1 ? 's' : ''} ${customSleepTimerValue % 60} min${customSleepTimerValue % 60 !== 1 ? 's' : ''}`
                        : `${customSleepTimerValue} min${customSleepTimerValue !== 1 ? 's' : ''}`
                      }
                    </span>
                  </div>
                  <input
                    type="range"
                    min="0"
                    max="180"
                    value={customSleepTimerValue}
                    onChange={(e) => {
                      const value = parseInt(e.target.value);
                      setCustomSleepTimerValue(value);
                      setSleepTimer(value);
                    }}
                    className="w-full h-2 bg-white/20 rounded-lg appearance-none cursor-pointer slider"
                    style={{
                      background: `linear-gradient(to right, #3b82f6 0%, #3b82f6 ${(customSleepTimerValue / 180) * 100}%, rgba(255,255,255,0.2) ${(customSleepTimerValue / 180) * 100}%, rgba(255,255,255,0.2) 100%)`
                    }}
                  />
                </div>
              )}
            </div>
          )}

          {/* Sleep Theme Selector */}
          {currentMode === 'sleep' && !isComfortLightEnabled && (
            <div className="p-3 bg-white/10 rounded-lg border border-white/30 mt-2">
              <h3 className="text-xs font-medium text-gray-800 mb-2">Sleep Theme</h3>
              <div className="grid grid-cols-4 gap-1.5">
                {Object.entries(getCurrentThemes()).map(([key, theme]) => (
                  <button
                    key={key}
                    onClick={() => setCurrentTheme(key)}
                    className={`p-1.5 rounded-lg text-xs transition-all relative overflow-hidden ${
                      currentTheme === key
                        ? 'bg-white/20 shadow-lg ring-1 ring-white/30'
                        : 'bg-white/10 hover:bg-white/15'
                    }`}
                  >
                    <div
                      className="w-full h-1 rounded-full mb-1 opacity-80"
                      style={{
                        background: `linear-gradient(90deg, ${theme.colors.slice(0, 3).join(', ')})`
                      }}
                    />
                    <div className="text-center">
                      <span className="text-xs font-medium block leading-tight text-gray-800">{theme.name}</span>
                    </div>
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* Wake Mode Alarm Settings */}
          {currentMode === 'wake' && (
            <div className="mb-6 space-y-4">
              {/* Alarm List */}
              <div className="space-y-3">
                {alarms.map((alarm) => (
                    <div key={alarm.id} className="relative backdrop-blur-xl rounded-2xl p-3 border shadow-lg" style={{ backgroundColor: 'rgba(255, 248, 225, 0.8)', borderColor: '#ffa500' }}>
                      <div className="flex items-center justify-between">
                        <div className="flex items-center space-x-4">
                          {/* Clickable Time Display with -20m indicator */}
                          <div
                            onClick={() => {
                              console.log('🔍 EDIT ALARM DEBUG - Alarm object being edited:', alarm);
                              console.log('🔍 EDIT ALARM DEBUG - Alarm time string:', alarm.time);
                              console.log('🔍 EDIT ALARM DEBUG - Alarm AM/PM:', alarm.amPm);

                              // Pre-fill the Add Alarm modal with current alarm data
                              // Parse time and AM/PM from alarm.time and alarm.amPm
                              const [hour, minute] = alarm.time.split(':').map(Number);
                              console.log('🔍 EDIT ALARM DEBUG - Parsed time components:', { hour, minute, amPm: alarm.amPm });

                              setNewAlarmHour(hour);
                              setNewAlarmMinute(minute);
                              setNewAlarmAmPm(alarm.amPm);
                              console.log('🔍 EDIT ALARM DEBUG - Form state set to:', {
                                newAlarmHour: hour,
                                newAlarmMinute: minute,
                                newAlarmAmPm: alarm.amPm
                              });

                              setNewAlarmRepeat(alarm.repeat);
                              setNewAlarmSound(alarm.sound.displayName);
                              setSelectedSoundResourceId(alarm.sound.resourceName);
                              setNewAlarmTheme(alarm.theme);
                              setEditingAlarm(alarm); // Keep track of which alarm we're editing
                              setShowAddAlarm(true);
                            }}
                            className="flex flex-col items-center cursor-pointer transition-colors"
                          >
                            <div className="text-xs text-red-500 font-medium mb-0.5">-20m</div>
                            <div
                              className="text-xl font-bold"
                              style={{ color: '#422d22' }}
                            >
                              {alarm.time} {alarm.amPm}
                        </div>
                      </div>
                          {/* Stacked Alarm Details */}
                          <div className="flex flex-col">
                            <div className="text-sm" style={{ color: '#422d22' }}>
                              {alarm.repeat}
                                </div>
                            <div className="text-xs" style={{ color: '#422d22' }}>
                              {alarm.theme}
                                </div>
                              </div>
                                </div>
                        <div className="flex items-center space-x-3">
                          <button
                          onClick={() => {
                            setAlarms(alarms.map(a => 
                              a.id === alarm.id ? { ...a, isEnabled: !a.isEnabled } : a
                            ));
                            }}
                            className={`w-12 h-6 rounded-full transition-colors ${
                              alarm.isEnabled ? 'bg-blue-500' : 'bg-gray-300'
                            }`}
                          >
                            <div className={`w-5 h-5 bg-white rounded-full shadow-md transition-transform ${
                              alarm.isEnabled ? 'translate-x-6' : 'translate-x-0.5'
                            }`} />
                          </button>
                        {/* Add Alarm Icon Button */}
                  <button
                    onClick={() => {
                      console.log('Add Alarm button clicked'); // Debug log
                      // Initialize with current time + 1 hour
                      const now = new Date();
                      const oneHourLater = new Date(now.getTime() + 60 * 60 * 1000);
                      let hour = oneHourLater.getHours();
                      const minute = oneHourLater.getMinutes();
                      const amPm = hour >= 12 ? 'PM' : 'AM';
                      if (hour > 12) hour -= 12;
                      if (hour === 0) hour = 12;

                      setNewAlarmHour(hour);
                      setNewAlarmMinute(minute);
                      setNewAlarmAmPm(amPm);
                      setShowAddAlarm(true);
                    }}
                          className="w-8 h-8 bg-blue-500 hover:bg-blue-600 text-white rounded-full shadow-md hover:shadow-lg transition-all duration-200 flex items-center justify-center"
                    title="Add Alarm"
                  >
                          <span className="text-lg font-light">+</span>
                        </button>
                        {/* Delete Alarm Icon Button */}
                        <button
                          onClick={async () => {
                            try {
                              console.log('🗑️ Delete alarm clicked:', alarm.id);
                              
                              // Call native AlarmService to cancel the alarm
                              await AlarmService.cancelAlarm({ id: alarm.id.toString() });
                              console.log('✅ Native alarm cancelled successfully');
                              
                              // Update React state
                              setAlarms(alarms.filter(a => a.id !== alarm.id));
                              console.log('🔄 React state updated - alarm removed');
                              
                            } catch (error) {
                              console.error('❌ Failed to cancel native alarm:', error);
                              alert('Failed to cancel alarm: ' + error.message);
                            }
                          }}
                          className="w-8 h-8 bg-red-500 hover:bg-red-600 text-white rounded-full shadow-md hover:shadow-lg transition-all duration-200 flex items-center justify-center"
                          title="Delete Alarm"
                        >
                          <Trash2 size={16} />
                  </button>
                </div>
                    </div>
                              </div>
                ))}
              </div>

              {/* Time Until Next Alarm Display */}
              {alarms.length > 0 && (
                <div className="mt-4 p-3 bg-gradient-to-r from-blue-50 to-indigo-50 rounded-lg border border-blue-200">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm font-medium text-blue-800">Next Alarm</p>
                      <p className="text-lg font-bold text-blue-900">
                        {alarmTimeUntil.hours > 0
                          ? `${alarmTimeUntil.hours}h ${alarmTimeUntil.minutes}m`
                          : `${alarmTimeUntil.minutes}m`
                        }
                      </p>
                    </div>
                    <div className="text-right">
                      <p className="text-xs text-blue-600">Until wake up</p>
                      <div className="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center mt-1">
                        <Clock size={16} className="text-white" />
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {/* Legacy time-picker card removed in Web Sandbox Mode */}

              {/* 2. Sky Theme Selection */}
              <div className="relative">
                <div className="absolute inset-0 bg-gradient-to-br from-blue-200 to-orange-200 rounded-[2.5rem] blur-md opacity-20"></div>
                <div className="relative bg-white/70 backdrop-blur-2xl rounded-[2.5rem] p-6 border-2 border-blue-200/60 shadow-lg">
                  <div className="text-blue-900 font-semibold mb-4 flex items-center gap-2">
                    <Cloud className="w-5 h-5 text-blue-600" />
                    Wake Up Theme
                  </div>
                  <div className="grid grid-cols-4 gap-2">
                    {Object.entries(getCurrentThemes()).map(([key, theme]) => (
                      <button
                        key={key}
                        onClick={() => setCurrentTheme(key)}
                        className={`relative overflow-hidden bg-gradient-to-b rounded-2xl p-3 border-2 backdrop-blur-sm shadow-sm transition-all ${
                          currentTheme === key
                            ? 'border-orange-400 ring-2 ring-blue-300 ring-offset-2 scale-105'
                            : 'border-white/60'
                        }`}
                        style={{
                          background: `linear-gradient(to bottom, ${theme.colors.slice(-3).join(', ')})`
                        }}
                      >
                        {/* Clouds in theme preview */}
                        <div className="absolute top-1 right-2 w-4 h-3 bg-white/60 backdrop-blur-sm rounded-full"></div>
                        <div className="absolute top-2 right-1 w-3 h-2 bg-white/70 backdrop-blur-sm rounded-full"></div>

                        <div className="relative z-10 h-12 flex items-center justify-center">
                          <span className="text-[10px] text-center font-semibold text-slate-800 drop-shadow leading-tight">
                            {theme.name}
                          </span>
                        </div>
                      </button>
                    ))}
                  </div>
                </div>
              </div>

              {/* 3. Repeat Days - Sky Cloud Design */}
              {isAlarmEnabled && (
                <div className="relative">
                  <div className="absolute inset-0 bg-gradient-to-br from-blue-200 to-orange-200 rounded-[2.5rem] blur-md opacity-20"></div>
                  <div className="relative bg-white/70 backdrop-blur-2xl rounded-[2.5rem] p-6 border-2 border-blue-200/60 shadow-lg">
                    <div className="text-blue-900 font-semibold mb-4">Repeat Days</div>
                    <div className="flex gap-2">
                      {['Mon','Tue','Wed','Thu','Fri','Sat','Sun'].map((day, i) => (
                        <button
                          key={i}
                          onClick={() => {
                            setRepeatDays(prev =>
                              prev.includes(day)
                                ? prev.filter(d => d !== day)
                                : [...prev, day]
                            );
                          }}
                          className={`flex-1 py-3 rounded-2xl text-sm font-semibold border-2 transition-all ${
                            repeatDays.includes(day)
                              ? 'bg-gradient-to-br from-orange-400 to-amber-400 text-white border-white/60 shadow-md'
                              : 'bg-blue-50/50 text-blue-400 border-blue-200/60 backdrop-blur-sm'
                          }`}
                        >
                          {day.slice(0,2)}
                        </button>
                      ))}
                    </div>
                  </div>
                </div>
              )}

              {/* 4. Save Button - Sky Cloud Design */}
              {isAlarmEnabled && (
                <div className="relative">
                  <div className="absolute inset-0 bg-gradient-to-r from-emerald-400 to-green-400 rounded-3xl blur-lg opacity-40"></div>
                  <button
                    onClick={async () => {
                      try {
                        const alarmId = editingAlarm ? editingAlarm.id : Date.now();
                        const formattedTime = `${String(newAlarmHour).padStart(2, '0')}:${String(newAlarmMinute).padStart(2, '0')}`;
                        
                        console.log('🔔 NATIVE ALARM SCHEDULING:');
                        console.log('  🆔 Alarm ID:', alarmId);
                        console.log('  🕐 Time:', formattedTime, newAlarmAmPm);
                        console.log('  🎵 Sound:', newAlarmSound);
                        console.log('  🎨 Theme:', currentTheme);
                        console.log('  🔄 Repeat Days:', repeatDays);

                        // CRITICAL: Check if AlarmService plugin is available
                        if (!Capacitor.Plugins.AlarmService) {
                          console.error('❌ AlarmService plugin not available - cannot schedule alarm');
                          alert('AlarmService not available. Please restart the app.');
                          return;
                        }

                        // Call native AlarmService plugin
                        const result = await AlarmService.scheduleAlarm({
                          id: alarmId.toString(),
                          hour: newAlarmHour,
                          minute: newAlarmMinute,
                          amPm: newAlarmAmPm,
                          soundResourceName: newAlarmSound.resourceName || 'classicalarm_digital_alarm',
                          theme: currentTheme,
                          repeatDays: repeatDays
                        });

                        console.log('✅ Native alarm scheduled successfully:', result);

                        // Update React state for UI display
                        const newAlarm = {
                          id: alarmId,
                          time: formattedTime,
                          amPm: newAlarmAmPm,
                          sound: newAlarmSound,
                          theme: currentTheme,
                          enabled: true
                        };

                        if (editingAlarm) {
                          setAlarms(alarms.map(a => a.id === editingAlarm.id ? newAlarm : a));
                        } else {
                          setAlarms([...alarms, newAlarm]);
                        }

                        // Calculate time until for UI display
                        const now = new Date();
                        let targetHour = newAlarmHour;
                        if (newAlarmAmPm === 'PM' && newAlarmHour !== 12) {
                          targetHour += 12;
                        } else if (newAlarmAmPm === 'AM' && newAlarmHour === 12) {
                          targetHour = 0;
                        }

                        const alarmDate = new Date();
                        alarmDate.setHours(targetHour, newAlarmMinute, 0, 0);
                        if (alarmDate <= now) {
                          alarmDate.setDate(alarmDate.getDate() + 1);
                        }

                        const diffMs = alarmDate - now;
                        const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
                        // Fix: Use Math.ceil for minutes to show "1 min" for any duration > 0 seconds
                        const diffMinutes = Math.ceil((diffMs % (1000 * 60 * 60)) / (1000 * 60));

                        setAlarmTimeUntil({ hours: diffHours, minutes: diffMinutes });
                        setSavedThemeName(getCurrentThemes()[currentTheme]?.name || 'Unknown');
                        setShowSaveConfirm(true);
                        setTimeout(() => setShowSaveConfirm(false), 3000);

                        // Close modal
                        setShowAddAlarm(false);
                        setEditingAlarm(null);

                      } catch (error) {
                        console.error('❌ Failed to schedule native alarm:', error);
                        alert('Failed to schedule alarm: ' + error.message);
                      }
                    }}
                    className="relative w-full bg-gradient-to-r from-emerald-500 to-green-500 text-white py-5 rounded-3xl font-semibold shadow-xl border-2 border-white/60 backdrop-blur-sm hover:shadow-2xl transition-all"
                  >
                    Save Alarm
                  </button>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Center Content */}
        <div className="flex-1 flex flex-col items-center justify-center px-6 pl-4">
          {isRunning && currentMode === 'wake' && progress >= 100 && (
            <div className="text-center mb-4">
              <p className="text-sm opacity-80 animate-pulse">
                🌅 Alarm Complete - Tap screen to stop
              </p>
            </div>
          )}

          {/* Progress Circle */}
          <div className="relative mb-8">
            <div className={`w-32 h-32 rounded-full border-4 ${currentMode === 'wake' ? 'border-gray-200' : 'border-white/20'}`}>
              <div
                className={`w-full h-full rounded-full border-4 transition-all duration-300 ${currentMode === 'wake' ? 'border-gray-900' : 'border-white'}`}
                style={{
                  clipPath: `polygon(50% 50%, 50% 0%, ${
                    progress <= 50
                      ? `${50 + progress}% 0%`
                      : progress <= 75
                      ? `100% 0%, 100% ${(progress - 50) * 2}%`
                      : `100% 100%, ${100 - (progress - 75) * 2}% 100%`
                  }, 50% 50%)`
                }}
              />
            </div>
            <div className="absolute inset-0 flex items-center justify-center">
              <span className={`text-xl font-bold ${currentMode === 'wake' ? 'text-gray-900' : 'text-gray-800'}`}>{Math.round(progress)}%</span>
            </div>
          </div>

          {/* Status Text */}
          <div className="text-center mb-8">
            <h2 className={`text-xl font-semibold mb-2 ${currentMode === 'wake' ? 'text-blue-900' : 'text-gray-800'}`}>
              {currentMode === 'wake' ? 'Wake Up Simulation' : 'Sleep Simulation'}
            </h2>
            <p className={`${currentMode === 'wake' ? 'text-blue-700' : 'text-gray-700'}`}>
              {config.name} • {currentMode === 'sleep' ? `${sleepTimer}min` : `${config.duration}s`} duration
            </p>
            {isRunning && (
              <p className={`text-sm mt-2 ${currentMode === 'wake' ? 'text-blue-600' : 'text-gray-600'}`}>
                Brightness: {Math.round(brightness * 100)}%
              </p>
            )}
          </div>

          {/* Controls */}
          <div className="flex space-x-4">
            {!isRunning ? (
              <button
                onClick={currentMode === 'wake' ? startColorPreview : startPreview}
                className={`flex flex-col items-center space-y-1 px-6 py-3 rounded-xl transition-all shadow-lg ${
                  currentMode === 'wake'
                    ? 'bg-gradient-to-r from-blue-400 via-cyan-400 to-orange-400 text-white border-2 border-white/60 backdrop-blur-sm hover:shadow-xl'
                    : 'bg-white/20 hover:bg-white/30 text-white'
                }`}
              >
                <div className="flex items-center space-x-2">
                <Play className="w-5 h-5" />
                  <span className="font-semibold">Start {currentMode === 'wake' ? 'Wake Up' : 'the light'}</span>
                </div>
                {currentMode === 'wake' && (
                  <span className="text-xs opacity-75">(Click to see how it looks like)</span>
                )}
              </button>
            ) : (
              <button
                onClick={stopSimulationWithConfirm}
                className={`flex items-center space-x-2 px-6 py-3 rounded-xl transition-all shadow-lg ${
                  currentMode === 'wake'
                    ? 'bg-blue-500 text-white hover:bg-blue-600'
                    : 'bg-white/20 hover:bg-white/30 text-white'
                }`}
              >
                <Pause className="w-5 h-5" />
                <span className="font-semibold">Pause</span>
              </button>
            )}

            {currentMode === 'wake' && (
            <button
              onClick={resetSimulation}
                className="flex items-center space-x-2 px-4 py-3 rounded-xl transition-all bg-white/50 hover:bg-white/70 text-blue-900"
            >
              <RotateCcw className="w-5 h-5" />
            </button>
            )}
          </div>
        </div>

        {/* Bottom Info */}
        <div className={`p-6 text-center ${currentMode === 'wake' ? 'text-slate-700' : 'text-gray-600'}`}>
          <p className="text-sm font-medium">
            {currentMode === 'wake'
              ? 'Gradually brightens to wake you naturally'
              : 'Gradually dims to help you fall asleep'
            }
          </p>
        </div>
      </div>

      {/* Add Alarm Modal */}
      {showAddAlarm && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl p-6 w-full max-w-md max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-2xl font-bold text-slate-800">{editingAlarm ? 'Edit Alarm' : 'Add Alarm'}</h2>
              <button
                onClick={() => {
                  setShowAddAlarm(false);
                  setEditingAlarm(null);
                }}
                className="w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center hover:bg-gray-200 transition-colors"
              >
                ✕
              </button>
            </div>

            <div className="space-y-6">
              {/* Time Picker */}
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-2">Time</label>
                <div className="flex gap-2">
                  <select 
                    value={newAlarmHour} 
                    onChange={(e) => setNewAlarmHour(Number(e.target.value))}
                    className="flex-1 px-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  >
                    {[...Array(12)].map((_, i) => (
                      <option key={i + 1} value={i + 1}>{i + 1}</option>
                    ))}
                  </select>
                  <select 
                    value={newAlarmMinute} 
                    onChange={(e) => setNewAlarmMinute(Number(e.target.value))}
                    className="flex-1 px-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  >
                    {[...Array(60)].map((_, i) => (
                      <option key={i} value={i}>{String(i).padStart(2, '0')}</option>
                    ))}
                  </select>
                  <select 
                    value={newAlarmAmPm} 
                    onChange={(e) => setNewAlarmAmPm(e.target.value)}
                    className="flex-1 px-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  >
                    <option value="AM">AM</option>
                    <option value="PM">PM</option>
                  </select>
                </div>
              </div>

              {/* Repeat Dropdown */}
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-2">Repeat</label>
                <select 
                  value={newAlarmRepeat}
                  onChange={(e) => setNewAlarmRepeat(e.target.value)}
                  className="w-full px-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="Daily">Daily</option>
                  <option value="Weekends">Weekends</option>
                  <option value="Certain Weekdays">Certain Weekdays</option>
                </select>
                
                {/* Conditional Day Selection for Certain Weekdays */}
                {newAlarmRepeat === 'Certain Weekdays' && (
                  <div className="mt-3">
                    <label className="block text-sm font-medium text-slate-700 mb-2">Select Days</label>
                    <div className="flex gap-2 flex-wrap">
                      {['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'].map((day) => (
                        <button
                          key={day}
                          onClick={() => {
                            setNewAlarmSelectedDays(prev =>
                              prev.includes(day)
                                ? prev.filter(d => d !== day)
                                : [...prev, day]
                            );
                          }}
                          className={`px-3 py-2 rounded-lg text-sm font-medium border-2 transition-colors ${
                            newAlarmSelectedDays.includes(day)
                              ? 'bg-blue-500 text-white border-blue-500'
                              : 'bg-gray-50 text-gray-700 border-gray-300 hover:bg-gray-100'
                          }`}
                        >
                          {day}
                        </button>
                      ))}
                    </div>
                  </div>
                )}
              </div>

              {/* Sound Selection */}
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-2">Sound</label>
                <button
                  onClick={openNativeSoundSelection}
                  className="w-full px-4 py-3 border border-gray-300 rounded-xl text-left hover:bg-gray-50 transition-colors"
                >
                  {newAlarmSound} →
                </button>
              </div>

              {/* Theme Selection */}
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-2">Theme</label>
                <select
                  value={newAlarmTheme}
                  onChange={(e) => setNewAlarmTheme(e.target.value)}
                  className="w-full px-4 py-3 border border-gray-300 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="Sunrise">Sunrise</option>
                  <option value="Aurora">Aurora</option>
                  <option value="Green Grass">Green Grass</option>
                  <option value="Blue Sea">Blue Sea</option>
                  <option value="Sephora Blue">Sephora Blue</option>
                  <option value="Pink Ocean">Pink Ocean</option>
                  <option value="Lavender">Lavender</option>
                  <option value="All Night">All Night</option>
                </select>
              </div>

              {/* Save/Cancel Buttons */}
              <div className="flex space-x-3 pt-4">
                <button
                  onClick={() => {
                    setShowAddAlarm(false);
                    setEditingAlarm(null);
                  }}
                  className="flex-1 px-4 py-3 border border-gray-300 rounded-xl text-gray-700 hover:bg-gray-50 transition-colors"
                >
                  Cancel
                </button>
                <button
                  onClick={async () => {
                    try {
                      const alarmId = editingAlarm ? editingAlarm.id : Date.now();
                      const formattedTime = `${String(newAlarmHour).padStart(2, '0')}:${String(newAlarmMinute).padStart(2, '0')}`;
                      
                      console.log('🔔 MODAL NATIVE ALARM SCHEDULING:');
                      console.log('  🆔 Alarm ID:', alarmId);
                      console.log('  🕐 Time:', formattedTime, newAlarmAmPm);
                      console.log('  🎵 Sound:', newAlarmSound);
                      console.log('  🎨 Theme:', newAlarmTheme);
                      console.log('  🔄 Repeat Days:', newAlarmSelectedDays);

                      // CRITICAL: Check if AlarmService plugin is available
                      if (!Capacitor.Plugins.AlarmService) {
                        console.error('❌ AlarmService plugin not available - cannot schedule alarm');
                        alert('AlarmService not available. Please restart the app.');
                        return;
                      }

                      // Call native AlarmService plugin
                      const result = await AlarmService.scheduleAlarm({
                        id: alarmId.toString(),
                        hour: newAlarmHour,
                        minute: newAlarmMinute,
                        amPm: newAlarmAmPm,
                        soundResourceName: selectedSoundResourceId || 'classicalarm_digital_alarm',
                        theme: newAlarmTheme,
                        repeatDays: newAlarmSelectedDays
                      });

                      console.log('✅ Native alarm scheduled successfully:', result);

                      // Update React state for UI display
                      const alarmData = {
                        id: alarmId,
                        time: formattedTime,
                        amPm: newAlarmAmPm,
                        sound: { displayName: newAlarmSound, resourceName: selectedSoundResourceId || 'classicalarm_digital_alarm' },
                        theme: newAlarmTheme,
                        enabled: true
                      };

                      if (editingAlarm) {
                        setAlarms(alarms.map(a => a.id === editingAlarm.id ? alarmData : a));
                        console.log('📅 Alarm updated, useEffect will recalculate alarmTimeUntil');
                      } else {
                        setAlarms([...alarms, alarmData]);
                        console.log('📅 New alarm added, useEffect will recalculate alarmTimeUntil');
                      }

                      setShowAddAlarm(false);
                      setEditingAlarm(null);
                      // Reset form
                      setNewAlarmHour(8);
                      setNewAlarmMinute(0);
                      setNewAlarmAmPm('AM');
                      setNewAlarmRepeat('Daily');
                      setNewAlarmSelectedDays([]);
                      setNewAlarmSound('Classic Alarm');
                      setNewAlarmTheme('Sunrise');

                    } catch (error) {
                      console.error('❌ Failed to schedule native alarm:', error);
                      alert('Failed to schedule alarm: ' + error.message);
                    }
                  }}
                  className="flex-1 px-4 py-3 bg-blue-500 text-white rounded-xl hover:bg-blue-600 transition-colors"
                >
                  {editingAlarm ? 'Update' : 'Save'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Sound Selection Modal */}
      {showSoundSelection && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl p-6 w-full max-w-md max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-2xl font-bold text-slate-800">Sound Selection</h2>
              <button
                onClick={() => setShowSoundSelection(false)}
                className="w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center hover:bg-gray-200 transition-colors"
              >
                ✕
              </button>
            </div>

            <div className="space-y-4">
              {Object.entries(
                ALARM_SOUNDS.reduce((acc, sound) => {
                  if (!acc[sound.category]) acc[sound.category] = [];
                  acc[sound.category].push(sound);
                  return acc;
                }, {})
              ).map(([category, sounds]) => (
                <div key={category}>
                  <h3 className="text-lg font-semibold text-slate-800 mb-3">{category}</h3>
                  <div className="space-y-2">
                    {sounds.map((sound) => (
                      <div key={sound.assetId} className="flex items-center gap-2">
                        <button
                          onClick={() => {
                            setNewAlarmSound(sound.displayName);
                            setSelectedSoundResourceId(sound.assetId);
                            setShowSoundSelection(false);
                          }}
                          className={`flex-1 px-4 py-3 border-2 rounded-xl text-left transition-colors ${
                            newAlarmSound === sound.displayName
                              ? 'border-blue-500 bg-blue-50 text-blue-700'
                              : 'border-gray-300 hover:bg-gray-50'
                          }`}
                        >
                          {sound.displayName}
                        </button>
                        <button
                          onClick={() => handlePlayPreview(sound.assetId)}
                          className="w-10 h-10 rounded-full bg-gray-100 flex items-center justify-center hover:bg-gray-200 transition-colors"
                        >
                          {previewingSound === sound.assetId ? '⏸' : '▶'}
                        </button>
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* Theme Selection Modal */}
      {showThemeSelection && (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-3xl p-6 w-full max-w-md max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-2xl font-bold text-slate-800">Theme Selection</h2>
              <button
                onClick={() => setShowThemeSelection(false)}
                className="w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center hover:bg-gray-200 transition-colors"
              >
                ✕
              </button>
            </div>

            <div className="grid grid-cols-2 gap-3">
              {['Sunrise', 'Aurora', 'Green Grass', 'Blue Sea', 'Sephora Blue', 'Pink Ocean', 'Lavender', 'All Night'].map((theme) => (
                <button
                  key={theme}
                  onClick={() => {
                    setNewAlarmTheme(theme);
                    setShowThemeSelection(false);
                  }}
                  className={`aspect-square rounded-2xl border-2 transition-colors flex items-center justify-center text-sm font-medium ${
                    newAlarmTheme === theme
                      ? 'border-blue-500 ring-2 ring-blue-300 ring-offset-2'
                      : 'border-gray-200 hover:border-blue-500'
                  }`}
                  style={{
                    background: theme === 'Sunrise' ? 'linear-gradient(135deg, #ff6600 0%, #ffcc00 50%, #ff6600 100%)' :
                               theme === 'Aurora' ? 'linear-gradient(135deg, #00ff66 0%, #ff69b4 50%, #0099ff 100%)' :
                               theme === 'Green Grass' ? 'linear-gradient(135deg, #00ff00 0%, #00cc00 100%)' :
                               theme === 'Blue Sea' ? 'linear-gradient(135deg, #00d2ff 0%, #3a7bd5 100%)' :
                               theme === 'Sephora Blue' ? 'linear-gradient(135deg, #00ced1 0%, #008080 100%)' :
                               theme === 'Pink Ocean' ? 'linear-gradient(135deg, #ff1493 0%, #ff69b4 100%)' :
                               theme === 'Lavender' ? 'linear-gradient(135deg, #9370db 0%, #7b68ee 100%)' :
                               'linear-gradient(135deg, #ffd700 0%, #ffa500 100%)'
                  }}
                >
                  {theme}
                </button>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* Save Confirmation Toast */}
      {showSaveConfirm && (
        <div className="fixed bottom-8 left-1/2 transform -translate-x-1/2 z-50 animate-fade-in">
          <div className="bg-black text-white px-6 py-3 rounded-lg shadow-2xl border border-white/20">
            <p className="text-sm font-light">
              Alarm saved! {savedThemeName} theme in {alarmTimeUntil.hours}h {alarmTimeUntil.minutes}m
            </p>
          </div>
        </div>
      )}

    </div>
  );
};

export default LightAlarm;