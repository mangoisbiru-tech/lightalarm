package com.lightalarm.nativeapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AddAlarmActivity : AppCompatActivity() {

    private lateinit var timePicker: TimePicker
    private lateinit var repeatSpinner: Spinner
    private lateinit var soundButton: Button
    private lateinit var soundNameText: TextView
    private lateinit var themeSpinner: Spinner
    private lateinit var saveButton: Button
    
    private var selectedSound: SoundItem? = null
    
    companion object {
        private const val REQUEST_SOUND_PICKER = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_alarm)

        timePicker = findViewById(R.id.timePicker)
        repeatSpinner = findViewById(R.id.repeatSpinner)
        soundButton = findViewById(R.id.soundButton)
        soundNameText = findViewById(R.id.soundNameText)
        themeSpinner = findViewById(R.id.themeSpinner)
        saveButton = findViewById(R.id.saveButton)

        setupSpinners()
        setupSoundPicker()
        setupSaveButton()
    }

    private fun setupSpinners() {
        // Repeat options
        repeatSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Daily", "Weekdays", "Weekends", "Custom")
        )

        // All 8 themes from codebase
        themeSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf(
                "Sunrise", "Green Grass", "Blue Sea", "Sephora Blue",
                "Pastel Green", "Aurora", "Pink Ocean", "Lavender"
            )
        )
    }

    private fun setupSoundPicker() {
        soundButton.setOnClickListener {
            val intent = Intent(this, SoundPickerActivity::class.java)
            startActivityForResult(intent, REQUEST_SOUND_PICKER)
        }
    }

    private fun setupSaveButton() {
        saveButton.setOnClickListener { saveAlarm() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SOUND_PICKER && resultCode == Activity.RESULT_OK) {
            data?.getSerializableExtra("selected_sound")?.let {
                selectedSound = it as SoundItem
                soundNameText.text = selectedSound?.displayName ?: "Select Sound"
            }
        }
    }

    private fun saveAlarm() {
        val hour = timePicker.hour
        val minute = timePicker.minute
        val repeat = repeatSpinner.selectedItem.toString()
        val sound = selectedSound?.displayName ?: "Digital Alarm"
        val theme = themeSpinner.selectedItem.toString()

        val formattedTime = String.format("%02d:%02d %s",
            if (hour % 12 == 0) 12 else hour % 12,
            minute,
            if (hour < 12) "AM" else "PM"
        )

        val alarm = AlarmItem(
            id = 0, // Will be set by MainActivity
            time = formattedTime,
            repeatDays = repeat,
            theme = theme,
            soundCategory = sound,
            isEnabled = true // New alarms are enabled by default
        )

        // Pass alarm back to MainActivity
        val resultIntent = Intent().apply {
            putExtra("new_alarm", alarm)
        }
        setResult(RESULT_OK, resultIntent)
        
        Toast.makeText(this, "Alarm saved: $formattedTime", Toast.LENGTH_SHORT).show()
        finish()
    }
}