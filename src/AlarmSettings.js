import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Volume2, Cloud } from 'lucide-react';

const AlarmSettings = ({ alarmTime, setAlarmTime, alarmAmPm, setAlarmAmPm, repeatDays, setRepeatDays, repeatMode, setRepeatMode, selectedAlarmSound, setSelectedAlarmSound, currentTheme, setCurrentTheme, alarmSounds, getCurrentThemes, previewSound, previewingSound }) => {
  const navigate = useNavigate();

  const handleRepeatModeChange = (mode) => {
    setRepeatMode(mode);
    if (mode === 'Daily') {
      setRepeatDays(['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']);
    } else if (mode === 'Weekdays') {
      setRepeatDays(['Mon', 'Tue', 'Wed', 'Thu', 'Fri']);
    } else if (mode === 'Weekend') {
      setRepeatDays(['Sat', 'Sun']);
    }
  };

  const handleDayToggle = (day) => {
    const newRepeatDays = repeatDays.includes(day)
      ? repeatDays.filter(d => d !== day)
      : [...repeatDays, day];

    setRepeatDays(newRepeatDays);

    const isDaily = newRepeatDays.length === 7;
    const isWeekdays = newRepeatDays.length === 5 && ['Mon', 'Tue', 'Wed', 'Thu', 'Fri'].every(d => newRepeatDays.includes(d));
    const isWeekend = newRepeatDays.length === 2 && ['Sat', 'Sun'].every(d => newRepeatDays.includes(d));

    if (isDaily) {
      setRepeatMode('Daily');
    } else if (isWeekdays) {
      setRepeatMode('Weekdays');
    } else if (isWeekend) {
      setRepeatMode('Weekend');
    } else {
      setRepeatMode('Custom');
    }
  };

  return (
    <div className="min-h-screen flex flex-col relative overflow-hidden bg-white">
      <div className="bg-white rounded-3xl p-8 mx-4 max-w-md w-full shadow-2xl border-2 border-blue-200">
        <div className="text-center mb-6">
          <h2 className="text-2xl font-bold text-gray-800 mb-2">Alarm Settings</h2>
        </div>
        {/* Time Setting */}
        <div className="flex items-baseline justify-center gap-4 mb-2">
          <select
            value={alarmTime.split(':')[0]}
            onChange={(e) => {
              const [, minute] = alarmTime.split(':');
              setAlarmTime(`${e.target.value}:${minute}`);
            }}
            className="text-7xl font-light text-blue-900 drop-shadow-sm bg-transparent border-none focus:outline-none focus:ring-2 focus:ring-blue-300 rounded-lg cursor-pointer"
          >
            {Array.from({length: 12}, (_, i) => i + 1).map((h) => (
              <option key={h} value={h.toString().padStart(2, '0')} className="bg-white text-blue-900">
                {h.toString().padStart(2, '0')}
              </option>
            ))}
          </select>
          <div className="text-5xl text-blue-400">:</div>
          <select
            value={alarmTime.split(':')[1]}
            onChange={(e) => {
              const [hour] = alarmTime.split(':');
              setAlarmTime(`${hour}:${e.target.value}`);
            }}
            className="text-7xl font-light text-blue-900 drop-shadow-sm bg-transparent border-none focus:outline-none focus:ring-2 focus:ring-blue-300 rounded-lg cursor-pointer"
          >
            {Array.from({length: 60}, (_, i) => i).map((m) => (
              <option key={m} value={m.toString().padStart(2, '0')} className="bg-white text-blue-900">
                {m.toString().padStart(2, '0')}
              </option>
            ))}
          </select>
          <select
            value={alarmAmPm}
            onChange={(e) => setAlarmAmPm(e.target.value)}
            className="text-3xl text-blue-700 ml-2 bg-transparent border-none focus:outline-none focus:ring-2 focus:ring-blue-300 rounded-lg cursor-pointer"
          >
            <option value="AM" className="bg-white text-blue-900">AM</option>
            <option value="PM" className="bg-white text-blue-900">PM</option>
          </select>
        </div>
        {/* Repeat */}
        <div className="text-blue-900 font-semibold mb-2">Repeat</div>
        <div className="grid grid-cols-3 gap-2 mb-4">
          <button onClick={() => handleRepeatModeChange('Daily')} className={`py-2 rounded-lg text-sm font-semibold border-2 transition-all ${repeatMode === 'Daily' ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-700'}`}>Daily</button>
          <button onClick={() => handleRepeatModeChange('Weekdays')} className={`py-2 rounded-lg text-sm font-semibold border-2 transition-all ${repeatMode === 'Weekdays' ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-700'}`}>Weekdays</button>
          <button onClick={() => handleRepeatModeChange('Weekend')} className={`py-2 rounded-lg text-sm font-semibold border-2 transition-all ${repeatMode === 'Weekend' ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-700'}`}>Weekend</button>
        </div>
        <div className="flex gap-2">
          {['Mon','Tue','Wed','Thu','Fri','Sat','Sun'].map((day) => (
            <button
              key={day}
              onClick={() => handleDayToggle(day)}
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
        {/* Alarm Sound */}
        <div className="relative bg-white/70 backdrop-blur-2xl rounded-[2.5rem] p-6 border-2 border-blue-200/60 shadow-lg mt-4">
          <div className="text-blue-900 font-semibold mb-4 flex items-center gap-2">
            <Volume2 className="w-5 h-5 text-blue-600" />
            Alarm Sound
          </div>
          <div className="space-y-3">
            {Object.entries(alarmSounds).filter(([, sound]) => !sound.hidden).map(([soundKey, sound]) => (
              <div key={soundKey}>
                <div
                  className={`p-3 rounded-xl transition-all cursor-pointer ${
                    selectedAlarmSound === soundKey
                      ? 'bg-gradient-to-r from-green-100 to-emerald-100 border-2 border-green-400 shadow-md'
                      : 'bg-white/50 hover:bg-white/70 border-2 border-white/40'
                  }`}
                  onClick={() => setSelectedAlarmSound(soundKey)}
                >
                  <div className="flex items-center justify-between">
                    <div>
                      <h3 className="font-semibold text-blue-900 text-sm">{sound.name}</h3>
                      <p className="text-xs text-blue-700">{sound.description}</p>
                    </div>
                    <div className="flex items-center space-x-2">
                      <button
                        className={`w-6 h-6 rounded-full flex items-center justify-center hover:scale-110 transition-all duration-200 shadow-lg ${
                          previewingSound === soundKey
                            ? 'bg-red-500/30 hover:bg-red-500/40 hover:shadow-red-500/20'
                            : 'bg-white/20 hover:bg-green-500/30 hover:shadow-green-500/20'
                        }`}
                        onClick={(e) => {
                          e.stopPropagation();
                          previewSound(soundKey);
                        }}
                        title={previewingSound === soundKey ? "Stop preview" : "Play preview"}
                      >
                        <span className={previewingSound === soundKey ? "text-red-400" : "text-green-400"} style={{fontSize: '10px'}}>
                          {previewingSound === soundKey ? "⏸" : "▶"}
                        </span>
                      </button>
                      {selectedAlarmSound === soundKey && (
                        <div className="w-5 h-5 rounded-full bg-green-500 flex items-center justify-center shadow-lg shadow-green-500/30 animate-pulse">
                          <span className="text-white font-bold" style={{fontSize: '10px'}}>✓</span>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
        {/* Theme Selection */}
        <div className="relative bg-white/70 backdrop-blur-2xl rounded-[2.5rem] p-6 border-2 border-blue-200/60 shadow-lg mt-4">
          <div className="text-blue-900 font-semibold mb-4 flex items-center gap-2">
            <Cloud className="w-5 h-5 text-blue-600" />
            Wake Up Theme
          </div>
          <select
            value={currentTheme}
            onChange={(e) => setCurrentTheme(e.target.value)}
            className="w-full bg-white/80 text-blue-900 rounded-lg px-2 py-1 border border-blue-400/60 focus:bg-white focus:border-blue-300 focus:outline-none shadow-sm text-xs"
          >
            {Object.entries(getCurrentThemes()).map(([themeKey, theme]) => (
              <option key={themeKey} value={themeKey} className="bg-white text-blue-900">
                {theme.name}
              </option>
            ))}
          </select>
        </div>
        {/* Save Button */}
        <div className="relative mt-4">
          <button
            onClick={() => {
              navigate('/');
            }}
            className="relative w-full bg-gradient-to-r from-emerald-500 to-green-500 text-white py-5 rounded-3xl font-semibold shadow-xl border-2 border-white/60 backdrop-blur-sm hover:shadow-2xl transition-all"
          >
            Save Alarm
          </button>
        </div>
      </div>
    </div>
  );
}

export default AlarmSettings;