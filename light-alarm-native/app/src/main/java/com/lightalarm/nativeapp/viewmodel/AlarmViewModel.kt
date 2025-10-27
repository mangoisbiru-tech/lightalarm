package com.lightalarm.nativeapp.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lightalarm.nativeapp.data.AlarmConfig
import com.lightalarm.nativeapp.data.LightTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class AlarmViewModel(application: Application) : AndroidViewModel(application) {
    
    private val context = getApplication<Application>()
    private val prefs: SharedPreferences = context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    private val _alarms = MutableStateFlow<List<AlarmConfig>>(emptyList())
    val alarms: StateFlow<List<AlarmConfig>> = _alarms.asStateFlow()
    
    private val _nextAlarmTime = MutableStateFlow<Long>(0)
    val nextAlarmTime: StateFlow<Long> = _nextAlarmTime.asStateFlow()
    
    private val _selectedTheme = MutableStateFlow(LightTheme.SUNRISE)
    val selectedTheme: StateFlow<LightTheme> = _selectedTheme.asStateFlow()
    
    init {
        loadAlarms()
    }
    
    fun addAlarm(alarm: AlarmConfig) {
        viewModelScope.launch {
            val currentAlarms = _alarms.value.toMutableList()
            val newAlarm = alarm.copy(id = System.currentTimeMillis().toInt())
            currentAlarms.add(newAlarm)
            _alarms.value = currentAlarms
            saveAlarms()
            updateNextAlarmTime()
        }
    }
    
    fun removeAlarm(alarmId: Int) {
        viewModelScope.launch {
            val currentAlarms = _alarms.value.toMutableList()
            currentAlarms.removeAll { it.id == alarmId }
            _alarms.value = currentAlarms
            saveAlarms()
            updateNextAlarmTime()
        }
    }
    
    fun toggleAlarm(alarmId: Int) {
        viewModelScope.launch {
            val currentAlarms = _alarms.value.toMutableList()
            val index = currentAlarms.indexOfFirst { it.id == alarmId }
            if (index != -1) {
                currentAlarms[index] = currentAlarms[index].copy(isEnabled = !currentAlarms[index].isEnabled)
                _alarms.value = currentAlarms
                saveAlarms()
                updateNextAlarmTime()
            }
        }
    }
    
    fun getTimeUntilNextAlarm(): String {
        val nextTime = _nextAlarmTime.value
        if (nextTime == 0L) return "No alarm set"
        
        val now = System.currentTimeMillis()
        val diff = nextTime - now
        
        if (diff <= 0) return "No alarm set"
        
        val hours = diff / (1000 * 60 * 60)
        val minutes = (diff / (1000 * 60)) % 60
        
        return "Alarm in $hours hours and $minutes minutes"
    }
    
    private fun loadAlarms() {
        val alarmsJson = prefs.getString("alarms", null)
        if (alarmsJson != null) {
            try {
                val type = object : TypeToken<List<AlarmConfig>>() {}.type
                val loadedAlarms: List<AlarmConfig> = gson.fromJson(alarmsJson, type)
                _alarms.value = loadedAlarms
                updateNextAlarmTime()
            } catch (e: Exception) {
                _alarms.value = emptyList()
            }
        }
    }
    
    private fun saveAlarms() {
        val alarmsJson = gson.toJson(_alarms.value)
        prefs.edit().putString("alarms", alarmsJson).apply()
    }
    
    private fun updateNextAlarmTime() {
        val enabledAlarms = _alarms.value.filter { it.isEnabled }
        if (enabledAlarms.isEmpty()) {
            _nextAlarmTime.value = 0
            return
        }
        
        val now = Calendar.getInstance()
        var soonestTime: Long = Long.MAX_VALUE
        
        for (alarm in enabledAlarms) {
            val alarmTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, alarm.hour)
                set(Calendar.MINUTE, alarm.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                
                // If time has passed today, set for tomorrow
                if (timeInMillis <= now.timeInMillis) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }
            
            if (alarmTime.timeInMillis < soonestTime) {
                soonestTime = alarmTime.timeInMillis
            }
        }
        
        _nextAlarmTime.value = if (soonestTime == Long.MAX_VALUE) 0 else soonestTime
    }
}
