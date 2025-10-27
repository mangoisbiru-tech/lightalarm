package com.lightalarm.nativeapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SoundPickerActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SoundPickerAdapter
    private val sounds = mutableListOf<SoundItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sound_picker)

        setupSoundList()
        setupRecyclerView()
    }

    private fun setupSoundList() {
        // Populate with all available sounds from res/raw
        // Note: Resource names have been normalized to lowercase with underscores
        
        // Classic Alarm category
        sounds.add(SoundItem(1, "Alarm 2", R.raw.classicalarm_alarm2_wav, "Classic Alarm"))
        sounds.add(SoundItem(2, "Alarm 3", R.raw.classicalarm_alarm3_wav, "Classic Alarm"))
        sounds.add(SoundItem(3, "Digital Alarm", R.raw.classicalarm_digital_alarm_wav, "Classic Alarm"))
        sounds.add(SoundItem(4, "Polite Warning", R.raw.classicalarm_polite_warning_wav, "Classic Alarm"))
        sounds.add(SoundItem(5, "Ringtone", R.raw.classicalarm_ringtone_wav, "Classic Alarm"))
        
        // Ambience Sounds category
        sounds.add(SoundItem(6, "Airport Luggage", R.raw.ambiencesound_airport_luggage_wheels_on_bricks_wav, "Ambient Sounds"))
        sounds.add(SoundItem(7, "Cafe", R.raw.ambiencesound_cafe_wav, "Ambient Sounds"))
        sounds.add(SoundItem(8, "School", R.raw.ambiencesound_school_wav, "Ambient Sounds"))
        sounds.add(SoundItem(9, "Street Basketball", R.raw.ambiencesound_street_basketball_wav, "Ambient Sounds"))
        
        // Natural Sounds category
        sounds.add(SoundItem(10, "Forest Sounds", R.raw.naturalsound_forest_sounds_wav, "Natural Sounds"))
        sounds.add(SoundItem(11, "Relaxing Sea", R.raw.naturalsound_relaxing_sea_mp3, "Natural Sounds"))
        sounds.add(SoundItem(12, "Underwater", R.raw.naturalsound_underwater_flac, "Natural Sounds"))
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.soundRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        
        adapter = SoundPickerAdapter(this, sounds) { selectedSound ->
            // Return selected sound to calling activity
            val resultIntent = Intent().apply {
                putExtra("selected_sound", selectedSound)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
        
        recyclerView.adapter = adapter
    }

    override fun onStop() {
        super.onStop()
        adapter.releasePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.releasePlayer()
    }
}





