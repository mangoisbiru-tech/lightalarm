// Service Worker for Light Alarm App
// Handles background alarms when phone is off

const CACHE_NAME = 'light-alarm-v1';
const urlsToCache = [
  '/',
  '/static/js/bundle.js',
  '/static/css/main.css',
  '/manifest.json'
];

// Install event
self.addEventListener('install', (event) => {
  console.log('Service Worker installing...');
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then((cache) => {
        console.log('Service Worker caching files');
        return cache.addAll(urlsToCache);
      })
  );
});

// Activate event
self.addEventListener('activate', (event) => {
  console.log('Service Worker activating...');
  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames.map((cacheName) => {
          if (cacheName !== CACHE_NAME) {
            console.log('Service Worker deleting old cache:', cacheName);
            return caches.delete(cacheName);
          }
        })
      );
    })
  );
});

// Fetch event
self.addEventListener('fetch', (event) => {
  event.respondWith(
    caches.match(event.request)
      .then((response) => {
        // Return cached version or fetch from network
        return response || fetch(event.request);
      })
  );
});

// Background alarm handling
self.addEventListener('message', (event) => {
  if (event.data && event.data.type === 'ALARM_TRIGGER') {
    console.log('Service Worker: Alarm triggered in background');
    
    // Show notification
    self.registration.showNotification('Light Alarm', {
      body: 'Your alarm is going off!',
      icon: '/favicon.ico',
      badge: '/favicon.ico',
      tag: 'light-alarm',
      requireInteraction: true,
      actions: [
        {
          action: 'stop-light',
          title: 'Stop Light'
        },
        {
          action: 'stop-sound',
          title: 'Stop Sound'
        }
      ]
    });
  }
});

// Handle notification clicks
self.addEventListener('notificationclick', (event) => {
  console.log('Service Worker: Notification clicked');
  event.notification.close();
  
  if (event.action === 'stop-light') {
    // Send message to main app to stop light
    self.clients.matchAll().then(clients => {
      clients.forEach(client => {
        client.postMessage({ type: 'STOP_LIGHT' });
      });
    });
  } else if (event.action === 'stop-sound') {
    // Send message to main app to stop sound
    self.clients.matchAll().then(clients => {
      clients.forEach(client => {
        client.postMessage({ type: 'STOP_SOUND' });
      });
    });
  } else {
    // Open the app
    event.waitUntil(
      self.clients.openWindow('/')
    );
  }
});

// Periodic background sync for alarm checking
self.addEventListener('sync', (event) => {
  if (event.tag === 'alarm-check') {
    console.log('Service Worker: Background alarm check');
    event.waitUntil(checkAlarm());
  }
});

// Function to check if alarm should trigger
async function checkAlarm() {
  try {
    // Get alarm time from storage
    const alarmTime = localStorage.getItem('la_alarmTime');
    const alarmAmPm = localStorage.getItem('la_alarmAmPm');
    
    if (!alarmTime || !alarmAmPm) return;
    
    const now = new Date();
    const currentHour = now.getHours();
    const currentMinute = now.getMinutes();
    
    // Parse alarm time
    let targetHour = parseInt(alarmTime.split(':')[0]);
    const alarmMinute = parseInt(alarmTime.split(':')[1]);
    
    if (alarmAmPm === 'PM' && targetHour !== 12) targetHour += 12;
    if (alarmAmPm === 'AM' && targetHour === 12) targetHour = 0;
    
    // Check if it's alarm time
    if (currentHour === targetHour && currentMinute === alarmMinute) {
      console.log('Service Worker: Alarm time reached!');
      
      // Trigger background alarm
      self.registration.showNotification('Light Alarm', {
        body: 'Your alarm is going off!',
        icon: '/favicon.ico',
        requireInteraction: true
      });
    }
  } catch (error) {
    console.error('Service Worker: Error checking alarm:', error);
  }
}

