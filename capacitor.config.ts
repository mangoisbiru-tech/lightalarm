import type { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.lightalarm.app',
  appName: 'LightAlarm',
  webDir: 'build',
  android: {
    allowMixedContent: true,
    captureInput: true,
    webContentsDebuggingEnabled: true
  },
  plugins: {
    SplashScreen: {
      launchShowDuration: 0,
      launchAutoHide: false,
      showSpinner: false,
      splashFullScreen: false,
      splashImmersive: false
    },
    AlarmService: {
      // Custom plugin configuration
    }
  }
};

export default config;
