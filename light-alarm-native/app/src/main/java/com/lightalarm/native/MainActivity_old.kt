package com.lightalarm.nativeapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import android.util.Log
import android.view.View
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var timePicker: TimePicker
    private lateinit var setAlarmButton: Button
    private lateinit var timeUntilAlarmText: TextView
    private lateinit var alarmList: LinearLayout
    
    private val alarms = mutableListOf<AlarmItem>()
    private var nextAlarmTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupViews()
        requestPermissions()
        updateTimeDisplay()
        setupTimePicker()
        setupAlarmButton()
        populateAlarmList()
    }

    private fun setupViews() {
        timePicker = findViewById(R.id.timePicker)
        setAlarmButton = findViewById(R.id.setAlarmButton)
        timeUntilAlarmText = findViewById(R.id.timeUntilAlarmText)
        alarmList = findViewById(R.id.alarmList)
    }

    private fun requestPermissions() {
        // Exact alarm permission for Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            val canSchedule = alarmManager.canScheduleExactAlarms()
            Log.d("MainActivity", "canScheduleExactAlarms=$canSchedule")
            if (!canSchedule) {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:$packageName")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Failed to open exact alarm settings", e)
                }
            }
        }
    }

    private fun updateTimeDisplay() {
        val currentTimeText: TextView = findViewById(R.id.currentTime)
        currentTimeText.text = getFormattedTime()
    }

    private fun setupTimePicker() {
        timePicker.setIs24HourView(false)
    }

    private fun setupAlarmButton() {
        setAlarmButton.setOnClickListener {
            scheduleAlarm()
        }
    }

    private fun scheduleAlarm() {
        val hour = timePicker.hour
        val minute = timePicker.minute
        
        // Calculate target alarm time
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        // If time has passed today, set for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        val alarmTime = calendar.timeInMillis
        
        // Calculate pre-alarm time (20 minutes earlier)
        val preAlarmTime = alarmTime - (20 * 60 * 1000) // 20 minutes in milliseconds
        
        // Schedule the pre-alarm (light stimulation)
        schedulePreAlarm(preAlarmTime)
        
        // Schedule the main alarm (sound)
        scheduleMainAlarm(alarmTime)
        
        // Update UI
        updateEstimationText()
        
        // Add to alarms list
        val formattedTime = String.format("%02d:%02d %s",
            if (hour % 12 == 0) 12 else hour % 12,
            minute,
            if (hour < 12) "AM" else "PM"
        )
        
        val newAlarm = AlarmItem(
            id = System.currentTimeMillis().toInt(),
            time = formattedTime,
            repeatDays = "Daily",
            theme = "Sunrise",
            soundCategory = "Classic Alarm",
            isEnabled = true
        )
        
        alarms.add(newAlarm)
        updateAlarmList()
    }

    private fun schedulePreAlarm(alarmTime: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            action = "com.lightalarm.nativeapp.ALARM_TRIGGERED"
            putExtra("theme", "Sunrise")
            putExtra("alarm_time", alarmTime)
            putExtra("is_pre_alarm", true)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            this, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                alarmTime,
                pendingIntent
            )
        }
    }

    private fun scheduleMainAlarm(alarmTime: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            action = "com.lightalarm.nativeapp.ALARM_TRIGGERED"
                putExtra("theme", "Sunrise")
            putExtra("alarm_time", alarmTime)
            putExtra("is_pre_alarm", false)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            this, 2, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                alarmTime,
                pendingIntent
            )
        }
    }

    private fun updateTimeUntilDisplay(alarmTime: Long) {
        val now = System.currentTimeMillis()
        val timeDiff = alarmTime - now
        
        if (timeDiff > 0) {
            val hours = timeDiff / (1000 * 60 * 60)
            val minutes = (timeDiff % (1000 * 60 * 60)) / (1000 * 60)
            
            timeUntilAlarmText.text = "Alarm in $hours hours and $minutes minutes"
            nextAlarmTime = alarmTime
        } else {
            timeUntilAlarmText.text = "No alarm set"
        }
    }

    // Replace estimation logic: only enabled alarms considered; show soonest
    private fun updateEstimationText() {
        val enabledAlarms = alarms.filter { it.isEnabled }

        if (enabledAlarms.isEmpty()) {
            timeUntilAlarmText.visibility = View.GONE
            nextAlarmTime = 0
            return
        }

        val now = Calendar.getInstance()
        var soonestAlarmTime: Calendar? = null

        val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())

        for (alarm in enabledAlarms) {
            try {
                val alarmTime = Calendar.getInstance()
                val parsedDate = timeFormat.parse(alarm.time) ?: continue
                val parsedTime = Calendar.getInstance().apply { time = parsedDate }

                alarmTime.set(Calendar.HOUR_OF_DAY, parsedTime.get(Calendar.HOUR_OF_DAY))
                alarmTime.set(Calendar.MINUTE, parsedTime.get(Calendar.MINUTE))
                alarmTime.set(Calendar.SECOND, 0)
                alarmTime.set(Calendar.MILLISECOND, 0)

                if (alarmTime.before(now)) {
                    alarmTime.add(Calendar.DAY_OF_YEAR, 1)
                }

                if (soonestAlarmTime == null || alarmTime.before(soonestAlarmTime)) {
                    soonestAlarmTime = alarmTime
                }

            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to parse alarm time: ${alarm.time}", e)
            }
        }

        if (soonestAlarmTime != null) {
            nextAlarmTime = soonestAlarmTime!!.timeInMillis
            val diff = nextAlarmTime - now.timeInMillis
            val hours = diff / (1000 * 60 * 60)
            val minutes = (diff / (1000 * 60)) % 60

            timeUntilAlarmText.text = "Alarm in $hours hours and $minutes minutes"
            timeUntilAlarmText.visibility = View.VISIBLE
        } else {
            timeUntilAlarmText.visibility = View.GONE
            nextAlarmTime = 0
        }
    }

    private fun populateAlarmList() {
        val inflater = LayoutInflater.from(this)
        
        // Add sample alarms for UI preview
        val samples = listOf(
            AlarmItem(1, "07:00 AM", "Weekdays", "Sunrise", "Classic Alarm", true),
            AlarmItem(2, "08:30 AM", "Weekends", "Aurora", "Ambient Sounds", false)
        )
        
        samples.forEach { alarm ->
            val item = inflater.inflate(R.layout.item_alarm, alarmList, false)
            val timeText = item.findViewById<TextView>(R.id.alarmTime)
            val detailsText = item.findViewById<TextView>(R.id.alarmDetails)
            val toggle = item.findViewById<Switch>(R.id.alarmToggle)
            
            timeText.text = alarm.time
            detailsText.text = "${alarm.repeatDays} • ${alarm.theme}"
            toggle.isChecked = alarm.isEnabled
            
            alarmList.addView(item)
        }
    }

    private fun updateAlarmList() {
        // This would update the UI with the new alarm
        // For now, just log it
        println("Alarm added: ${alarms.last()}")
    }

    private fun getFormattedTime(): String {
        val now = Calendar.getInstance().time
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(now)
    }
}